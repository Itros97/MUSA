package org.softwareanvil.musa.fakes

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.softwareanvil.musa.domain.logic.IdGenerator
import org.softwareanvil.musa.domain.logic.TagNormalizer
import org.softwareanvil.musa.domain.model.DigitalProduct
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.Product
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductStatus
import org.softwareanvil.musa.domain.model.Tag
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.domain.repository.ProductRepository
import org.softwareanvil.musa.domain.repository.TagRepository

class FakeProductRepository(initial: List<Product> = emptyList()) : ProductRepository {

	val products = MutableStateFlow(initial.associateBy { it.id })

	override fun observeAll(): Flow<List<Product>> = products.map { it.values.toList() }

	override suspend fun findById(id: ProductId) = products.value[id]

	override suspend fun findSkuOwnerName(sku: String, excluding: ProductId): String? = products.value.values
		.filterIsInstance<PhysicalProduct>()
		.firstOrNull { product -> product.id != excluding && product.variants.any { it.sku == sku } }
		?.name

	override suspend fun save(product: Product) = products.update { it + (product.id to product) }

	override suspend fun adjustStock(variantId: VariantId, delta: Int, at: Instant) = products.update { all ->
		all.mapValues { (_, product) ->
			if (product !is PhysicalProduct) return@mapValues product
			product.copy(variants = product.variants.map {
				if (it.id == variantId) it.copy(stock = maxOf(0, it.stock + delta)) else it
			})
		}
	}

	override suspend fun updateStatus(id: ProductId, status: ProductStatus, at: Instant) = products.update { all ->
		val product = all.getValue(id)
		all + (id to when (product) {
			is PhysicalProduct -> product.copy(status = status, updatedAt = at)
			is DigitalProduct -> product.copy(status = status, updatedAt = at)
		})
	}

	override suspend fun delete(id: ProductId, at: Instant) = products.update { it - id }
}

class FakeTagRepository(initial: List<Tag> = emptyList()) : TagRepository {

	val tags = MutableStateFlow(initial)

	override fun observeAll(): Flow<List<Tag>> = tags

	override suspend fun findByKey(key: String) = tags.value.firstOrNull { TagNormalizer.key(it.name) == key }

	override suspend fun insert(tag: Tag, at: Instant) = tags.update { it + tag }
}

class FixedClock(var now: Instant = EPOCH) : Clock {
	override fun now() = now
}

class SequentialIds(private val prefix: String = "id") : IdGenerator {
	private var counter = 0
	override fun next() = "$prefix-${++counter}"
}
