package org.softwareanvil.musa.domain.usecase

import kotlin.time.Clock
import org.softwareanvil.musa.domain.logic.EventValidator
import org.softwareanvil.musa.domain.model.Event
import org.softwareanvil.musa.domain.model.EventDraft
import org.softwareanvil.musa.domain.model.EventStatus
import org.softwareanvil.musa.domain.repository.EventRepository

/** Validates an event draft and stores it; new events start open. */
class SaveEventUseCase(
	private val repository: EventRepository,
	private val validator: EventValidator,
	private val clock: Clock,
) {

	suspend operator fun invoke(draft: EventDraft): EventResult {
		val clean = draft.copy(name = draft.name.trim(), location = draft.location.trim())
		val errors = validator.validate(clean)
		if (errors.isNotEmpty()) return EventResult.Rejected(errors)
		val now = clock.now()
		val existing = repository.findById(clean.id)
		repository.save(
			Event(
				id = clean.id,
				name = clean.name,
				location = clean.location,
				startDate = clean.startDate,
				endDate = clean.endDate,
				status = existing?.status ?: EventStatus.OPEN,
				createdAt = existing?.createdAt ?: now,
				updatedAt = now,
			),
		)
		return EventResult.Done
	}
}
