package org.softwareanvil.musa.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.softwareanvil.musa.domain.logic.EventError
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.domain.model.EventStatus
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.fakes.event

class EventStockUseCasesTest {

	private val eventId = EventId("e1")
	private val variantId = VariantId("v1")

	@Test
	fun `moves units from the catalogue to the event`() = runTest {
		val bed = EventTestBed()
		assertEquals(EventResult.Done, bed.moveStock(eventId, variantId, 4))
		assertEquals(6, bed.catalogueStock())
		assertEquals(4, bed.events.observeDetail(eventId).first()?.lineOf(variantId)?.remaining)
	}

	@Test
	fun `rejects taking more than the catalogue has`() = runTest {
		val bed = EventTestBed()
		assertEquals(rejected(EventError.NotEnoughStock(10)), bed.moveStock(eventId, variantId, 11))
		assertEquals(10, bed.catalogueStock())
	}

	@Test
	fun `rejects a zero or negative quantity`() = runTest {
		val bed = EventTestBed()
		assertEquals(rejected(EventError.InvalidQuantity), bed.moveStock(eventId, variantId, 0))
		assertEquals(rejected(EventError.InvalidQuantity), bed.returnStock(eventId, variantId, -1))
	}

	@Test
	fun `rejects moving stock to a closed event`() = runTest {
		val bed = EventTestBed(event(status = EventStatus.CLOSED))
		assertEquals(rejected(EventError.EventClosed), bed.moveStock(eventId, variantId, 1))
	}

	@Test
	fun `returns unsold units to the catalogue`() = runTest {
		val bed = EventTestBed()
		bed.moveStock(eventId, variantId, 4)
		assertEquals(EventResult.Done, bed.returnStock(eventId, variantId, 3))
		assertEquals(9, bed.catalogueStock())
		assertEquals(1, bed.events.observeDetail(eventId).first()?.lineOf(variantId)?.remaining)
	}

	@Test
	fun `rejects returning more than remains at the event`() = runTest {
		val bed = EventTestBed()
		bed.moveStock(eventId, variantId, 4)
		bed.recordSale(eventId, variantId, 2, Money(1000))
		assertEquals(rejected(EventError.ExceedsRemaining(2)), bed.returnStock(eventId, variantId, 3))
	}
}
