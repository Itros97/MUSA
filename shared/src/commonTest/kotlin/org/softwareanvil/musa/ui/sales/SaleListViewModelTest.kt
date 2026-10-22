package org.softwareanvil.musa.ui.sales

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import org.softwareanvil.musa.domain.logic.SalesPeriod
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.SaleChannel
import org.softwareanvil.musa.domain.usecase.DeleteSaleUseCase
import org.softwareanvil.musa.domain.usecase.ObserveSalesUseCase
import org.softwareanvil.musa.fakes.EPOCH
import org.softwareanvil.musa.fakes.FakeProductRepository
import org.softwareanvil.musa.fakes.FakeSaleRepository
import org.softwareanvil.musa.fakes.FixedClock
import org.softwareanvil.musa.fakes.physicalProduct
import org.softwareanvil.musa.fakes.physicalVariant
import org.softwareanvil.musa.fakes.sale
import org.softwareanvil.musa.ui.sales.list.SaleListViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class SaleListViewModelTest {

	private val products = FakeProductRepository(listOf(physicalProduct(variants = listOf(physicalVariant(stock = 5)))))
	private val sales = FakeSaleRepository(products)
	private val clock = FixedClock(EPOCH)

	@BeforeTest
	fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

	@AfterTest
	fun tearDown() = Dispatchers.resetMain()

	private suspend fun TestScope.viewModel(): SaleListViewModel {
		sales.record(sale("online", eventId = null, quantity = 2))
		sales.record(sale("social", eventId = null, channel = SaleChannel.SOCIAL))
		sales.record(sale("stand", eventId = "e1"))
		sales.record(sale("old", eventId = null, soldAt = EPOCH - 400.days))
		val vm = SaleListViewModel(ObserveSalesUseCase(sales), DeleteSaleUseCase(sales, clock), clock, TimeZone.UTC)
		backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
		return vm
	}

	@Test
	fun `shows this month with its summary, event sales included`() = runTest {
		val state = viewModel().state.value
		assertEquals(setOf("online", "social", "stand"), state.sales.map { it.id.value }.toSet())
		assertEquals(4, state.summary.units)
		assertEquals(Money(4000), state.summary.revenue)
	}

	@Test
	fun `filters by channel and toggles it off again`() = runTest {
		val vm = viewModel()
		vm.onChannelToggle(SaleChannel.SOCIAL)
		assertEquals(listOf("social"), vm.state.value.sales.map { it.id.value })
		vm.onChannelToggle(SaleChannel.SOCIAL)
		assertEquals(3, vm.state.value.sales.size)
	}

	@Test
	fun `all periods include older sales`() = runTest {
		val vm = viewModel()
		vm.onPeriodSelect(SalesPeriod.ALL)
		assertEquals(4, vm.state.value.sales.size)
	}

	@Test
	fun `deleting an opened sale returns its stock and confirms`() = runTest {
		val vm = viewModel()
		vm.onSaleOpen(sale("online").id)
		vm.onDeleteRequest()
		vm.onDeleteConfirm()
		assertEquals("Venta eliminada; el stock ha vuelto al catálogo", vm.messages.first())
		assertEquals(null, vm.state.value.opened)
		// Three online sales of one unit or more took 4 units; the deleted one gives back 2.
		assertEquals(3, (products.findById(ProductId("p1")) as PhysicalProduct).totalStock)
	}

	@Test
	fun `event sales cannot be deleted from the list`() = runTest {
		val vm = viewModel()
		vm.onSaleOpen(sale("stand").id)
		vm.onDeleteConfirm()
		assertEquals("Esta venta es de un evento: deshazla desde el evento", vm.messages.first())
	}
}
