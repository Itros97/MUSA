package org.softwareanvil.musa.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.softwareanvil.musa.domain.logic.EventError
import org.softwareanvil.musa.domain.logic.EventValidator
import org.softwareanvil.musa.domain.model.EventDraft
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.domain.model.EventStatus
import org.softwareanvil.musa.domain.model.ExpenseId
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.fakes.EPOCH

class EventLifecycleUseCasesTest {

	private val eventId = EventId("e1")
	private val variantId = VariantId("v1")

	@Test
	fun `saves a new event as open with trimmed text`() = runTest {
		val bed = EventTestBed(*emptyArray())
		val draft = EventDraft(EventId("e9"), "  Feria  ", " Bilbao ", LocalDate(2026, 6, 1), LocalDate(2026, 6, 2))
		assertEquals(EventResult.Done, SaveEventUseCase(bed.events, EventValidator(), bed.clock)(draft))
		val saved = bed.events.findById(EventId("e9"))!!
		assertEquals("Feria", saved.name)
		assertEquals("Bilbao", saved.location)
		assertEquals(EventStatus.OPEN, saved.status)
	}

	@Test
	fun `editing an event keeps its status and creation time`() = runTest {
		val bed = EventTestBed()
		bed.close(eventId)
		bed.clock.now = EPOCH + 2.hours
		val save = SaveEventUseCase(bed.events, EventValidator(), bed.clock)
		save(EventDraft(eventId, "Otro nombre", "", LocalDate(2026, 4, 18), LocalDate(2026, 4, 18)))
		val saved = bed.events.findById(eventId)!!
		assertEquals(EventStatus.CLOSED, saved.status)
		assertEquals(EPOCH, saved.createdAt)
		assertEquals(EPOCH + 2.hours, saved.updatedAt)
	}

	@Test
	fun `does not save an invalid draft`() = runTest {
		val bed = EventTestBed(*emptyArray())
		val draft = EventDraft(EventId("e9"), "", "", LocalDate(2026, 6, 2), LocalDate(2026, 6, 1))
		val result = SaveEventUseCase(bed.events, EventValidator(), bed.clock)(draft)
		assertEquals(EventResult.Rejected(listOf(EventError.BlankName, EventError.EndBeforeStart)), result)
		assertNull(bed.events.findById(EventId("e9")))
	}

	@Test
	fun `closing returns unsold units and makes the event read-only`() = runTest {
		val bed = EventTestBed()
		bed.moveStock(eventId, variantId, 4)
		bed.recordSale(eventId, variantId, 1, Money(1000))
		assertEquals(EventResult.Done, bed.close(eventId))
		assertEquals(9, bed.catalogueStock())
		val detail = bed.events.observeDetail(eventId).first()!!
		assertEquals(EventStatus.CLOSED, detail.event.status)
		assertEquals(0, detail.lineOf(variantId)?.remaining)
		assertEquals(rejected(EventError.EventClosed), bed.close(eventId))
	}

	@Test
	fun `deletes an event without sales and returns its stock`() = runTest {
		val bed = EventTestBed()
		bed.moveStock(eventId, variantId, 4)
		assertEquals(EventResult.Done, bed.delete(eventId))
		assertEquals(10, bed.catalogueStock())
		assertNull(bed.events.findById(eventId))
	}

	@Test
	fun `refuses to delete an event with sales`() = runTest {
		val bed = EventTestBed()
		bed.moveStock(eventId, variantId, 4)
		bed.recordSale(eventId, variantId, 1, Money(1000))
		assertEquals(rejected(EventError.HasSales), bed.delete(eventId))
		assertEquals(eventId, bed.events.findById(eventId)?.id)
	}

	@Test
	fun `adds and deletes expenses on an open event`() = runTest {
		val bed = EventTestBed()
		assertEquals(EventResult.Done, AddExpenseUseCase(bed.events, bed.clock, bed.ids)(eventId, " Stand ", Money(4000)))
		val expense = bed.events.expenses.value.single()
		assertEquals("Stand", expense.concept)
		assertEquals(EventResult.Done, DeleteExpenseUseCase(bed.events, bed.clock)(eventId, expense.id))
		assertEquals(emptyList(), bed.events.expenses.value)
	}

	@Test
	fun `rejects a blank or negative expense`() = runTest {
		val bed = EventTestBed()
		val result = AddExpenseUseCase(bed.events, bed.clock, bed.ids)(eventId, " ", Money(-1))
		assertEquals(EventResult.Rejected(listOf(EventError.BlankConcept, EventError.NegativeAmount)), result)
	}

	@Test
	fun `rejects expense changes on a closed event`() = runTest {
		val bed = EventTestBed()
		bed.close(eventId)
		assertEquals(rejected(EventError.EventClosed), AddExpenseUseCase(bed.events, bed.clock, bed.ids)(eventId, "Taxi", Money(900)))
		assertEquals(rejected(EventError.EventClosed), DeleteExpenseUseCase(bed.events, bed.clock)(eventId, ExpenseId("x1")))
	}
}
