package org.softwareanvil.musa.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.softwareanvil.musa.data.mapper.EventMapper
import org.softwareanvil.musa.db.MusaDatabase
import org.softwareanvil.musa.domain.model.Event
import org.softwareanvil.musa.domain.model.EventDetail
import org.softwareanvil.musa.domain.model.EventExpense
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.domain.model.EventStatus
import org.softwareanvil.musa.domain.model.ExpenseId
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.domain.repository.EventRepository

/** SQLDelight-backed [EventRepository]; stock moves touch the catalogue in the same transaction. */
class EventRepositoryImpl(
	private val database: MusaDatabase,
	private val dispatcher: CoroutineDispatcher,
) : EventRepository {

	private val stock = EventStockWriter(database)
	private val sales = SaleRowStore(database)

	// region read

	override fun observeAll(): Flow<List<Event>> = database.eventQueries.selectAll()
		.asFlow()
		.mapToList(dispatcher)
		.map { rows -> rows.map(EventMapper::toEvent) }

	override fun observeDetail(id: EventId): Flow<EventDetail?> = combine(
		database.eventQueries.selectById(id.value).asFlow().mapToOneOrNull(dispatcher),
		database.eventStockQueries.selectByEvent(id.value, EventMapper::toStockLine).asFlow().mapToList(dispatcher),
		database.saleQueries.selectByEvent(id.value).asFlow().mapToList(dispatcher).map { rows -> withContext(dispatcher) { sales.withLines(rows) } },
		database.eventExpenseQueries.selectByEvent(id.value).asFlow().mapToList(dispatcher),
	) { event, lines, eventSales, expenses ->
		event?.let { EventDetail(EventMapper.toEvent(it), lines, eventSales, expenses.map(EventMapper::toExpense)) }
	}

	override suspend fun findById(id: EventId): Event? = withContext(dispatcher) {
		database.eventQueries.selectById(id.value).executeAsOneOrNull()?.let(EventMapper::toEvent)
	}

	// endregion

	// region write

	override suspend fun save(event: Event) = withContext(dispatcher) {
		database.eventQueries.upsert(
			id = event.id.value,
			name = event.name,
			location = event.location,
			start_date = event.startDate.toString(),
			end_date = event.endDate.toString(),
			status = event.status.name,
			created_at = event.createdAt.toEpochMilliseconds(),
			updated_at = event.updatedAt.toEpochMilliseconds(),
		)
		Unit
	}

	override suspend fun moveStock(eventId: EventId, variantId: VariantId, quantity: Int, at: Instant) = withContext(dispatcher) {
		database.transaction { stock.move(eventId.value, variantId.value, quantity.toLong(), at.toEpochMilliseconds()) }
	}

	override suspend fun returnStock(eventId: EventId, variantId: VariantId, quantity: Int, at: Instant) = withContext(dispatcher) {
		database.transaction { stock.giveBack(eventId.value, variantId.value, quantity.toLong(), at.toEpochMilliseconds()) }
	}

	override suspend fun close(id: EventId, at: Instant) = withContext(dispatcher) {
		database.transaction {
			stock.giveBackRemaining(id.value, at.toEpochMilliseconds())
			database.eventQueries.updateStatus(EventStatus.CLOSED.name, at.toEpochMilliseconds(), id.value)
		}
	}

	override suspend fun delete(id: EventId, at: Instant) = withContext(dispatcher) {
		database.transaction {
			stock.giveBackRemaining(id.value, at.toEpochMilliseconds())
			database.eventQueries.softDelete(at = at.toEpochMilliseconds(), id = id.value)
		}
	}

	override suspend fun addExpense(expense: EventExpense) = withContext(dispatcher) {
		val at = expense.createdAt.toEpochMilliseconds()
		database.eventExpenseQueries.insert(expense.id.value, expense.eventId.value, expense.concept, expense.amount.cents, at, at)
		Unit
	}

	override suspend fun deleteExpense(id: ExpenseId, at: Instant) = withContext(dispatcher) {
		database.eventExpenseQueries.softDelete(at = at.toEpochMilliseconds(), id = id.value)
		Unit
	}

	// endregion
}
