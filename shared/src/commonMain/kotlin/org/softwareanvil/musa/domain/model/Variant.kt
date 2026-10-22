package org.softwareanvil.musa.domain.model

/** A sellable version of a product (size, format, level of detail...) with its own price and cost. */
sealed interface Variant {
	val id: VariantId
	val name: String
	val price: Money
	val cost: Money
}

/** A version of a physical product, with stock and shipping data. */
data class PhysicalVariant(
	override val id: VariantId,
	override val name: String,
	override val price: Money,
	override val cost: Money,
	val sku: String?,
	val stock: Int,
	val minStock: Int?,
	val weightGrams: Int?,
	val widthMm: Int?,
	val heightMm: Int?,
	val depthMm: Int?,
) : Variant {

	/** True when the stock has reached the minimum the artist wants to keep, or is sold out. */
	val isLowStock: Boolean get() = stock == 0 || minStock?.let { stock <= it } == true
}

/** A version of a digital product, such as a commission level. */
data class DigitalVariant(
	override val id: VariantId,
	override val name: String,
	override val price: Money,
	override val cost: Money,
) : Variant
