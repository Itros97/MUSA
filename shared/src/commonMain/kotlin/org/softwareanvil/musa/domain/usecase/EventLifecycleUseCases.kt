package org.softwareanvil.musa.domain.usecase

import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import org.softwareanvil.musa.domain.logic.EventError
import org.softwareanvil.musa.domain.model.Event
import org.softwareanvil.musa.domain.model.EventDetail
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.domain.repository.EventRepository

/** Emits every event for the event list. */
class ObserveEventsUseCase(private val repository: EventRepository) {
	operator fun invoke(): Flow<List<Event>> = repository.observeAll()
}

/** Emits one event with its stock, sales and expenses. */
class ObserveEventDetailUseCase(private val repository: EventRepository) {
	operator fun invoke(id: EventId): Flow<EventDetail?> = repository.observeDetail(id)
}

/** Finishes an event: unsold units go back to the catalogue and it becomes read-only. */
class CloseEventUseCase(private val repository: EventRepository, private val clock: Clock) {

	suspend operator fun invoke(id: EventId): EventResult {
		if (!repository.requireDetail(id).event.isOpen) return rejected(EventError.EventClosed)
		repository.close(id, clock.now())
		return EventResult.Done
	}
}

/** Deletes an event without sales, returning its stock; events with sales must be closed instead. */
class DeleteEventUseCase(private val repository: EventRepository, private val clock: Clock) {

	suspend operator fun invoke(id: EventId): EventResult {
		if (repository.requireDetail(id).sales.isNotEmpty()) return rejected(EventError.HasSales)
		repository.delete(id, clock.now())
		return EventResult.Done
	}
}
