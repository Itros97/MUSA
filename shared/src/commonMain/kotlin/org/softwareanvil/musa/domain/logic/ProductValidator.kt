package org.softwareanvil.musa.domain.logic

import org.softwareanvil.musa.domain.logic.ValidationError.BlankName
import org.softwareanvil.musa.domain.logic.ValidationError.BlankVariantName
import org.softwareanvil.musa.domain.logic.ValidationError.DuplicateSku
import org.softwareanvil.musa.domain.logic.ValidationError.DuplicateVariantName
import org.softwareanvil.musa.domain.logic.ValidationError.InvalidMeasure
import org.softwareanvil.musa.domain.logic.ValidationError.NegativeCost
import org.softwareanvil.musa.domain.logic.ValidationError.NegativeMinStock
import org.softwareanvil.musa.domain.logic.ValidationError.NegativePrice
import org.softwareanvil.musa.domain.logic.ValidationError.NegativeStock
import org.softwareanvil.musa.domain.logic.ValidationError.NegativeTurnaround
import org.softwareanvil.musa.domain.logic.ValidationError.NoVariants
import org.softwareanvil.musa.domain.model.DigitalDetails
import org.softwareanvil.musa.domain.model.PhysicalDetails
import org.softwareanvil.musa.domain.model.PhysicalVariant
import org.softwareanvil.musa.domain.model.ProductDraft
import org.softwareanvil.musa.domain.model.Variant

/** Checks the rules a draft must meet before saving; checks needing storage live in the use case. */
class ProductValidator {

	/** Returns every problem found; an empty list means the draft is valid. */
	fun validate(draft: ProductDraft): List<ValidationError> = buildList {
		if (draft.name.isBlank()) add(BlankName)
		val variants = draft.details.variants
		if (variants.isEmpty()) add(NoVariants)
		addAll(validateVariantNames(variants))
		variants.forEach { addAll(validateVariant(it)) }
		when (val details = draft.details) {
			is PhysicalDetails -> addAll(validateSkus(details.variants))
			is DigitalDetails -> if ((details.turnaroundDays ?: 0) < 0) add(NegativeTurnaround)
		}
	}

	// A single variant is shown as "the product" in the UI, so it may stay unnamed.
	private fun validateVariantNames(variants: List<Variant>): List<ValidationError> {
		if (variants.size < 2) return emptyList()
		val seen = mutableSetOf<String>()
		return variants.mapNotNull { variant ->
			val key = TextNormalizer.fold(variant.name.trim())
			when {
				key.isEmpty() -> BlankVariantName(variant.id)
				!seen.add(key) -> DuplicateVariantName(variant.id)
				else -> null
			}
		}
	}

	private fun validateVariant(variant: Variant): List<ValidationError> = buildList {
		if (variant.price.cents < 0) add(NegativePrice(variant.id))
		if (variant.cost.cents < 0) add(NegativeCost(variant.id))
		if (variant !is PhysicalVariant) return@buildList
		if (variant.stock < 0) add(NegativeStock(variant.id))
		if ((variant.minStock ?: 0) < 0) add(NegativeMinStock(variant.id))
		val measures = listOf(variant.weightGrams, variant.widthMm, variant.heightMm, variant.depthMm)
		if (measures.any { it != null && it <= 0 }) add(InvalidMeasure(variant.id))
	}

	private fun validateSkus(variants: List<PhysicalVariant>): List<ValidationError> {
		val seen = mutableSetOf<String>()
		return variants
			.filter { !it.sku.isNullOrBlank() }
			.filterNot { seen.add(it.sku!!.trim().lowercase()) }
			.map { DuplicateSku(it.id, ownerName = null) }
	}
}
