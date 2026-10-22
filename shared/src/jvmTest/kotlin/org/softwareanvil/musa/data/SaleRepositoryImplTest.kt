package org.softwareanvil.musa.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.softwareanvil.musa.data.repository.ProductRepositoryImpl
import org.softwareanvil.musa.data.repository.SaleRepositoryImpl
import org.softwareanvil.musa.db.MusaDatabase
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.fakes.EPOCH
import org.softwareanvil.musa.fakes.digitalProduct
import org.softwareanvil.musa.fakes.physicalProduct
import org.softwareanvil.musa.fakes.physicalVariant
import org.softwareanvil.musa.fakes.sale
import org.softwareanvil.musa.fakes.saleLine

class SaleRepositoryImplTest {

	private val database = MusaDatabase(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also(MusaDatabase.Schema::create))
	private val products = ProductRepositoryImpl(database, Dispatchers.Unconfined)
	private val sales = SaleRepositoryImpl(database, Dispatchers.Unconfined)
	private val a4 = VariantId("a4")
	private val later = EPOCH + 1.hours

	// An online order: two prints and a commission, with shipping.
	private val order = sale("o1", eventId = null, shippingCharged = 450, shippingCost = 380).copy(
		lines = listOf(saleLine("l1", "a4", quantity = 2), saleLine("l2", "d1", quantity = 1, price = 4000, cost = 0)),
	)

	private suspend fun givenCatalogue() {
		products.save(physicalProduct(variants = listOf(physicalVariant("a4", "A4", stock = 5))))
		products.save(digitalProduct())
	}

	@Test
	fun `round trips a sale with its lines`() = runTest {
		givenCatalogue()
		sales.record(order)
		assertEquals(order, sales.findById(order.id))
		assertEquals(listOf(order), sales.observeAll().first())
	}

	@Test
	fun `a sale outside events takes physical units and deleting gives them back`() = runTest {
		givenCatalogue()
		sales.record(order)
		assertEquals(3, products.findStock(a4))
		sales.delete(order.id, later)
		assertEquals(5, products.findStock(a4))
		assertNull(sales.findById(order.id))
		assertEquals(emptyList(), sales.observeAll().first())
	}

	@Test
	fun `event sales leave the catalogue alone`() = runTest {
		givenCatalogue()
		val atStand = sale("ev", variantId = "a4", quantity = 2, eventId = "e1")
		sales.record(atStand)
		sales.delete(atStand.id, later)
		assertEquals(5, products.findStock(a4))
	}

	@Test
	fun `lists sales newest first`() = runTest {
		givenCatalogue()
		val older = sale("old", variantId = "d1", eventId = null)
		val newer = sale("new", variantId = "d1", eventId = null, soldAt = later)
		sales.record(older)
		sales.record(newer)
		assertEquals(listOf("new", "old"), sales.observeAll().first().map { it.id.value })
	}

	@Test
	fun `finds the product owning a variant`() = runTest {
		givenCatalogue()
		assertEquals("Lámina gato", products.findByVariant(a4)?.name)
		assertNull(products.findByVariant(VariantId("nope")))
	}
}
