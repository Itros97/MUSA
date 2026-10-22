package org.softwareanvil.musa.fakes

import kotlin.time.Instant
import org.softwareanvil.musa.domain.model.DigitalProduct
import org.softwareanvil.musa.domain.model.DigitalVariant
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.PhysicalVariant
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductStatus
import org.softwareanvil.musa.domain.model.Tag
import org.softwareanvil.musa.domain.model.TagColor
import org.softwareanvil.musa.domain.model.TagId
import org.softwareanvil.musa.domain.model.VariantId

val EPOCH: Instant = Instant.fromEpochMilliseconds(1_700_000_000_000)

fun tag(name: String) = Tag(TagId("tag-$name"), name, TagColor.TEAL)

fun physicalVariant(
	id: String = "v1",
	name: String = "",
	price: Long = 1000,
	cost: Long = 300,
	sku: String? = null,
	stock: Int = 5,
	minStock: Int? = null,
) = PhysicalVariant(
	id = VariantId(id),
	name = name,
	price = Money(price),
	cost = Money(cost),
	sku = sku,
	stock = stock,
	minStock = minStock,
	weightGrams = null,
	widthMm = null,
	heightMm = null,
	depthMm = null,
)

fun digitalVariant(id: String = "d1", name: String = "", price: Long = 4000, cost: Long = 0) =
	DigitalVariant(VariantId(id), name, Money(price), Money(cost))

fun physicalProduct(
	id: String = "p1",
	name: String = "Lámina gato",
	description: String = "",
	tags: List<Tag> = emptyList(),
	status: ProductStatus = ProductStatus.ACTIVE,
	variants: List<PhysicalVariant> = listOf(physicalVariant()),
) = PhysicalProduct(
	id = ProductId(id),
	name = name,
	description = description,
	tags = tags,
	status = status,
	createdAt = EPOCH,
	updatedAt = EPOCH,
	variants = variants,
	material = "Papel",
)

fun digitalProduct(
	id: String = "c1",
	name: String = "Comisión",
	tags: List<Tag> = emptyList(),
	status: ProductStatus = ProductStatus.ACTIVE,
	variants: List<DigitalVariant> = listOf(digitalVariant()),
) = DigitalProduct(
	id = ProductId(id),
	name = name,
	description = "",
	tags = tags,
	status = status,
	createdAt = EPOCH,
	updatedAt = EPOCH,
	variants = variants,
	deliveryFormat = "PNG",
	turnaroundDays = 14,
)
