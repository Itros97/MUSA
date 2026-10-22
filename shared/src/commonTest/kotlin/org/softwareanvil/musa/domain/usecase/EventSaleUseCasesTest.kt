package org.softwareanvil.musa.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.softwareanvil.musa.domain.logic.EventError
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.SaleChannel
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.fakes.EPOCH

class EventSaleUseCasesTest {

	private val eventId = EventId("e1")
	private val variantId = VariantId("v1")

	private suspend fun bedWithFourTaken() = EventTestBed().also { it.moveStock(eventId, variantId, 4) }

	@Test
	fun `records a sale at the given price with the variant cost`() = runTest {
		val bed = bedWithFourTaken()
		assertEquals(EventResult.Done, bed.recordSale(eventId, variantId, 2, Money(800)))
		val sale = bed.sales.sales.value.single()
		val line = sale.lines.single()
		assertEquals(2, line.quantity)
		assertEquals(Money(800), line.unitPrice)
		assertEquals(Money(300), line.unitCost)
		assertEquals("Lámina gato", line.productName)
		assertEquals(SaleChannel.EVENT, sale.channel)
		assertEquals(Money.ZERO, sale.shippingCharged)
		assertEquals(eventId, sale.eventId)
		assertEquals(EPOCH, sale.soldAt)
	}

	@Test
	fun `selling lowers what remains at the event but not the catalogue`() = runTest {
		val bed = bedWithFourTaken()
		bed.recordSale(eventId, variantId, 3, Money(1000))
		assertEquals(1, bed.events.observeDetail(eventId).first()?.lineOf(variantId)?.remaining)
		assertEquals(6, bed.catalogueStock())
	}

	@Test
	fun `rejects selling more than remains`() = runTest {
		val bed = bedWithFourTaken()
		assertEquals(rejected(EventError.ExceedsRemaining(4)), bed.recordSale(eventId, variantId, 5, Money(1000)))
	}

	@Test
	fun `rejects a zero quantity and a negative price together`() = runTest {
		val bed = bedWithFourTaken()
		val result = bed.recordSale(eventId, variantId, 0, Money(-1))
		assertEquals(EventResult.Rejected(listOf(EventError.InvalidQuantity, EventError.NegativePrice)), result)
	}

	@Test
	fun `rejects selling at a closed event`() = runTest {
		val bed = bedWithFourTaken()
		bed.close(eventId)
		assertEquals(rejected(EventError.EventClosed), bed.recordSale(eventId, variantId, 1, Money(1000)))
	}

	@Test
	fun `undoing a sale makes its units remain again`() = runTest {
		val bed = bedWithFourTaken()
		bed.recordSale(eventId, variantId, 3, Money(1000))
		val sale = bed.sales.sales.value.single()
		assertEquals(EventResult.Done, bed.undoSale(sale.id))
		assertEquals(emptyList(), bed.sales.sales.value)
		assertEquals(4, bed.events.observeDetail(eventId).first()?.lineOf(variantId)?.remaining)
	}

	@Test
	fun `keeps sales of a closed event`() = runTest {
		val bed = bedWithFourTaken()
		bed.recordSale(eventId, variantId, 1, Money(1000))
		bed.close(eventId)
		val sale = bed.sales.sales.value.single()
		assertEquals(rejected(EventError.EventClosed), bed.undoSale(sale.id))
		assertEquals(listOf(sale), bed.sales.sales.value)
	}
}
