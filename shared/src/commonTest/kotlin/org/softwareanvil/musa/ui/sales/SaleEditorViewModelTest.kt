package org.softwareanvil.musa.ui.sales

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import org.softwareanvil.musa.domain.logic.SaleValidator
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.SaleChannel
import org.softwareanvil.musa.domain.usecase.ObserveProductsUseCase
import org.softwareanvil.musa.domain.usecase.RecordSaleUseCase
import org.softwareanvil.musa.fakes.FakeProductRepository
import org.softwareanvil.musa.fakes.FakeSaleRepository
import org.softwareanvil.musa.fakes.FixedClock
import org.softwareanvil.musa.fakes.SequentialIds
import org.softwareanvil.musa.fakes.digitalProduct
import org.softwareanvil.musa.fakes.physicalProduct
import org.softwareanvil.musa.fakes.physicalVariant
import org.softwareanvil.musa.ui.sales.editor.SaleEditorViewModel
import org.softwareanvil.musa.ui.sales.editor.SaleField

@OptIn(ExperimentalCoroutinesApi::class)
class SaleEditorViewModelTest {

	private val products = FakeProductRepository(
		listOf(physicalProduct(variants = listOf(physicalVariant("a4", "A4", price = 1200, cost = 350, stock = 2))), digitalProduct()),
	)
	private val sales = FakeSaleRepository(products)
	private val ids = SequentialIds("line")

	@BeforeTest
	fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

	@AfterTest
	fun tearDown() = Dispatchers.resetMain()

	private fun viewModel() = SaleEditorViewModel(
		observeProducts = ObserveProductsUseCase(products),
		recordSale = RecordSaleUseCase(sales, products, SaleValidator(), ids),
		clock = FixedClock(),
		timeZone = TimeZone.UTC,
		ids = ids,
	)

	private fun SaleEditorViewModel.pick(label: String) = onOptionPick(state.value.options.first { it.label == label })

	@Test
	fun `offers physical variants with stock and digital ones, sorted by name`() = runTest {
		assertEquals(listOf("Comisión" to null, "Lámina gato · A4" to 2), viewModel().state.value.options.map { it.label to it.stock })
	}

	@Test
	fun `a picked line starts at one unit and the catalogue price, and totals follow`() = runTest {
		val vm = viewModel()
		vm.pick("Lámina gato · A4")
		vm.onQuantityStep(vm.state.value.form!!.lines.single().id, 1)
		vm.onShippingChargedChange("4,50")
		vm.onShippingCostChange("3,80")
		val preview = vm.state.value.preview!!
		assertEquals(Money(2400 + 450), preview.total)
		assertEquals(Money(700 + 380), preview.cost)
		assertEquals("12", vm.state.value.form!!.lines.single().price)
	}

	@Test
	fun `quantity cannot go past the stock`() = runTest {
		val vm = viewModel()
		vm.pick("Lámina gato · A4")
		val id = vm.state.value.form!!.lines.single().id
		repeat(5) { vm.onQuantityStep(id, 1) }
		assertEquals(2, vm.state.value.form!!.lines.single().quantity)
	}

	@Test
	fun `saving records the sale, takes the stock and closes`() = runTest {
		val vm = viewModel()
		vm.onChannelSelect(SaleChannel.SOCIAL)
		vm.onCustomerChange("@cliente")
		vm.pick("Lámina gato · A4")
		vm.onSave()
		vm.closed.first()
		val stored = sales.sales.value.single()
		assertEquals(SaleChannel.SOCIAL, stored.channel)
		assertEquals("@cliente", stored.customer)
		assertEquals(1, (products.findById(ProductId("p1")) as PhysicalProduct).totalStock)
	}

	@Test
	fun `shows errors next to their fields and does not save`() = runTest {
		val vm = viewModel()
		vm.onSave()
		assertEquals(setOf(SaleField.Lines), vm.state.value.errors.keys)
		vm.pick("Lámina gato · A4")
		val id = vm.state.value.form!!.lines.single().id
		vm.onPriceChange(id, "abc")
		vm.onShippingCostChange("x")
		vm.onSave()
		assertEquals(setOf(SaleField.LinePrice(id), SaleField.Shipping), vm.state.value.errors.keys)
		assertEquals(emptyList(), sales.sales.value)
	}

	@Test
	fun `stock errors land on every line of that variant`() = runTest {
		val vm = viewModel()
		vm.pick("Lámina gato · A4")
		vm.pick("Lámina gato · A4")
		val lines = vm.state.value.form!!.lines
		lines.forEach { vm.onQuantityStep(it.id, 1) }
		vm.onSave()
		assertEquals(lines.map { SaleField.LineQuantity(it.id) }.toSet(), vm.state.value.errors.keys)
		assertEquals("En el catálogo solo quedan 2", vm.state.value.errors.values.first())
	}

	@Test
	fun `closing with lines asks before discarding`() = runTest {
		val vm = viewModel()
		vm.pick("Comisión")
		vm.onClose()
		assertEquals(true, vm.state.value.isConfirmingDiscard)
		vm.onDiscardConfirm()
		vm.closed.first()
	}
}
