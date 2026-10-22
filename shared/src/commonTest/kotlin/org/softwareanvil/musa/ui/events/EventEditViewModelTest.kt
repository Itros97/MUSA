package org.softwareanvil.musa.ui.events

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
import kotlinx.datetime.LocalDate
import org.softwareanvil.musa.domain.logic.EventValidator
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.domain.usecase.EventTestBed
import org.softwareanvil.musa.domain.usecase.GetEventUseCase
import org.softwareanvil.musa.domain.usecase.SaveEventUseCase
import org.softwareanvil.musa.ui.events.edit.EventEditTarget
import org.softwareanvil.musa.ui.events.edit.EventEditViewModel
import org.softwareanvil.musa.ui.events.edit.EventField

@OptIn(ExperimentalCoroutinesApi::class)
class EventEditViewModelTest {

	private val bed = EventTestBed()

	@BeforeTest
	fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

	@AfterTest
	fun tearDown() = Dispatchers.resetMain()

	private fun viewModel(target: EventEditTarget = EventEditTarget.New) = EventEditViewModel(
		target = target,
		getEvent = GetEventUseCase(bed.events),
		saveEvent = SaveEventUseCase(bed.events, EventValidator(), bed.clock),
		clock = bed.clock,
		ids = bed.ids,
	)

	@Test
	fun `shows the name error and clears it when typing`() = runTest {
		val vm = viewModel()
		vm.onSave()
		assertEquals(setOf(EventField.NAME), vm.state.value.errors.keys)
		vm.onNameChange("Feria")
		assertEquals(emptyMap(), vm.state.value.errors)
	}

	@Test
	fun `saves a new event and emits its id`() = runTest {
		val vm = viewModel()
		vm.onNameChange("Feria del cómic")
		vm.onSave()
		val id = vm.saved.first()
		assertEquals("Feria del cómic", bed.events.findById(id)?.name)
	}

	@Test
	fun `moving the start past the end drags the end along`() = runTest {
		val vm = viewModel(EventEditTarget.Existing(EventId("e1")))
		vm.onStartDateChange(LocalDate(2026, 5, 3))
		assertEquals(LocalDate(2026, 5, 3), vm.state.value.draft?.endDate)
	}

	@Test
	fun `loads an existing event`() = runTest {
		assertEquals("Mercado de primavera", viewModel(EventEditTarget.Existing(EventId("e1"))).state.value.draft?.name)
	}
}
