package org.softwareanvil.musa.domain.logic

import org.softwareanvil.musa.domain.model.EventDraft

/** Checks the rules an event draft must meet before saving. */
class EventValidator {

	/** Returns every problem found; an empty list means the draft is valid. */
	fun validate(draft: EventDraft): List<EventError> = buildList {
		if (draft.name.isBlank()) add(EventError.BlankName)
		if (draft.endDate < draft.startDate) add(EventError.EndBeforeStart)
	}
}
