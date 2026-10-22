package org.softwareanvil.musa.data.mapper

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import org.softwareanvil.musa.db.Event_expense
import org.softwareanvil.musa.domain.model.Event
import org.softwareanvil.musa.domain.model.EventExpense
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.domain.model.EventStatus
import org.softwareanvil.musa.domain.model.EventStockLine
import org.softwareanvil.musa.domain.model.ExpenseId
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.Sale
import org.softwareanvil.musa.domain.model.SaleChannel
import org.softwareanvil.musa.domain.model.SaleId
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.db.Event as EventRow
import org.softwareanvil.musa.db.Sale as SaleRow

/** Converts event, expense and sale rows into domain models. */
internal object EventMapper {

	/** Builds an event; dates are stored as ISO strings. */
	fun toEvent(row: EventRow) = Event(
		id = EventId(row.id),
		name = row.name,
		location = row.location,
		startDate = LocalDate.parse(row.start_date),
		endDate = LocalDate.parse(row.end_date),
		status = EventStatus.valueOf(row.status),
		createdAt = Instant.fromEpochMilliseconds(row.created_at),
		updatedAt = Instant.fromEpochMilliseconds(row.updated_at),
	)

	/** Mapper for the `selectByEvent` stock query. */
	fun toStockLine(
		variantId: String,
		productName: String,
		variantName: String,
		priceCents: Long,
		costCents: Long,
		taken: Long,
		sold: Long,
	) = EventStockLine(
		variantId = VariantId(variantId),
		productName = productName,
		variantName = variantName,
		price = Money(priceCents),
		cost = Money(costCents),
		taken = taken.toInt(),
		sold = sold.toInt(),
	)

	fun toExpense(row: Event_expense) = EventExpense(
		id = ExpenseId(row.id),
		eventId = EventId(row.event_id),
		concept = row.concept,
		amount = Money(row.amount_cents),
		createdAt = Instant.fromEpochMilliseconds(row.created_at),
	)

	fun toSale(row: SaleRow) = Sale(
		id = SaleId(row.id),
		variantId = VariantId(row.variant_id),
		productName = row.product_name,
		variantName = row.variant_name,
		quantity = row.quantity.toInt(),
		unitPrice = Money(row.unit_price_cents),
		unitCost = Money(row.unit_cost_cents),
		channel = SaleChannel.valueOf(row.channel),
		eventId = row.event_id?.let(::EventId),
		soldAt = Instant.fromEpochMilliseconds(row.sold_at),
	)
}
