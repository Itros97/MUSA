package org.softwareanvil.musa.domain.model

import kotlin.time.Instant

/** Where a sale happened; [EVENT] sales are only recorded from their event. */
enum class SaleChannel { EVENT, ONLINE_SHOP, SOCIAL, MARKETPLACE, IN_PERSON, OTHER }

/** An order: one or more lines sold together through a channel, with what shipping was charged and cost. */
data class Sale(
	val id: SaleId,
	val channel: SaleChannel,
	val eventId: EventId?,
	val customer: String,
	val note: String,
	val lines: List<SaleLine>,
	val shippingCharged: Money,
	val shippingCost: Money,
	val soldAt: Instant,
) {

	val units: Int get() = lines.sumOf { it.quantity }

	/** What the customer paid: lines plus shipping charged. */
	val total: Money get() = lines.sumOfMoney { it.total } + shippingCharged

	/** What the sale cost the artist: goods plus shipping paid. */
	val cost: Money get() = lines.sumOfMoney { it.totalCost } + shippingCost

	/** Total minus cost; negative when the sale lost money. */
	val margin: Money get() = total - cost
}

/** Units of one variant in a sale; names, price and cost are copied so later product edits do not change it. */
data class SaleLine(
	val id: SaleLineId,
	val variantId: VariantId,
	val productName: String,
	val variantName: String,
	val quantity: Int,
	val unitPrice: Money,
	val unitCost: Money,
) {

	val total: Money get() = unitPrice * quantity

	val totalCost: Money get() = unitCost * quantity
}

/** What the sale editor submits; names and costs are resolved from the catalogue on save. */
data class SaleDraft(
	val channel: SaleChannel,
	val customer: String,
	val note: String,
	val lines: List<SaleLineDraft>,
	val shippingCharged: Money,
	val shippingCost: Money,
	val soldAt: Instant,
)

/** One line of the sale editor. */
data class SaleLineDraft(val id: SaleLineId, val variantId: VariantId, val quantity: Int, val unitPrice: Money)
