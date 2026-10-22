package org.softwareanvil.musa.domain.usecase

import kotlin.time.Clock
import org.softwareanvil.musa.domain.logic.IdGenerator
import org.softwareanvil.musa.domain.model.DigitalProduct
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductStatus
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.domain.repository.ProductRepository

/** Copies a product as a new draft; stock starts at zero and SKUs are cleared because they must be unique. */
class DuplicateProductUseCase(
	private val repository: ProductRepository,
	private val clock: Clock,
	private val ids: IdGenerator,
) {

	/** Returns the id of the copy, named [copyName]. */
	suspend operator fun invoke(id: ProductId, copyName: String): ProductId {
		val original = checkNotNull(repository.findById(id)) { "Cannot duplicate missing product ${id.value}" }
		val now = clock.now()
		val newId = ProductId(ids.next())
		val copy = when (original) {
			is PhysicalProduct -> original.copy(
				id = newId,
				name = copyName,
				status = ProductStatus.DRAFT,
				createdAt = now,
				updatedAt = now,
				variants = original.variants.map { it.copy(id = VariantId(ids.next()), sku = null, stock = 0) },
			)
			is DigitalProduct -> original.copy(
				id = newId,
				name = copyName,
				status = ProductStatus.DRAFT,
				createdAt = now,
				updatedAt = now,
				variants = original.variants.map { it.copy(id = VariantId(ids.next())) },
			)
		}
		repository.save(copy)
		return newId
	}
}
