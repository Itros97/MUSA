package org.softwareanvil.musa.ui.products

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.softwareanvil.musa.domain.logic.ProductValidator
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductKind
import org.softwareanvil.musa.domain.usecase.ArchiveProductUseCase
import org.softwareanvil.musa.domain.usecase.DeleteProductUseCase
import org.softwareanvil.musa.domain.usecase.DuplicateProductUseCase
import org.softwareanvil.musa.domain.usecase.GetProductUseCase
import org.softwareanvil.musa.domain.usecase.ObserveTagsUseCase
import org.softwareanvil.musa.domain.usecase.SaveProductUseCase
import org.softwareanvil.musa.fakes.FakeProductRepository
import org.softwareanvil.musa.fakes.FakeTagRepository
import org.softwareanvil.musa.fakes.FixedClock
import org.softwareanvil.musa.fakes.SequentialIds
import org.softwareanvil.musa.fakes.physicalProduct
import org.softwareanvil.musa.ui.products.edit.EditDialog
import org.softwareanvil.musa.ui.products.edit.EditEvent
import org.softwareanvil.musa.ui.products.edit.EditTarget
import org.softwareanvil.musa.ui.products.edit.FormField
import org.softwareanvil.musa.ui.products.edit.ProductEditActions
import org.softwareanvil.musa.ui.products.edit.ProductEditViewModel
import org.softwareanvil.musa.ui.products.edit.VariantField

@OptIn(ExperimentalCoroutinesApi::class)
class ProductEditViewModelTest {

	private val products = FakeProductRepository(listOf(physicalProduct()))
	private val tags = FakeTagRepository()
	private val clock = FixedClock()
	private val ids = SequentialIds()

	@BeforeTest
	fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

	@AfterTest
	fun tearDown() = Dispatchers.resetMain()

	private fun viewModel(target: EditTarget = EditTarget.New(ProductKind.PHYSICAL)) = ProductEditViewModel(
		target = target,
		actions = ProductEditActions(
			GetProductUseCase(products),
			SaveProductUseCase(products, tags, ProductValidator(), clock, ids),
			DuplicateProductUseCase(products, clock, ids),
			ArchiveProductUseCase(products, clock),
			DeleteProductUseCase(products, clock),
			ProductValidator(),
		),
		observeTags = ObserveTagsUseCase(tags),
		ids = ids,
	)

	@Test
	fun `starts a new product with one variant and no changes`() {
		val state = viewModel().state.value
		assertEquals(1, state.form?.variants?.size)
		assertEquals(false, state.isDirty)
	}

	@Test
	fun `asks before discarding unsaved changes`() {
		val vm = viewModel()
		vm.onNameChange("Pegatina")
		vm.onBack()
		assertEquals(EditDialog.DISCARD, vm.state.value.dialog)
	}

	@Test
	fun `closes right away when nothing changed`() = runTest {
		val vm = viewModel()
		vm.onBack()
		assertEquals(EditEvent.Close, vm.events.first())
	}

	@Test
	fun `shows unreadable fields and rule errors together and clears them while typing`() = runTest {
		val vm = viewModel()
		val variantId = vm.state.value.form!!.variants.single().id
		vm.onSave()
		assertTrue(FormField.Name in vm.state.value.errors)
		assertTrue(FormField.Variant(variantId, VariantField.PRICE) in vm.state.value.errors)
		vm.onNameChange("Pegatina")
		assertTrue(FormField.Name !in vm.state.value.errors)
	}

	@Test
	fun `saves a valid product and closes`() = runTest {
		val vm = viewModel()
		val variantId = vm.state.value.form!!.variants.single().id
		vm.onNameChange("Pegatina")
		vm.onVariantChange(variantId, VariantField.PRICE, "3")
		vm.onVariantChange(variantId, VariantField.COST, "0,40")
		vm.onSave()
		assertEquals(EditEvent.Close, vm.events.first())
		assertNotNull(products.findById(vm.state.value.form!!.id))
	}

	@Test
	fun `loads an existing product`() =
		assertEquals("Lámina gato", viewModel(EditTarget.Existing(ProductId("p1"))).state.value.form?.name)

	@Test
	fun `closes when the product does not exist`() = runTest {
		assertEquals(EditEvent.Close, viewModel(EditTarget.Existing(ProductId("missing"))).events.first())
	}

	@Test
	fun `refuses to duplicate with unsaved changes`() = runTest {
		val vm = viewModel(EditTarget.Existing(ProductId("p1")))
		vm.onNameChange("Otro nombre")
		vm.onDuplicate()
		assertEquals(EditEvent.Message("Guarda los cambios antes de duplicar"), vm.events.first())
	}
}
