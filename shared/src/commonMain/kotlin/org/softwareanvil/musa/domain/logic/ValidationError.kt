package org.softwareanvil.musa.domain.logic

import org.softwareanvil.musa.domain.model.VariantId

/** Reasons a product draft cannot be saved. */
sealed interface ValidationError {
	data object BlankName : ValidationError
	data object NoVariants : ValidationError
	data object NegativeTurnaround : ValidationError
	data class BlankVariantName(val variantId: VariantId) : ValidationError
	data class DuplicateVariantName(val variantId: VariantId) : ValidationError
	data class NegativePrice(val variantId: VariantId) : ValidationError
	data class NegativeCost(val variantId: VariantId) : ValidationError
	data class NegativeStock(val variantId: VariantId) : ValidationError
	data class NegativeMinStock(val variantId: VariantId) : ValidationError
	data class InvalidMeasure(val variantId: VariantId) : ValidationError

	/** [ownerName] is the other product using the SKU, or null when it repeats inside this product. */
	data class DuplicateSku(val variantId: VariantId, val ownerName: String?) : ValidationError
}
