package org.softwareanvil.musa.fakes

import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.softwareanvil.musa.domain.model.Event
import org.softwareanvil.musa.domain.model.EventDetail
import org.softwareanvil.musa.domain.model.EventExpense
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.domain.model.EventStatus
import org.softwareanvil.musa.domain.model.EventStockLine
import org.softwareanvil.musa.domain.model.ExpenseId
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.Sale
import org.softwareanvil.musa.domain.model.SaleId
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.domain.repository.EventRepository
import org.softwareanvil.musa.domain.repository.SaleRepository

class FakeSaleRepository : SaleRepository {

	val sales = MutableStateFlow(emptyList<Sale>())

	override suspend fun record(sale: Sale) = sales.update { it + sale }

	override suspend fun findById(id: SaleId) = sales.value.firstOrNull { it.id == id }

	override suspend fun delete(id: SaleId, at: Instant) = sales.update { all -> all.filterNot { it.id == id } }
}

/** Keeps taken units per event and variant, and moves catalogue stock through [products]. */
class FakeEventRepository(
	private val products: FakeProductRepository,
	private val sales: FakeSaleRepository,
	initial: List<Event> = emptyList(),
) : EventRepository {

	val events = MutableStateFlow(initial.associateBy { it.id })
	val taken = MutableStateFlow(emptyMap<Pair<EventId, VariantId>, Int>())
	val expenses = MutableStateFlow(emptyList<EventExpense>())

	override fun observeAll(): Flow<List<Event>> = events.map { it.values.toList() }

	override fun observeDetail(id: EventId): Flow<EventDetail?> =
		combine(events, taken, sales.sales, expenses) { allEvents, allTaken, allSales, allExpenses ->
			val event = allEvents[id] ?: return@combine null
			val eventSales = allSales.filter { it.eventId == id }
			val lines = allTaken.filter { (key, units) -> key.first == id && units > 0 }
				.map { (key, units) -> lineOf(key.second, units, eventSales) }
			EventDetail(event, lines, eventSales, allExpenses.filter { it.eventId == id })
		}

	override suspend fun findById(id: EventId) = events.value[id]

	override suspend fun save(event: Event) = events.update { it + (event.id to event) }

	override suspend fun moveStock(eventId: EventId, variantId: VariantId, quantity: Int, at: Instant) {
		products.adjustStock(variantId, -quantity, at)
		addTaken(eventId, variantId, quantity)
	}

	override suspend fun returnStock(eventId: EventId, variantId: VariantId, quantity: Int, at: Instant) {
		addTaken(eventId, variantId, -quantity)
		products.adjustStock(variantId, quantity, at)
	}

	override suspend fun close(id: EventId, at: Instant) {
		returnRemaining(id, at)
		events.update { it + (id to it.getValue(id).copy(status = EventStatus.CLOSED, updatedAt = at)) }
	}

	override suspend fun delete(id: EventId, at: Instant) {
		returnRemaining(id, at)
		events.update { it - id }
	}

	override suspend fun addExpense(expense: EventExpense) = expenses.update { it + expense }

	override suspend fun deleteExpense(id: ExpenseId, at: Instant) = expenses.update { all -> all.filterNot { it.id == id } }

	private fun addTaken(eventId: EventId, variantId: VariantId, delta: Int) = taken.update {
		val key = eventId to variantId
		it + (key to (it[key] ?: 0) + delta)
	}

	private suspend fun returnRemaining(id: EventId, at: Instant) {
		val detail = observeDetail(id).first() ?: return
		detail.stock.filter { it.remaining > 0 }.forEach { returnStock(id, it.variantId, it.remaining, at) }
	}

	private fun lineOf(variantId: VariantId, units: Int, eventSales: List<Sale>): EventStockLine {
		val (product, variant) = products.products.value.values
			.filterIsInstance<PhysicalProduct>()
			.flatMap { product -> product.variants.map { product to it } }
			.first { it.second.id == variantId }
		return EventStockLine(
			variantId = variantId,
			productName = product.name,
			variantName = variant.name,
			price = variant.price,
			cost = variant.cost,
			taken = units,
			sold = eventSales.filter { it.variantId == variantId }.sumOf { it.quantity },
		)
	}
}
