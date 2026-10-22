package org.softwareanvil.musa.ui.products

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductKind
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.domain.usecase.AdjustStockUseCase
import org.softwareanvil.musa.domain.usecase.ObserveProductsUseCase
import org.softwareanvil.musa.domain.usecase.ObserveTagsUseCase
import org.softwareanvil.musa.fakes.FakeProductRepository
import org.softwareanvil.musa.fakes.FakeTagRepository
import org.softwareanvil.musa.fakes.FixedClock
import org.softwareanvil.musa.fakes.digitalProduct
import org.softwareanvil.musa.fakes.physicalProduct
import org.softwareanvil.musa.fakes.physicalVariant
import org.softwareanvil.musa.ui.products.list.ProductListViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class ProductListViewModelTest {

	private val sticker = physicalProduct(id = "sticker", name = "Pegatina", variants = listOf(physicalVariant(stock = 1, minStock = 2)))
	private val repository = FakeProductRepository(listOf(sticker, digitalProduct()))

	@BeforeTest
	fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

	@AfterTest
	fun tearDown() = Dispatchers.resetMain()

	private fun viewModel() = ProductListViewModel(
		ObserveProductsUseCase(repository),
		ObserveTagsUseCase(FakeTagRepository()),
		AdjustStockUseCase(repository, FixedClock()),
	)

	@Test
	fun `filters by search text`() = runTest {
		val vm = viewModel()
		backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
		vm.onSearchChange("pega")
		assertEquals(listOf("Pegatina"), vm.state.value.products.map { it.name })
	}

	@Test
	fun `toggling the same kind twice clears the filter`() = runTest {
		val vm = viewModel()
		backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
		vm.onKindToggle(ProductKind.DIGITAL)
		vm.onKindToggle(ProductKind.DIGITAL)
		assertEquals(2, vm.state.value.products.size)
	}

	@Test
	fun `keeps the stock sheet open when the product leaves the filter`() = runTest {
		val vm = viewModel()
		backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
		vm.onLowStockToggle()
		vm.onStockClick(ProductId("sticker"))
		vm.onStockStep(VariantId("v1"), delta = 5)
		assertEquals(emptyList(), vm.state.value.products)
		assertEquals(6, vm.state.value.stockSheet?.totalStock)
	}
}
