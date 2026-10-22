package org.softwareanvil.musa.domain.usecase

import kotlinx.coroutines.flow.first
import org.softwareanvil.musa.domain.logic.EventError
import org.softwareanvil.musa.domain.model.EventDetail
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.domain.repository.EventRepository

/** Outcome of an action on an event. */
sealed interface EventResult {
	data object Done : EventResult
	data class Rejected(val errors: List<EventError>) : EventResult
}

internal fun rejected(error: EventError) = EventResult.Rejected(listOf(error))

/** Loads the current detail of an event; a missing event is a caller bug, so it fails loud. */
internal suspend fun EventRepository.requireDetail(id: EventId): EventDetail =
	checkNotNull(observeDetail(id).first()) { "Missing event ${id.value}" }
