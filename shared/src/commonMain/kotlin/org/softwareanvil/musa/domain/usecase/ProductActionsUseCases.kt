package org.softwareanvil.musa.domain.usecase

import kotlin.time.Clock
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductStatus
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.domain.repository.ProductRepository

/** Adds or removes units of a physical variant; stock never drops below zero. */
class AdjustStockUseCase(private val repository: ProductRepository, private val clock: Clock) {
	suspend operator fun invoke(variantId: VariantId, delta: Int) = repository.adjustStock(variantId, delta, clock.now())
}

/** Archives a product, or restores it as active. */
class ArchiveProductUseCase(private val repository: ProductRepository, private val clock: Clock) {
	suspend operator fun invoke(id: ProductId, archived: Boolean) {
		val status = if (archived) ProductStatus.ARCHIVED else ProductStatus.ACTIVE
		repository.updateStatus(id, status, clock.now())
	}
}

/** Deletes a product from the catalogue. */
class DeleteProductUseCase(private val repository: ProductRepository, private val clock: Clock) {
	suspend operator fun invoke(id: ProductId) = repository.delete(id, clock.now())
}
