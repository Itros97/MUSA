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
import org.softwareanvil.musa.data.repository.TagRepositoryImpl
import org.softwareanvil.musa.db.MusaDatabase
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductStatus
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.fakes.EPOCH
import org.softwareanvil.musa.fakes.digitalProduct
import org.softwareanvil.musa.fakes.digitalVariant
import org.softwareanvil.musa.fakes.physicalProduct
import org.softwareanvil.musa.fakes.physicalVariant
import org.softwareanvil.musa.fakes.tag

class ProductRepositoryImplTest {

	private val database = MusaDatabase(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also(MusaDatabase.Schema::create))
	private val tags = TagRepositoryImpl(database, Dispatchers.Unconfined)
	private val repository = ProductRepositoryImpl(database, Dispatchers.Unconfined)
	private val pokemon = tag("Pokémon")

	private val print = physicalProduct(
		tags = listOf(pokemon),
		variants = listOf(
			physicalVariant("a4", "A4", sku = "LAM-A4", stock = 3, minStock = 1).copy(weightGrams = 40, widthMm = 210),
			physicalVariant("a3", "A3", price = 1800, stock = 2),
		),
	)

	@Test
	fun `round trips a physical product with variants and tags`() = runTest {
		tags.insert(pokemon, EPOCH)
		repository.save(print)
		assertEquals(print, repository.findById(print.id))
	}

	@Test
	fun `round trips a digital product`() = runTest {
		val commission = digitalProduct(variants = listOf(digitalVariant("bust", "Busto"), digitalVariant("full", "Cuerpo entero", 9000)))
		repository.save(commission)
		assertEquals(commission, repository.findById(commission.id))
	}

	@Test
	fun `observes every product`() = runTest {
		tags.insert(pokemon, EPOCH)
		repository.save(print)
		repository.save(digitalProduct())
		assertEquals(setOf(print, digitalProduct()), repository.observeAll().first().toSet())
	}

	@Test
	fun `drops variants removed from the product`() = runTest {
		repository.save(print)
		val edited = print.copy(tags = emptyList(), variants = print.variants.take(1), updatedAt = EPOCH + 1.hours)
		repository.save(edited)
		assertEquals(edited, repository.findById(print.id))
	}

	@Test
	fun `adjusts stock without going below zero`() = runTest {
		repository.save(print)
		repository.adjustStock(VariantId("a4"), delta = -10, at = EPOCH + 1.hours)
		val stored = repository.findById(print.id) as PhysicalProduct
		assertEquals(listOf(0, 2), stored.variants.map { it.stock })
		assertEquals(EPOCH + 1.hours, stored.updatedAt)
	}

	@Test
	fun `finds the product owning a sku ignoring case`() = runTest {
		repository.save(print)
		assertEquals("Lámina gato", repository.findSkuOwnerName("lam-a4", excluding = ProductId("other")))
		assertNull(repository.findSkuOwnerName("LAM-A4", excluding = print.id))
	}

	@Test
	fun `updates the status`() = runTest {
		repository.save(print)
		repository.updateStatus(print.id, ProductStatus.ARCHIVED, EPOCH + 1.hours)
		assertEquals(ProductStatus.ARCHIVED, repository.findById(print.id)?.status)
	}

	@Test
	fun `hides deleted products and frees their sku`() = runTest {
		repository.save(print)
		repository.delete(print.id, EPOCH + 1.hours)
		assertNull(repository.findById(print.id))
		assertEquals(emptyList(), repository.observeAll().first())
		assertNull(repository.findSkuOwnerName("LAM-A4", excluding = ProductId("other")))
	}

	@Test
	fun `reuses tags by normalized key`() = runTest {
		tags.insert(pokemon, EPOCH)
		assertEquals(pokemon, tags.findByKey("pokemon"))
	}
}
