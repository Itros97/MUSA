package org.softwareanvil.musa.ui.products.edit

import org.softwareanvil.musa.domain.logic.ValidationError
import org.softwareanvil.musa.domain.model.VariantId

/** Where each domain validation error is shown and what it says. */
fun ValidationError.toFieldError(): Pair<FormField, String> = when (this) {
	ValidationError.BlankName -> FormField.Name to "Ponle un nombre"
	ValidationError.NoVariants -> FormField.Variants to "Añade al menos una variante"
	ValidationError.NegativeTurnaround -> FormField.TurnaroundDays to "No puede ser negativo"
	is ValidationError.BlankVariantName -> variant(variantId, VariantField.NAME) to "Ponle nombre (A4, Busto…)"
	is ValidationError.DuplicateVariantName -> variant(variantId, VariantField.NAME) to "Ya hay otra variante con este nombre"
	is ValidationError.NegativePrice -> variant(variantId, VariantField.PRICE) to "No puede ser negativo"
	is ValidationError.NegativeCost -> variant(variantId, VariantField.COST) to "No puede ser negativo"
	is ValidationError.NegativeStock -> variant(variantId, VariantField.STOCK) to "No puede ser negativo"
	is ValidationError.NegativeMinStock -> variant(variantId, VariantField.MIN_STOCK) to "No puede ser negativo"
	is ValidationError.InvalidMeasure -> variant(variantId, VariantField.WEIGHT) to "El peso y las medidas deben ser mayores que 0"
	is ValidationError.DuplicateSku -> variant(variantId, VariantField.SKU) to
		(ownerName?.let { "Ese SKU ya lo usa «$it»" } ?: "Otra variante ya usa este SKU")
}

private fun variant(id: VariantId, field: VariantField) = FormField.Variant(id, field)
