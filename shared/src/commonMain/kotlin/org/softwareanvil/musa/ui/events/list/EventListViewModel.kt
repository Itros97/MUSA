package org.softwareanvil.musa.ui.events.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.softwareanvil.musa.domain.model.Event
import org.softwareanvil.musa.domain.usecase.ObserveEventsUseCase

/** Everything the event list draws. */
data class EventListState(
	val isLoading: Boolean = true,
	val events: List<Event> = emptyList(),
	val isCreating: Boolean = false,
)

/** Drives the event list and the sheet that creates a new event. */
class EventListViewModel(observeEvents: ObserveEventsUseCase) : ViewModel() {

	private val isCreating = MutableStateFlow(false)

	val state: StateFlow<EventListState> = combine(observeEvents(), isCreating) { events, creating ->
		EventListState(isLoading = false, events = events, isCreating = creating)
	}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EventListState())

	fun onCreateClick() = isCreating.update { true }

	fun onCreateDismiss() = isCreating.update { false }
}
