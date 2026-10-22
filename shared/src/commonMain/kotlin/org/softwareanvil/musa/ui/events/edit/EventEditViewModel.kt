package org.softwareanvil.musa.ui.events.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Clock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.softwareanvil.musa.domain.logic.EventError
import org.softwareanvil.musa.domain.logic.IdGenerator
import org.softwareanvil.musa.domain.model.EventDraft
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.domain.usecase.EventResult
import org.softwareanvil.musa.domain.usecase.GetEventUseCase
import org.softwareanvil.musa.domain.usecase.SaveEventUseCase
import org.softwareanvil.musa.ui.events.message

/** What the event editor opens: a blank event or an existing one. */
sealed interface EventEditTarget {
	data object New : EventEditTarget
	data class Existing(val id: EventId) : EventEditTarget
}

/** Fields of the event editor that can show an error. */
enum class EventField { NAME, END_DATE }

/** Everything the event editor draws; [draft] is null while loading. */
data class EventEditState(
	val draft: EventDraft? = null,
	val isNew: Boolean = true,
	val errors: Map<EventField, String> = emptyMap(),
	val isSaving: Boolean = false,
)

/** Drives the sheet that creates or edits an event; emits the id once saved. */
class EventEditViewModel(
	target: EventEditTarget,
	private val getEvent: GetEventUseCase,
	private val saveEvent: SaveEventUseCase,
	private val clock: Clock,
	private val ids: IdGenerator,
) : ViewModel() {

	private val _state = MutableStateFlow(EventEditState(isNew = target is EventEditTarget.New))
	val state: StateFlow<EventEditState> = _state.asStateFlow()

	private val _saved = Channel<EventId>(Channel.BUFFERED)
	val saved: Flow<EventId> = _saved.receiveAsFlow()

	init {
		viewModelScope.launch { _state.update { it.copy(draft = load(target)) } }
	}

	private suspend fun load(target: EventEditTarget): EventDraft? = when (target) {
		is EventEditTarget.New -> clock.todayIn(TimeZone.currentSystemDefault())
			.let { today -> EventDraft(EventId(ids.next()), name = "", location = "", startDate = today, endDate = today) }
		is EventEditTarget.Existing -> getEvent(target.id)?.let { EventDraft(it.id, it.name, it.location, it.startDate, it.endDate) }
	}

	fun onNameChange(value: String) = updateDraft(EventField.NAME) { it.copy(name = value) }

	fun onLocationChange(value: String) = updateDraft { it.copy(location = value) }

	/** Moving the start past the end drags the end along, so the range stays valid. */
	fun onStartDateChange(date: LocalDate) = updateDraft(EventField.END_DATE) { it.copy(startDate = date, endDate = maxOf(date, it.endDate)) }

	fun onEndDateChange(date: LocalDate) = updateDraft(EventField.END_DATE) { it.copy(endDate = date) }

	fun onSave() {
		val draft = _state.value.draft ?: return
		if (_state.value.isSaving) return
		viewModelScope.launch {
			_state.update { it.copy(isSaving = true) }
			when (val result = saveEvent(draft)) {
				EventResult.Done -> _saved.send(draft.id)
				is EventResult.Rejected -> _state.update { state ->
					state.copy(errors = result.errors.mapNotNull { error -> error.field()?.let { it to error.message() } }.toMap())
				}
			}
			_state.update { it.copy(isSaving = false) }
		}
	}

	private fun updateDraft(clearing: EventField? = null, transform: (EventDraft) -> EventDraft) = _state.update { state ->
		val draft = state.draft ?: return@update state
		state.copy(draft = transform(draft), errors = clearing?.let { state.errors - it } ?: state.errors)
	}
}

private fun EventError.field(): EventField? = when (this) {
	EventError.BlankName -> EventField.NAME
	EventError.EndBeforeStart -> EventField.END_DATE
	else -> null
}
