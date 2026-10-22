package org.softwareanvil.musa.fakes

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import org.softwareanvil.musa.domain.model.DigitalProduct
import org.softwareanvil.musa.domain.model.DigitalVariant
import org.softwareanvil.musa.domain.model.Event
import org.softwareanvil.musa.domain.model.EventExpense
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.domain.model.EventStatus
import org.softwareanvil.musa.domain.model.ExpenseId
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.PhysicalVariant
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductStatus
import org.softwareanvil.musa.domain.model.Sale
import org.softwareanvil.musa.domain.model.SaleChannel
import org.softwareanvil.musa.domain.model.SaleId
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

fun event(
	id: String = "e1",
	name: String = "Mercado de primavera",
	status: EventStatus = EventStatus.OPEN,
	startDate: LocalDate = LocalDate(2026, 4, 18),
	endDate: LocalDate = LocalDate(2026, 4, 19),
) = Event(
	id = EventId(id),
	name = name,
	location = "Pabellón 2",
	startDate = startDate,
	endDate = endDate,
	status = status,
	createdAt = EPOCH,
	updatedAt = EPOCH,
)

fun sale(id: String = "s1", variantId: String = "v1", quantity: Int = 1, price: Long = 1000, cost: Long = 300) = Sale(
	id = SaleId(id),
	variantId = VariantId(variantId),
	productName = "Lámina gato",
	variantName = "",
	quantity = quantity,
	unitPrice = Money(price),
	unitCost = Money(cost),
	channel = SaleChannel.EVENT,
	eventId = EventId("e1"),
	soldAt = EPOCH,
)

fun expense(id: String = "x1", concept: String = "Stand", amount: Long = 5000) =
	EventExpense(ExpenseId(id), EventId("e1"), concept, Money(amount), EPOCH)
