package org.softwareanvil.musa.ui.events

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.domain.usecase.AddExpenseUseCase
import org.softwareanvil.musa.domain.usecase.DeleteExpenseUseCase
import org.softwareanvil.musa.domain.usecase.EventTestBed
import org.softwareanvil.musa.domain.usecase.ObserveEventDetailUseCase
import org.softwareanvil.musa.domain.usecase.ObserveProductsUseCase
import org.softwareanvil.musa.ui.events.detail.DetailEvent
import org.softwareanvil.musa.ui.events.detail.DetailSheet
import org.softwareanvil.musa.ui.events.detail.EventActions
import org.softwareanvil.musa.ui.events.detail.EventDetailViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class EventDetailViewModelTest {

	private val eventId = EventId("e1")
	private val variantId = VariantId("v1")
	private val bed = EventTestBed()

	@BeforeTest
	fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

	@AfterTest
	fun tearDown() = Dispatchers.resetMain()

	private fun TestScope.viewModel() = EventDetailViewModel(
		eventId = eventId,
		observeDetail = ObserveEventDetailUseCase(bed.events),
		observeProducts = ObserveProductsUseCase(bed.products),
		actions = EventActions(
			moveStock = bed.moveStock,
			returnStock = bed.returnStock,
			recordSale = bed.recordSale,
			undoSale = bed.undoSale,
			addExpense = AddExpenseUseCase(bed.events, bed.clock, bed.ids),
			deleteExpense = DeleteExpenseUseCase(bed.events, bed.clock),
			close = bed.close,
			delete = bed.delete,
		),
	).also { vm -> backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() } }

	@Test
	fun `adding stock closes the sheet and shows the line with its summary`() = runTest {
		val vm = viewModel()
		assertEquals(listOf(10), vm.state.value.options.map { it.stock })
		vm.onSheetOpen(DetailSheet.AddStock)
		vm.onAddStock(variantId, 4)
		val state = vm.state.value
		assertEquals(null, state.sheet)
		assertEquals(4, state.detail?.lineOf(variantId)?.remaining)
		assertEquals(listOf(6), state.options.map { it.stock })
	}

	@Test
	fun `selling at an edited price updates the summary and confirms`() = runTest {
		val vm = viewModel()
		vm.onAddStock(variantId, 4)
		vm.onSheetOpen(DetailSheet.Sell(variantId))
		vm.onSell(variantId, 2, "8,50")
		assertEquals(DetailEvent.Message("Venta registrada · 17 €"), vm.events.first())
		assertEquals(Money(1700), vm.state.value.summary?.gross)
		assertEquals(null, vm.state.value.sheet)
	}

	@Test
	fun `keeps the sheet open with an error when the price is unreadable or too many are sold`() = runTest {
		val vm = viewModel()
		vm.onAddStock(variantId, 1)
		vm.onSheetOpen(DetailSheet.Sell(variantId))
		vm.onSell(variantId, 1, "abc")
		assertEquals("Escribe un precio válido, como 12 o 12,50", vm.state.value.sheetError)
		vm.onSell(variantId, 2, "10")
		assertEquals("En el evento solo quedan 1", vm.state.value.sheetError)
		assertEquals(DetailSheet.Sell(variantId), vm.state.value.sheet)
	}

	@Test
	fun `shows a message when a quick action is rejected`() = runTest {
		val vm = viewModel()
		vm.onReturnOne(variantId)
		assertEquals(DetailEvent.Message("En el evento solo quedan 0"), vm.events.first())
	}

	@Test
	fun `deleting an event without sales closes the screen`() = runTest {
		val vm = viewModel()
		vm.onDeleteConfirm()
		assertEquals(DetailEvent.Close, vm.events.first())
	}
}
