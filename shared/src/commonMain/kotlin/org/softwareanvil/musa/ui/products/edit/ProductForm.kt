package org.softwareanvil.musa.ui.products.edit

import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductKind
import org.softwareanvil.musa.domain.model.ProductStatus
import org.softwareanvil.musa.domain.model.VariantId

/** Raw text of the editor, exactly as typed; parsed into a draft on save. */
data class ProductForm(
	val id: ProductId,
	val kind: ProductKind,
	val name: String = "",
	val description: String = "",
	val tagNames: List<String> = emptyList(),
	val status: ProductStatus = ProductStatus.ACTIVE,
	val material: String = "",
	val deliveryFormat: String = "",
	val turnaroundDays: String = "",
	val variants: List<VariantForm>,
)

/** Editable fields of one variant. */
enum class VariantField { NAME, PRICE, COST, SKU, STOCK, MIN_STOCK, WEIGHT, WIDTH, HEIGHT, DEPTH }

/** Raw text of one variant. */
data class VariantForm(
	val id: VariantId,
	val values: Map<VariantField, String> = mapOf(VariantField.STOCK to "0"),
) {

	/** Text of [field], empty when never typed. */
	operator fun get(field: VariantField): String = values[field].orEmpty()

	/** Copy with [field] set to [value]. */
	fun with(field: VariantField, value: String) = copy(values = values + (field to value))
}

/** Where an error is shown in the editor. */
sealed interface FormField {
	data object Name : FormField
	data object TurnaroundDays : FormField
	data object Variants : FormField
	data class Variant(val id: VariantId, val field: VariantField) : FormField
}
