package org.softwareanvil.musa.data.repository

import org.softwareanvil.musa.db.MusaDatabase
import org.softwareanvil.musa.domain.model.DigitalProduct
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.PhysicalVariant
import org.softwareanvil.musa.domain.model.Product
import org.softwareanvil.musa.domain.model.Variant

/** Writes a product into its rows; callers wrap [write] in a transaction. */
internal class ProductRowWriter(private val database: MusaDatabase) {

	/** Upserts the product, its details, variants and tag links; variants no longer present are soft-deleted. */
	fun write(product: Product) {
		upsertProduct(product)
		product.variants.forEachIndexed { position, variant -> upsertVariant(product, variant, position) }
		database.productVariantQueries.softDeleteMissing(
			at = product.updatedAt.toEpochMilliseconds(),
			productId = product.id.value,
			keep = product.variants.map { it.id.value },
		)
		database.productTagQueries.deleteByProduct(product.id.value)
		product.tags.forEachIndexed { position, tag ->
			database.productTagQueries.insert(product.id.value, tag.id.value, position.toLong())
		}
	}

	private fun upsertProduct(product: Product) {
		val queries = database.productQueries
		val id = product.id.value
		val updatedAt = product.updatedAt.toEpochMilliseconds()
		if (queries.exists(id).executeAsOne() > 0) {
			queries.update(product.name, product.description, product.status.name, updatedAt, id)
		} else {
			val createdAt = product.createdAt.toEpochMilliseconds()
			queries.insert(id, product.kind.name, product.name, product.description, product.status.name, createdAt, updatedAt)
		}
		when (product) {
			is PhysicalProduct -> database.physicalProductQueries.upsert(id, product.material)
			is DigitalProduct -> database.digitalProductQueries.upsert(id, product.deliveryFormat, product.turnaroundDays?.toLong())
		}
	}

	private fun upsertVariant(product: Product, variant: Variant, position: Int) {
		val queries = database.productVariantQueries
		val id = variant.id.value
		val updatedAt = product.updatedAt.toEpochMilliseconds()
		val price = variant.price.cents
		val cost = variant.cost.cents
		if (queries.exists(id).executeAsOne() > 0) {
			queries.update(variant.name, price, cost, position.toLong(), updatedAt, id)
		} else {
			queries.insert(id, product.id.value, variant.name, price, cost, position.toLong(), updatedAt)
		}
		if (variant is PhysicalVariant) upsertPhysicalVariant(variant)
	}

	private fun upsertPhysicalVariant(variant: PhysicalVariant) = database.physicalVariantQueries.upsert(
		variant_id = variant.id.value,
		sku = variant.sku,
		stock = variant.stock.toLong(),
		min_stock = variant.minStock?.toLong(),
		weight_grams = variant.weightGrams?.toLong(),
		width_mm = variant.widthMm?.toLong(),
		height_mm = variant.heightMm?.toLong(),
		depth_mm = variant.depthMm?.toLong(),
	)
}
