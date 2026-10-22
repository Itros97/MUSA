package org.softwareanvil.musa.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.softwareanvil.musa.data.mapper.ProductMapper
import org.softwareanvil.musa.data.mapper.VariantRow
import org.softwareanvil.musa.db.MusaDatabase
import org.softwareanvil.musa.domain.model.Product
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductStatus
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.domain.repository.ProductRepository
import org.softwareanvil.musa.db.Product as ProductRow

/** SQLDelight-backed [ProductRepository]. Every write touches the product row so observers refresh. */
class ProductRepositoryImpl(
	private val database: MusaDatabase,
	private val dispatcher: CoroutineDispatcher,
) : ProductRepository {

	private val writer = ProductRowWriter(database)
	private val stockWriter = CatalogueStockWriter(database)

	// region read

	override fun observeAll(): Flow<List<Product>> = database.productQueries.selectAll()
		.asFlow()
		.mapToList(dispatcher)
		.map { rows -> withContext(dispatcher) { loadAll(rows) } }

	override suspend fun findById(id: ProductId): Product? = withContext(dispatcher) {
		val row = database.productQueries.selectById(id.value).executeAsOneOrNull() ?: return@withContext null
		ProductMapper.toDomain(
			row = row,
			variants = database.productVariantQueries.selectByProduct(id.value, ::VariantRow).executeAsList(),
			tags = database.productTagQueries.selectByProduct(id.value) { tagId, name, _, color, _, _, _ ->
				ProductMapper.toTag(tagId, name, color)
			}.executeAsList(),
			physical = database.physicalProductQueries.selectByProduct(id.value).executeAsOneOrNull(),
			digital = database.digitalProductQueries.selectByProduct(id.value).executeAsOneOrNull(),
		)
	}

	override suspend fun findSkuOwnerName(sku: String, excluding: ProductId): String? = withContext(dispatcher) {
		database.physicalVariantQueries.selectSkuOwnerName(sku, excluding.value).executeAsOneOrNull()
	}

	override suspend fun findStock(variantId: VariantId): Int? = withContext(dispatcher) {
		database.physicalVariantQueries.selectStock(variantId.value).executeAsOneOrNull()?.toInt()
	}

	private fun loadAll(rows: List<ProductRow>): List<Product> {
		val variants = database.productVariantQueries.selectAll(::VariantRow).executeAsList().groupBy { it.productId }
		val tags = database.productTagQueries.selectAll { productId, tagId, name, _, color, _, _, _ ->
			productId to ProductMapper.toTag(tagId, name, color)
		}.executeAsList().groupBy({ it.first }, { it.second })
		val physical = database.physicalProductQueries.selectAll().executeAsList().associateBy { it.product_id }
		val digital = database.digitalProductQueries.selectAll().executeAsList().associateBy { it.product_id }
		return rows.map {
			ProductMapper.toDomain(it, variants[it.id].orEmpty(), tags[it.id].orEmpty(), physical[it.id], digital[it.id])
		}
	}

	// endregion

	// region write

	override suspend fun save(product: Product) = withContext(dispatcher) {
		database.transaction { writer.write(product) }
	}

	override suspend fun adjustStock(variantId: VariantId, delta: Int, at: Instant) = withContext(dispatcher) {
		database.transaction { stockWriter.adjust(variantId.value, delta.toLong(), at.toEpochMilliseconds()) }
	}

	override suspend fun updateStatus(id: ProductId, status: ProductStatus, at: Instant) = withContext(dispatcher) {
		database.productQueries.updateStatus(status.name, at.toEpochMilliseconds(), id.value)
		Unit
	}

	override suspend fun delete(id: ProductId, at: Instant) = withContext(dispatcher) {
		database.productQueries.softDelete(at.toEpochMilliseconds(), at.toEpochMilliseconds(), id.value)
		Unit
	}

	// endregion
}
