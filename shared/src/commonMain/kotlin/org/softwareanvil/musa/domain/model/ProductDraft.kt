package org.softwareanvil.musa.domain.model

/**
 * What the editor submits to create or update a product; timestamps and tags are resolved on save.
 * [stockBaseline] is the stock each variant had when the editor opened, so changes made elsewhere meanwhile are kept.
 */
data class ProductDraft(
	val id: ProductId,
	val name: String,
	val description: String,
	val tagNames: List<String>,
	val status: ProductStatus,
	val details: ProductDetails,
	val stockBaseline: Map<VariantId, Int> = emptyMap(),
)

/** Kind-specific part of a draft. */
sealed interface ProductDetails {
	val variants: List<Variant>
}

/** Physical-only fields of a draft. */
data class PhysicalDetails(
	val material: String,
	override val variants: List<PhysicalVariant>,
) : ProductDetails

/** Digital-only fields of a draft. */
data class DigitalDetails(
	val deliveryFormat: String,
	val turnaroundDays: Int?,
	override val variants: List<DigitalVariant>,
) : ProductDetails
