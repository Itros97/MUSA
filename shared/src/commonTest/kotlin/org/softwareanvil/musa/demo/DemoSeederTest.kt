package org.softwareanvil.musa.demo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.softwareanvil.musa.domain.logic.EventSummary
import org.softwareanvil.musa.domain.logic.EventValidator
import org.softwareanvil.musa.domain.logic.ProductValidator
import org.softwareanvil.musa.domain.model.EventStatus
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.usecase.AddExpenseUseCase
import org.softwareanvil.musa.domain.usecase.CloseEventUseCase
import org.softwareanvil.musa.domain.usecase.MoveStockToEventUseCase
import org.softwareanvil.musa.domain.usecase.RecordEventSaleUseCase
import org.softwareanvil.musa.domain.usecase.SaveEventUseCase
import org.softwareanvil.musa.domain.usecase.SaveProductUseCase
import org.softwareanvil.musa.fakes.FakeEventRepository
import org.softwareanvil.musa.fakes.FakeProductRepository
import org.softwareanvil.musa.fakes.FakeSaleRepository
import org.softwareanvil.musa.fakes.FakeTagRepository
import org.softwareanvil.musa.fakes.FixedClock
import org.softwareanvil.musa.fakes.SequentialIds

class DemoSeederTest {

	private val clock = FixedClock()
	private val ids = SequentialIds("demo")
	private val products = FakeProductRepository()
	private val sales = FakeSaleRepository()
	private val events = FakeEventRepository(products, sales)

	private val seeder = DemoSeeder(
		saveProduct = SaveProductUseCase(products, FakeTagRepository(), ProductValidator(), clock, ids),
		saveEvent = SaveEventUseCase(events, EventValidator(), clock),
		moveStock = MoveStockToEventUseCase(events, products, clock),
		recordSale = RecordEventSaleUseCase(events, sales, clock, ids),
		addExpense = AddExpenseUseCase(events, clock, ids),
		closeEvent = CloseEventUseCase(events, clock),
		clock = clock,
		ids = ids,
	)

	@Test
	fun `seeds a catalogue with low and sold-out stock`() = runTest {
		seeder.seed()
		val catalogue = products.observeAll().first()
		assertEquals(10, catalogue.size)
		val physical = catalogue.filterIsInstance<PhysicalProduct>()
		assertTrue(physical.any { it.totalStock == 0 })
		assertTrue(physical.any { it.isLowStock && it.totalStock > 0 })
	}

	@Test
	fun `seeds a profitable closed event, an open one with sales and an empty upcoming one`() = runTest {
		seeder.seed()
		val details = events.observeAll().first().map { events.observeDetail(it.id).first()!! }
		val (closed, open) = details.filter { it.sales.isNotEmpty() }.partition { it.event.status == EventStatus.CLOSED }
		assertTrue(EventSummary.of(closed.single()).profit.cents > 0)
		assertEquals(4, open.single().sales.size)
		assertTrue(open.single().stock.all { it.remaining > 0 })
		assertEquals(1, details.count { it.sales.isEmpty() && it.stock.isEmpty() })
	}
}
