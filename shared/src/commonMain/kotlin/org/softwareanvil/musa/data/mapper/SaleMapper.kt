package org.softwareanvil.musa.data.mapper

import kotlin.time.Instant
import org.softwareanvil.musa.db.Sale_line
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.Sale
import org.softwareanvil.musa.domain.model.SaleChannel
import org.softwareanvil.musa.domain.model.SaleId
import org.softwareanvil.musa.domain.model.SaleLine
import org.softwareanvil.musa.domain.model.SaleLineId
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.db.Sale as SaleRow

/** Converts sale and sale_line rows into domain sales. */
internal object SaleMapper {

	/** Builds a sale from its header row and its lines, already in position order. */
	fun toSale(row: SaleRow, lines: List<Sale_line>) = Sale(
		id = SaleId(row.id),
		channel = SaleChannel.valueOf(row.channel),
		eventId = row.event_id?.let(::EventId),
		customer = row.customer,
		note = row.note,
		lines = lines.map(::toLine),
		shippingCharged = Money(row.shipping_charged_cents),
		shippingCost = Money(row.shipping_cost_cents),
		soldAt = Instant.fromEpochMilliseconds(row.sold_at),
	)

	private fun toLine(row: Sale_line) = SaleLine(
		id = SaleLineId(row.id),
		variantId = VariantId(row.variant_id),
		productName = row.product_name,
		variantName = row.variant_name,
		quantity = row.quantity.toInt(),
		unitPrice = Money(row.unit_price_cents),
		unitCost = Money(row.unit_cost_cents),
	)
}
