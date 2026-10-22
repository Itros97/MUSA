package org.softwareanvil.musa.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.softwareanvil.musa.data.repository.EventRepositoryImpl
import org.softwareanvil.musa.data.repository.ProductRepositoryImpl
import org.softwareanvil.musa.data.repository.SaleRepositoryImpl
import org.softwareanvil.musa.db.MusaDatabase
import org.softwareanvil.musa.domain.model.EventStatus
import org.softwareanvil.musa.domain.model.EventStockLine
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.fakes.EPOCH
import org.softwareanvil.musa.fakes.event
import org.softwareanvil.musa.fakes.expense
import org.softwareanvil.musa.fakes.physicalProduct
import org.softwareanvil.musa.fakes.physicalVariant
import org.softwareanvil.musa.fakes.sale

class EventRepositoryImplTest {

	private val database = MusaDatabase(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also(MusaDatabase.Schema::create))
	private val products = ProductRepositoryImpl(database, Dispatchers.Unconfined)
	private val events = EventRepositoryImpl(database, Dispatchers.Unconfined)
	private val sales = SaleRepositoryImpl(database, Dispatchers.Unconfined)
	private val fair = event()
	private val variantId = VariantId("v1")
	private val later = EPOCH + 1.hours

	private suspend fun givenFourTaken() {
		products.save(physicalProduct(variants = listOf(physicalVariant(stock = 10))))
		events.save(fair)
		events.moveStock(fair.id, variantId, 4, later)
	}

	private suspend fun detail() = events.observeDetail(fair.id).first()!!

	@Test
	fun `round trips an event`() = runTest {
		events.save(fair)
		assertEquals(fair, events.findById(fair.id))
		assertEquals(listOf(fair), events.observeAll().first())
	}

	@Test
	fun `lists open events before closed ones, newest first`() = runTest {
		val old = event("old", startDate = LocalDate(2026, 3, 1), endDate = LocalDate(2026, 3, 1))
		val closed = event("closed", status = EventStatus.CLOSED, startDate = fair.endDate, endDate = fair.endDate)
		listOf(old, closed, fair).forEach { events.save(it) }
		assertEquals(listOf(fair, old, closed), events.observeAll().first())
	}

	@Test
	fun `moving stock lowers the catalogue and fills the event line`() = runTest {
		givenFourTaken()
		events.moveStock(fair.id, variantId, 2, later)
		assertEquals(4, products.findStock(variantId))
		assertEquals(listOf(EventStockLine(variantId, "Lámina gato", "", Money(1000), Money(300), taken = 6, sold = 0)), detail().stock)
	}

	@Test
	fun `counts sold units from the sales of the event`() = runTest {
		givenFourTaken()
		sales.record(sale("s1", quantity = 3))
		assertEquals(1, detail().lineOf(variantId)?.remaining)
		sales.delete(sale().id, later)
		assertEquals(4, detail().lineOf(variantId)?.remaining)
		assertNull(sales.findById(sale().id))
	}

	@Test
	fun `returning every unit hides the line`() = runTest {
		givenFourTaken()
		events.returnStock(fair.id, variantId, 4, later)
		assertEquals(10, products.findStock(variantId))
		assertEquals(emptyList(), detail().stock)
	}

	@Test
	fun `closing returns the remaining units and keeps the sold ones`() = runTest {
		givenFourTaken()
		sales.record(sale("s1", quantity = 1))
		events.close(fair.id, later)
		assertEquals(9, products.findStock(variantId))
		assertEquals(EventStatus.CLOSED, detail().event.status)
		assertEquals(0, detail().lineOf(variantId)?.remaining)
	}

	@Test
	fun `a failed stock return rolls back the event line`() = runTest {
		events.save(fair)
		// A line whose variant is gone from the catalogue: the line is updated first, then the catalogue write fails.
		database.eventStockQueries.insert(fair.id.value, "ghost", 3, 0)
		assertFailsWith<IllegalStateException> { events.returnStock(fair.id, VariantId("ghost"), 1, later) }
		assertEquals(3, database.eventStockQueries.selectTaken(fair.id.value, "ghost").executeAsOne())
	}

	@Test
	fun `deleting returns the stock and hides the event`() = runTest {
		givenFourTaken()
		events.delete(fair.id, later)
		assertEquals(10, products.findStock(variantId))
		assertNull(events.findById(fair.id))
		assertEquals(emptyList(), events.observeAll().first())
	}

	@Test
	fun `stores and deletes expenses`() = runTest {
		events.save(fair)
		events.addExpense(expense())
		assertEquals(listOf(expense()), detail().expenses)
		events.deleteExpense(expense().id, later)
		assertEquals(emptyList(), detail().expenses)
	}

	@Test
	fun `round trips a sale with its copied names and prices`() = runTest {
		sales.record(sale())
		assertEquals(sale(), sales.findById(sale().id))
	}
}
