package org.softwareanvil.musa.data

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.softwareanvil.musa.data.repository.DatabaseMaintenanceImpl
import org.softwareanvil.musa.data.repository.EventRepositoryImpl
import org.softwareanvil.musa.data.repository.ProductRepositoryImpl
import org.softwareanvil.musa.data.repository.SaleRepositoryImpl
import org.softwareanvil.musa.data.repository.TagRepositoryImpl
import org.softwareanvil.musa.db.MusaDatabase
import org.softwareanvil.musa.demo.DemoSeeder
import org.softwareanvil.musa.domain.logic.EventValidator
import org.softwareanvil.musa.domain.logic.ProductValidator
import org.softwareanvil.musa.domain.logic.UuidGenerator
import org.softwareanvil.musa.domain.usecase.AddExpenseUseCase
import org.softwareanvil.musa.domain.usecase.CloseEventUseCase
import org.softwareanvil.musa.domain.usecase.MoveStockToEventUseCase
import org.softwareanvil.musa.domain.usecase.RecordEventSaleUseCase
import org.softwareanvil.musa.domain.usecase.SaveEventUseCase
import org.softwareanvil.musa.domain.usecase.SaveProductUseCase

class DatabaseMaintenanceImplTest {

	private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also(MusaDatabase.Schema::create)
	private val database = MusaDatabase(driver)
	private val tables = listOf(
		"product", "physical_product", "digital_product", "product_variant", "physical_variant", "tag", "product_tag",
		"event", "event_stock", "event_expense", "sale", "sale_line",
	)
	private val products = ProductRepositoryImpl(database, Dispatchers.Unconfined)
	private val tags = TagRepositoryImpl(database, Dispatchers.Unconfined)
	private val events = EventRepositoryImpl(database, Dispatchers.Unconfined)
	private val sales = SaleRepositoryImpl(database, Dispatchers.Unconfined)
	private val maintenance = DatabaseMaintenanceImpl(database, Dispatchers.Unconfined)
	private val clock = Clock.System
	private val ids = UuidGenerator()

	private val seeder = DemoSeeder(
		saveProduct = SaveProductUseCase(products, tags, ProductValidator(), clock, ids),
		saveEvent = SaveEventUseCase(events, EventValidator(), clock),
		moveStock = MoveStockToEventUseCase(events, products, clock),
		recordSale = RecordEventSaleUseCase(events, sales, clock, ids),
		addExpense = AddExpenseUseCase(events, clock, ids),
		closeEvent = CloseEventUseCase(events, clock),
		clock = clock,
		ids = ids,
	)

	private fun rowCount(table: String): Long = driver.executeQuery(
		identifier = null,
		sql = "SELECT count(*) FROM $table",
		mapper = { cursor -> cursor.next(); QueryResult.Value(cursor.getLong(0)!!) },
		parameters = 0,
	).value

	@Test
	fun `the demo data puts rows in every table`() = runTest {
		seeder.seed()
		assertTrue(tables.all { rowCount(it) > 0 }, "Every table should have rows: ${tables.associateWith(::rowCount)}")
	}

	@Test
	fun `a new database is empty`() = runTest { assertTrue(maintenance.isEmpty()) }

	@Test
	fun `the demo data fills every table on real SQLite`() = runTest {
		seeder.seed()
		assertFalse(maintenance.isEmpty())
		assertEquals(10, products.observeAll().first().size)
		assertEquals(3, events.observeAll().first().size)
	}

	@Test
	fun `wiping clears every table and the demo can be loaded again`() = runTest {
		seeder.seed()
		maintenance.wipeAll()
		assertTrue(maintenance.isEmpty())
		assertEquals(tables.associateWith { 0L }, tables.associateWith(::rowCount))
		maintenance.wipeAll()
		seeder.seed()
		assertEquals(10, products.observeAll().first().size)
	}
}
