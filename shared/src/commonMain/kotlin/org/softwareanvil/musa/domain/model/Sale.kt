package org.softwareanvil.musa.domain.model

import kotlin.time.Instant

/** Where a sale happened; more channels come with the sales module. */
enum class SaleChannel { EVENT }

/** Units of one variant sold together; names, price and cost are copied so later edits do not change it. */
data class Sale(
	val id: SaleId,
	val variantId: VariantId,
	val productName: String,
	val variantName: String,
	val quantity: Int,
	val unitPrice: Money,
	val unitCost: Money,
	val channel: SaleChannel,
	val eventId: EventId?,
	val soldAt: Instant,
) {

	val total: Money get() = unitPrice * quantity

	val totalCost: Money get() = unitCost * quantity
}
