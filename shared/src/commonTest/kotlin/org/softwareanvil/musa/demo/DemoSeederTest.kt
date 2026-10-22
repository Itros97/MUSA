package org.softwareanvil.musa.demo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.softwareanvil.musa.domain.logic.EventSummary
import org.softwareanvil.musa.domain.logic.EventValidator
import org.softwareanvil.musa.domain.logic.ProductValidator
import org.softwareanvil.musa.domain.logic.SaleValidator
import org.softwareanvil.musa.domain.model.EventStatus
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.usecase.AddExpenseUseCase
import org.softwareanvil.musa.domain.usecase.CloseEventUseCase
import org.softwareanvil.musa.domain.usecase.MoveStockToEventUseCase
import org.softwareanvil.musa.domain.usecase.RecordEventSaleUseCase
import org.softwareanvil.musa.domain.usecase.RecordSaleUseCase
import org.softwareanvil.musa.domain.usecase.SaveEventUseCase
import org.softwareanvil.musa.domain.usecase.SaveProductUseCase
import org.softwareanvil.musa.fakes.FakeDatabaseMaintenance
import org.softwareanvil.musa.fakes.FakeEventRepository
import org.softwareanvil.musa.fakes.FakeProductRepository
import org.softwareanvil.musa.fakes.FakeSaleRepository
import org.softwareanvil.musa.fakes.FakeTagRepository
import org.softwareanvil.musa.fakes.FixedClock
import org.softwareanvil.musa.fakes.SequentialIds

/** Fake repositories plus a [DemoSeeder] wired to them. */
class DemoTestBed {
	val clock = FixedClock()
	val ids = SequentialIds("demo")
	val products = FakeProductRepository()
	val sales = FakeSaleRepository(products)
	val events = FakeEventRepository(products, sales)
	val maintenance = FakeDatabaseMaintenance(products, events, sales)

	val seeder = DemoSeeder(
		saveProduct = SaveProductUseCase(products, FakeTagRepository(), ProductValidator(), clock, ids),
		saveEvent = SaveEventUseCase(events, EventValidator(), clock),
		moveStock = MoveStockToEventUseCase(events, products, clock),
		recordEventSale = RecordEventSaleUseCase(events, sales, clock, ids),
		recordSale = RecordSaleUseCase(sales, products, SaleValidator(), ids),
		addExpense = AddExpenseUseCase(events, clock, ids),
		closeEvent = CloseEventUseCase(events, clock),
		clock = clock,
		ids = ids,
	)
}

class DemoSeederTest {

	private val bed = DemoTestBed()
	private val seeder = bed.seeder
	private val products = bed.products
	private val events = bed.events

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

	@Test
	fun `seeds four orders outside events through different channels`() = runTest {
		seeder.seed()
		val orders = bed.sales.sales.value.filter { it.eventId == null }
		assertEquals(4, orders.size)
		assertEquals(4, orders.map { it.channel }.distinct().size)
		assertTrue(orders.any { it.shippingCharged.cents > 0 })
	}
}
