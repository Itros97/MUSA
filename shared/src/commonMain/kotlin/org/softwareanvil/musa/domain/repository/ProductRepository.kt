package org.softwareanvil.musa.domain.repository

import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import org.softwareanvil.musa.domain.model.Product
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductStatus
import org.softwareanvil.musa.domain.model.VariantId

/** Storage for products; deleted products are never returned. */
interface ProductRepository {

	/** Emits the whole catalogue every time it changes. */
	fun observeAll(): Flow<List<Product>>

	/** Returns the product, or null if it does not exist or was deleted. */
	suspend fun findById(id: ProductId): Product?

	/** Name of another product already using [sku], or null if it is free. */
	suspend fun findSkuOwnerName(sku: String, excluding: ProductId): String?

	/** Inserts or replaces the product with its variants and tag links. */
	suspend fun save(product: Product)

	/** Adds [delta] to the variant stock, never going below zero. */
	suspend fun adjustStock(variantId: VariantId, delta: Int, at: Instant)

	/** Changes only the status of a product. */
	suspend fun updateStatus(id: ProductId, status: ProductStatus, at: Instant)

	/** Soft-deletes the product so a future sync can propagate the deletion. */
	suspend fun delete(id: ProductId, at: Instant)
}
