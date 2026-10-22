package org.softwareanvil.musa.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.softwareanvil.musa.domain.logic.SaleError
import org.softwareanvil.musa.domain.logic.SaleValidator
import org.softwareanvil.musa.domain.model.DigitalProduct
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.SaleChannel
import org.softwareanvil.musa.domain.model.SaleDraft
import org.softwareanvil.musa.domain.model.SaleLineDraft
import org.softwareanvil.musa.domain.model.SaleLineId
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.fakes.EPOCH
import org.softwareanvil.musa.fakes.FakeProductRepository
import org.softwareanvil.musa.fakes.FakeSaleRepository
import org.softwareanvil.musa.fakes.FixedClock
import org.softwareanvil.musa.fakes.SequentialIds
import org.softwareanvil.musa.fakes.digitalProduct
import org.softwareanvil.musa.fakes.physicalProduct
import org.softwareanvil.musa.fakes.physicalVariant
import org.softwareanvil.musa.fakes.sale

class SaleUseCasesTest {

	private val products = FakeProductRepository(
		listOf(physicalProduct(variants = listOf(physicalVariant("a4", "A4", stock = 5, cost = 350))), digitalProduct()),
	)
	private val sales = FakeSaleRepository(products)
	private val record = RecordSaleUseCase(sales, products, SaleValidator(), SequentialIds("sale"))
	private val delete = DeleteSaleUseCase(sales, FixedClock())

	private fun line(id: String, variant: String, quantity: Int, price: Long = 1200) =
		SaleLineDraft(SaleLineId(id), VariantId(variant), quantity, Money(price))

	private fun draft(vararg lines: SaleLineDraft) =
		SaleDraft(SaleChannel.ONLINE_SHOP, " @cliente ", " Etsy ", lines.toList(), Money(450), Money(380), EPOCH)

	private suspend fun printStock() = (products.findById(ProductId("p1")) as PhysicalProduct).totalStock

	@Test
	fun `records a sale taking physical units and copying names and cost`() = runTest {
		val sale = assertIs<SaleResult.Done>(record(draft(line("l1", "a4", 2, price = 1000)))).sale
		assertEquals(3, printStock())
		val stored = sales.sales.value.single()
		assertEquals(sale, stored)
		assertEquals("@cliente", stored.customer)
		assertEquals("Etsy", stored.note)
		assertEquals(listOf("Lámina gato" to "A4"), stored.lines.map { it.productName to it.variantName })
		assertEquals(Money(350), stored.lines.single().unitCost)
		assertEquals(Money(1000), stored.lines.single().unitPrice)
	}

	@Test
	fun `adds up lines of the same variant against the stock`() = runTest {
		val result = record(draft(line("l1", "a4", 3), line("l2", "a4", 3)))
		assertEquals(SaleResult.Rejected(listOf(SaleError.NotEnoughStock(VariantId("a4"), 5))), result)
		assertEquals(5, printStock())
		assertEquals(emptyList(), sales.sales.value)
	}

	@Test
	fun `digital lines have no stock to check`() = runTest {
		val result = record(draft(line("l1", "d1", 7, price = 4000)))
		assertIs<SaleResult.Done>(result)
		assertIs<DigitalProduct>(products.findById(ProductId("c1")))
		assertEquals(5, printStock())
	}

	@Test
	fun `does not record an invalid draft`() = runTest {
		assertEquals(SaleResult.Rejected(listOf(SaleError.NoLines)), record(draft()))
	}

	@Test
	fun `deleting a sale gives its physical units back`() = runTest {
		val sale = assertIs<SaleResult.Done>(record(draft(line("l1", "a4", 2)))).sale
		assertIs<SaleResult.Done>(delete(sale.id))
		assertEquals(5, printStock())
		assertEquals(emptyList(), sales.sales.value)
	}

	@Test
	fun `event sales are not deleted from here`() = runTest {
		sales.record(sale("ev", variantId = "a4", eventId = "e1"))
		assertEquals(SaleResult.Rejected(listOf(SaleError.ManagedByEvent)), delete(sale("ev").id))
		assertEquals(1, sales.sales.value.size)
	}
}
