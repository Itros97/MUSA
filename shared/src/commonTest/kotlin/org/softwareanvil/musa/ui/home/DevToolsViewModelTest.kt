package org.softwareanvil.musa.ui.home

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.softwareanvil.musa.demo.DemoTestBed
import org.softwareanvil.musa.demo.FillWithDemoDataUseCase
import org.softwareanvil.musa.domain.repository.DatabaseMaintenance
import org.softwareanvil.musa.domain.usecase.WipeDatabaseUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class DevToolsViewModelTest {

	private val bed = DemoTestBed()

	@BeforeTest
	fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

	@AfterTest
	fun tearDown() = Dispatchers.resetMain()

	private fun viewModel(maintenance: DatabaseMaintenance = bed.maintenance) =
		DevToolsViewModel(FillWithDemoDataUseCase(maintenance, bed.seeder), WipeDatabaseUseCase(maintenance))

	@Test
	fun `fills the database and confirms`() = runTest {
		val vm = viewModel()
		vm.onFillClick()
		assertEquals("Datos de prueba cargados", vm.messages.first())
		assertEquals(10, bed.products.products.value.size)
	}

	@Test
	fun `warns instead of filling a database with data`() = runTest {
		val vm = viewModel()
		vm.onFillClick()
		vm.messages.first()
		vm.onFillClick()
		assertEquals("Vacía la base de datos antes de rellenarla", vm.messages.first())
	}

	@Test
	fun `wipes only after confirming`() = runTest {
		val vm = viewModel()
		vm.onFillClick()
		vm.messages.first()
		vm.onWipeClick()
		assertTrue(vm.state.value.isConfirmingWipe)
		vm.onWipeDismiss()
		assertEquals(10, bed.products.products.value.size)
		vm.onWipeClick()
		vm.onWipeConfirm()
		assertEquals("Base de datos vacía", vm.messages.first())
		assertEquals(emptyMap(), bed.products.products.value)
		assertEquals(false, vm.state.value.isConfirmingWipe)
	}

	@Test
	fun `ignores clicks while an action runs`() = runTest {
		val gate = CompletableDeferred<Unit>()
		var wipes = 0
		val slow = object : DatabaseMaintenance {
			override suspend fun isEmpty() = false
			override suspend fun wipeAll() {
				wipes++
				gate.await()
			}
		}
		val vm = viewModel(slow)
		vm.onWipeConfirm()
		assertTrue(vm.state.value.isBusy)
		vm.onWipeConfirm()
		vm.onFillClick()
		gate.complete(Unit)
		assertEquals(1, wipes)
		assertEquals(false, vm.state.value.isBusy)
		assertEquals("Base de datos vacía", vm.messages.first())
	}
}
