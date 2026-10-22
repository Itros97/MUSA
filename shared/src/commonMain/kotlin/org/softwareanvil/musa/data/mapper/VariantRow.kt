package org.softwareanvil.musa.data.mapper

/** One row of the variant queries: product_variant joined with its optional physical_variant. */
internal data class VariantRow(
	val id: String,
	val productId: String,
	val name: String,
	val priceCents: Long,
	val costCents: Long,
	val position: Long,
	val updatedAt: Long,
	val deletedAt: Long?,
	val sku: String?,
	val stock: Long?,
	val minStock: Long?,
	val weightGrams: Long?,
	val widthMm: Long?,
	val heightMm: Long?,
	val depthMm: Long?,
)
