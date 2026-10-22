package org.softwareanvil.musa.domain.usecase

import kotlin.time.Clock
import kotlin.time.Instant
import org.softwareanvil.musa.domain.logic.IdGenerator
import org.softwareanvil.musa.domain.logic.ProductValidator
import org.softwareanvil.musa.domain.logic.TagNormalizer
import org.softwareanvil.musa.domain.logic.ValidationError
import org.softwareanvil.musa.domain.model.DigitalDetails
import org.softwareanvil.musa.domain.model.DigitalProduct
import org.softwareanvil.musa.domain.model.PhysicalDetails
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.Product
import org.softwareanvil.musa.domain.model.ProductDraft
import org.softwareanvil.musa.domain.model.Tag
import org.softwareanvil.musa.domain.model.TagId
import org.softwareanvil.musa.domain.repository.ProductRepository
import org.softwareanvil.musa.domain.repository.TagRepository

/** Outcome of saving a draft. */
sealed interface SaveResult {
	data class Saved(val product: Product) : SaveResult
	data class Invalid(val errors: List<ValidationError>) : SaveResult
}

/** Validates a draft, resolves its tags and stores it as a product. */
class SaveProductUseCase(
	private val productRepository: ProductRepository,
	private val tagRepository: TagRepository,
	private val validator: ProductValidator,
	private val clock: Clock,
	private val ids: IdGenerator,
) {

	suspend operator fun invoke(draft: ProductDraft): SaveResult {
		val clean = draft.trimmed()
		val errors = validator.validate(clean) + findTakenSkus(clean)
		if (errors.isNotEmpty()) return SaveResult.Invalid(errors)
		val now = clock.now()
		val createdAt = productRepository.findById(clean.id)?.createdAt ?: now
		val product = clean.toProduct(resolveTags(clean.tagNames, now), createdAt, now)
		productRepository.save(product)
		return SaveResult.Saved(product)
	}

	private suspend fun findTakenSkus(draft: ProductDraft): List<ValidationError> {
		val details = draft.details as? PhysicalDetails ?: return emptyList()
		return details.variants.mapNotNull { variant ->
			val sku = variant.sku ?: return@mapNotNull null
			productRepository.findSkuOwnerName(sku, excluding = draft.id)
				?.let { ValidationError.DuplicateSku(variant.id, it) }
		}
	}

	private suspend fun resolveTags(names: List<String>, now: Instant): List<Tag> =
		TagNormalizer.distinct(names).map { name ->
			val key = TagNormalizer.key(name)
			tagRepository.findByKey(key)
				?: Tag(TagId(ids.next()), name, TagNormalizer.colorFor(key)).also { tagRepository.insert(it, now) }
		}
}

private fun ProductDraft.trimmed(): ProductDraft = copy(
	name = name.trim(),
	description = description.trim(),
	details = when (details) {
		is PhysicalDetails -> details.copy(
			material = details.material.trim(),
			variants = details.variants.map { it.copy(name = it.name.trim(), sku = it.sku?.trim()?.ifEmpty { null }) },
		)
		is DigitalDetails -> details.copy(
			deliveryFormat = details.deliveryFormat.trim(),
			variants = details.variants.map { it.copy(name = it.name.trim()) },
		)
	},
)

private fun ProductDraft.toProduct(tags: List<Tag>, createdAt: Instant, updatedAt: Instant): Product =
	when (details) {
		is PhysicalDetails -> PhysicalProduct(
			id = id,
			name = name,
			description = description,
			tags = tags,
			status = status,
			createdAt = createdAt,
			updatedAt = updatedAt,
			variants = details.variants,
			material = details.material,
		)
		is DigitalDetails -> DigitalProduct(
			id = id,
			name = name,
			description = description,
			tags = tags,
			status = status,
			createdAt = createdAt,
			updatedAt = updatedAt,
			variants = details.variants,
			deliveryFormat = details.deliveryFormat,
			turnaroundDays = details.turnaroundDays,
		)
	}
