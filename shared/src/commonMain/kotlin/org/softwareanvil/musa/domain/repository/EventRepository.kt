package org.softwareanvil.musa.domain.repository

import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import org.softwareanvil.musa.domain.model.Event
import org.softwareanvil.musa.domain.model.EventDetail
import org.softwareanvil.musa.domain.model.EventExpense
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.domain.model.ExpenseId
import org.softwareanvil.musa.domain.model.VariantId

/** Storage for events, the stock taken to them and their expenses; deleted events are never returned. */
interface EventRepository {

	/** Emits every event, open ones first and then the most recent first. */
	fun observeAll(): Flow<List<Event>>

	/** Emits the event with its stock, sales and expenses, or null if it does not exist. */
	fun observeDetail(id: EventId): Flow<EventDetail?>

	/** Returns the event, or null if it does not exist or was deleted. */
	suspend fun findById(id: EventId): Event?

	/** Inserts or replaces the event row. */
	suspend fun save(event: Event)

	/** Moves [quantity] units of a variant from the catalogue to the event in one transaction. */
	suspend fun moveStock(eventId: EventId, variantId: VariantId, quantity: Int, at: Instant)

	/** Moves [quantity] unsold units of a variant back to the catalogue in one transaction. */
	suspend fun returnStock(eventId: EventId, variantId: VariantId, quantity: Int, at: Instant)

	/** Returns every unsold unit to the catalogue and marks the event closed, in one transaction. */
	suspend fun close(id: EventId, at: Instant)

	/** Returns every unsold unit to the catalogue and soft-deletes the event, in one transaction. */
	suspend fun delete(id: EventId, at: Instant)

	/** Stores a new expense. */
	suspend fun addExpense(expense: EventExpense)

	/** Soft-deletes an expense. */
	suspend fun deleteExpense(id: ExpenseId, at: Instant)
}
