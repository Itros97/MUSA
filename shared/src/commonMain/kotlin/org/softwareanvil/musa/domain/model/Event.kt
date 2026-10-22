package org.softwareanvil.musa.domain.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate

/** Whether an event still takes stock and sales, or is finished and read-only. */
enum class EventStatus { OPEN, CLOSED }

/** A fair or market the artist sells at. */
data class Event(
	val id: EventId,
	val name: String,
	val location: String,
	val startDate: LocalDate,
	val endDate: LocalDate,
	val status: EventStatus,
	val createdAt: Instant,
	val updatedAt: Instant,
) {

	val isOpen: Boolean get() = status == EventStatus.OPEN
}

/** What the editor submits to create or update an event; status and timestamps are resolved on save. */
data class EventDraft(
	val id: EventId,
	val name: String,
	val location: String,
	val startDate: LocalDate,
	val endDate: LocalDate,
)

/** An event with the stock taken to it, the sales made there and what it cost to attend. */
data class EventDetail(
	val event: Event,
	val stock: List<EventStockLine>,
	val sales: List<Sale>,
	val expenses: List<EventExpense>,
) {

	/** The stock line of [variantId], or null if that variant was not taken. */
	fun lineOf(variantId: VariantId): EventStockLine? = stock.firstOrNull { it.variantId == variantId }
}

/** A physical variant taken to an event: units moved from the catalogue and units sold there. */
data class EventStockLine(
	val variantId: VariantId,
	val productName: String,
	val variantName: String,
	val price: Money,
	val cost: Money,
	val taken: Int,
	val sold: Int,
) {

	/** Units still on the stand. */
	val remaining: Int get() = taken - sold
}

/** Money spent to attend an event: stand, travel, lodging... */
data class EventExpense(
	val id: ExpenseId,
	val eventId: EventId,
	val concept: String,
	val amount: Money,
	val createdAt: Instant,
)
