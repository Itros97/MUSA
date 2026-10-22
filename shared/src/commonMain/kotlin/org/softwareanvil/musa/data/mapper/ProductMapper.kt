package org.softwareanvil.musa.data.mapper

import kotlin.time.Instant
import org.softwareanvil.musa.db.Digital_product
import org.softwareanvil.musa.db.Physical_product
import org.softwareanvil.musa.domain.model.DigitalProduct
import org.softwareanvil.musa.domain.model.DigitalVariant
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.PhysicalVariant
import org.softwareanvil.musa.domain.model.Product
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductKind
import org.softwareanvil.musa.domain.model.ProductStatus
import org.softwareanvil.musa.domain.model.Tag
import org.softwareanvil.musa.domain.model.TagColor
import org.softwareanvil.musa.domain.model.TagId
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.db.Product as ProductRow

/** Converts database rows into domain products and tags. */
internal object ProductMapper {

	/** Builds the product of the right kind; fails loud if its kind-specific row is missing. */
	fun toDomain(
		row: ProductRow,
		variants: List<VariantRow>,
		tags: List<Tag>,
		physical: Physical_product?,
		digital: Digital_product?,
	): Product = when (ProductKind.valueOf(row.kind)) {
		ProductKind.PHYSICAL -> PhysicalProduct(
			id = ProductId(row.id),
			name = row.name,
			description = row.description,
			tags = tags,
			status = ProductStatus.valueOf(row.status),
			createdAt = Instant.fromEpochMilliseconds(row.created_at),
			updatedAt = Instant.fromEpochMilliseconds(row.updated_at),
			variants = variants.map(::toPhysicalVariant),
			material = checkNotNull(physical) { "Physical product ${row.id} has no physical_product row" }.material,
		)
		ProductKind.DIGITAL -> {
			val details = checkNotNull(digital) { "Digital product ${row.id} has no digital_product row" }
			DigitalProduct(
				id = ProductId(row.id),
				name = row.name,
				description = row.description,
				tags = tags,
				status = ProductStatus.valueOf(row.status),
				createdAt = Instant.fromEpochMilliseconds(row.created_at),
				updatedAt = Instant.fromEpochMilliseconds(row.updated_at),
				variants = variants.map(::toDigitalVariant),
				deliveryFormat = details.delivery_format,
				turnaroundDays = details.turnaround_days?.toInt(),
			)
		}
	}

	/** Maps a tag row; colour names are the [TagColor] entry names. */
	fun toTag(id: String, name: String, color: String) = Tag(TagId(id), name, TagColor.valueOf(color))

	private fun toPhysicalVariant(row: VariantRow) = PhysicalVariant(
		id = VariantId(row.id),
		name = row.name,
		price = Money(row.priceCents),
		cost = Money(row.costCents),
		sku = row.sku,
		stock = checkNotNull(row.stock) { "Physical variant ${row.id} has no physical_variant row" }.toInt(),
		minStock = row.minStock?.toInt(),
		weightGrams = row.weightGrams?.toInt(),
		widthMm = row.widthMm?.toInt(),
		heightMm = row.heightMm?.toInt(),
		depthMm = row.depthMm?.toInt(),
	)

	private fun toDigitalVariant(row: VariantRow) =
		DigitalVariant(VariantId(row.id), row.name, Money(row.priceCents), Money(row.costCents))
}
