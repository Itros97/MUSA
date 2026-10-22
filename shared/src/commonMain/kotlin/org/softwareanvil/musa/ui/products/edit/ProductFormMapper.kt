package org.softwareanvil.musa.ui.products.edit

import org.softwareanvil.musa.domain.logic.MoneyFormatter
import org.softwareanvil.musa.domain.logic.MoneyParser
import org.softwareanvil.musa.domain.logic.NumberParser
import org.softwareanvil.musa.domain.model.DigitalDetails
import org.softwareanvil.musa.domain.model.DigitalProduct
import org.softwareanvil.musa.domain.model.DigitalVariant
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.PhysicalDetails
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.PhysicalVariant
import org.softwareanvil.musa.domain.model.Product
import org.softwareanvil.musa.domain.model.ProductDraft
import org.softwareanvil.musa.domain.model.ProductKind
import org.softwareanvil.musa.domain.model.Variant
import org.softwareanvil.musa.ui.products.edit.VariantField.COST
import org.softwareanvil.musa.ui.products.edit.VariantField.DEPTH
import org.softwareanvil.musa.ui.products.edit.VariantField.HEIGHT
import org.softwareanvil.musa.ui.products.edit.VariantField.MIN_STOCK
import org.softwareanvil.musa.ui.products.edit.VariantField.NAME
import org.softwareanvil.musa.ui.products.edit.VariantField.PRICE
import org.softwareanvil.musa.ui.products.edit.VariantField.SKU
import org.softwareanvil.musa.ui.products.edit.VariantField.STOCK
import org.softwareanvil.musa.ui.products.edit.VariantField.WEIGHT
import org.softwareanvil.musa.ui.products.edit.VariantField.WIDTH

/** Draft built from the typed text; unreadable fields hold neutral placeholders and appear in [errors]. */
data class ParsedForm(val draft: ProductDraft, val errors: Map<FormField, String>)

/** Converts between products and the editor's raw text. */
object ProductFormMapper {

	/** Prefills the editor with a stored product. */
	fun fromProduct(product: Product): ProductForm {
		val base = ProductForm(
			id = product.id,
			kind = product.kind,
			name = product.name,
			description = product.description,
			tagNames = product.tags.map { it.name },
			status = product.status,
			variants = product.variants.map(::toVariantForm),
		)
		return when (product) {
			is PhysicalProduct -> base.copy(material = product.material, stockBaseline = product.variants.associate { it.id to it.stock })
			is DigitalProduct -> base.copy(
				deliveryFormat = product.deliveryFormat,
				turnaroundDays = product.turnaroundDays?.toString().orEmpty(),
			)
		}
	}

	/** Parses every field, collecting all problems so the artist can fix them in one go. */
	fun parse(form: ProductForm): ParsedForm {
		val parser = FieldParser()
		val details = when (form.kind) {
			ProductKind.PHYSICAL -> PhysicalDetails(form.material, form.variants.map { parser.physicalVariant(it) })
			ProductKind.DIGITAL -> DigitalDetails(
				deliveryFormat = form.deliveryFormat,
				turnaroundDays = parser.optional(form.turnaroundDays, FormField.TurnaroundDays, "Solo días enteros", NumberParser::parseCount),
				variants = form.variants.map { parser.digitalVariant(it) },
			)
		}
		return ParsedForm(ProductDraft(form.id, form.name, form.description, form.tagNames, form.status, details, form.stockBaseline), parser.errors)
	}

	private fun toVariantForm(variant: Variant): VariantForm {
		val common = mapOf(
			NAME to variant.name,
			PRICE to MoneyFormatter.formatForInput(variant.price),
			COST to MoneyFormatter.formatForInput(variant.cost),
		)
		val physical = (variant as? PhysicalVariant)?.let {
			mapOf(
				SKU to it.sku.orEmpty(),
				STOCK to it.stock.toString(),
				MIN_STOCK to it.minStock?.toString().orEmpty(),
				WEIGHT to it.weightGrams?.toString().orEmpty(),
				WIDTH to it.widthMm?.let(NumberParser::formatCentimetres).orEmpty(),
				HEIGHT to it.heightMm?.let(NumberParser::formatCentimetres).orEmpty(),
				DEPTH to it.depthMm?.let(NumberParser::formatCentimetres).orEmpty(),
			)
		}.orEmpty()
		return VariantForm(variant.id, common + physical)
	}
}

/** Collects parse errors while building variants. */
private class FieldParser {

	val errors = mutableMapOf<FormField, String>()

	fun physicalVariant(form: VariantForm) = PhysicalVariant(
		id = form.id,
		name = form[NAME],
		price = money(form, PRICE, "Escribe un precio, p. ej. 12,50"),
		cost = money(form, COST, "Escribe el coste (0 si no tiene)"),
		sku = form[SKU].ifBlank { null },
		stock = required(form[STOCK], FormField.Variant(form.id, STOCK), "Solo números enteros", NumberParser::parseCount) ?: 0,
		minStock = optional(form[MIN_STOCK], FormField.Variant(form.id, MIN_STOCK), "Solo números enteros", NumberParser::parseCount),
		weightGrams = positive(form, WEIGHT, "Gramos enteros, mayor que 0", NumberParser::parseCount),
		widthMm = positive(form, WIDTH, CENTIMETRES_ERROR, NumberParser::parseCentimetres),
		heightMm = positive(form, HEIGHT, CENTIMETRES_ERROR, NumberParser::parseCentimetres),
		depthMm = positive(form, DEPTH, CENTIMETRES_ERROR, NumberParser::parseCentimetres),
	)

	fun digitalVariant(form: VariantForm) = DigitalVariant(
		id = form.id,
		name = form[NAME],
		price = money(form, PRICE, "Escribe un precio, p. ej. 40"),
		cost = money(form, COST, "Escribe el coste (0 si no tiene)"),
	)

	fun <T : Any> optional(text: String, field: FormField, message: String, parse: (String) -> T?): T? =
		if (text.isBlank()) null else required(text, field, message, parse)

	private fun <T : Any> required(text: String, field: FormField, message: String, parse: (String) -> T?): T? =
		parse(text).also { if (it == null) errors[field] = message }

	private fun money(form: VariantForm, field: VariantField, message: String): Money =
		required(form[field], FormField.Variant(form.id, field), message, MoneyParser::parse) ?: Money.ZERO

	private fun positive(form: VariantForm, field: VariantField, message: String, parse: (String) -> Int?): Int? =
		optional(form[field], FormField.Variant(form.id, field), message) { text -> parse(text)?.takeIf { it > 0 } }

	private companion object {
		const val CENTIMETRES_ERROR = "En cm, mayor que 0, p. ej. 29,7"
	}
}
