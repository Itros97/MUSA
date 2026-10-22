package org.softwareanvil.musa.domain.model

import kotlin.time.Instant

/** Anything the artist sells. Always has at least one variant carrying price and cost. */
sealed interface Product {
	val id: ProductId
	val name: String
	val description: String
	val tags: List<Tag>
	val status: ProductStatus
	val createdAt: Instant
	val updatedAt: Instant
	val variants: List<Variant>
	val kind: ProductKind
}

/** Stock the artist keeps and ships: prints, stickers, merch... */
data class PhysicalProduct(
	override val id: ProductId,
	override val name: String,
	override val description: String,
	override val tags: List<Tag>,
	override val status: ProductStatus,
	override val createdAt: Instant,
	override val updatedAt: Instant,
	override val variants: List<PhysicalVariant>,
	val material: String,
) : Product {

	override val kind get() = ProductKind.PHYSICAL

	/** Units in stock across all variants. */
	val totalStock: Int get() = variants.sumOf { it.stock }

	/** True when any variant needs restocking. */
	val isLowStock: Boolean get() = variants.any { it.isLowStock }
}

/** Digital work made to order, such as commissions. */
data class DigitalProduct(
	override val id: ProductId,
	override val name: String,
	override val description: String,
	override val tags: List<Tag>,
	override val status: ProductStatus,
	override val createdAt: Instant,
	override val updatedAt: Instant,
	override val variants: List<DigitalVariant>,
	val deliveryFormat: String,
	val turnaroundDays: Int?,
) : Product {

	override val kind get() = ProductKind.DIGITAL
}

/** Cheapest price among the variants, shown as "from X" in the catalogue. */
val Product.minPrice: Money get() = variants.minOf { it.price }

/** True when variants have different prices. */
val Product.hasPriceRange: Boolean get() = variants.map { it.price }.distinct().size > 1
