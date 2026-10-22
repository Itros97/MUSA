package org.softwareanvil.musa.domain.usecase

import kotlin.time.Clock
import org.softwareanvil.musa.domain.logic.EventError
import org.softwareanvil.musa.domain.logic.IdGenerator
import org.softwareanvil.musa.domain.model.EventExpense
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.domain.model.ExpenseId
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.repository.EventRepository

/** Records what attending an open event cost: stand, travel, lodging... */
class AddExpenseUseCase(
	private val repository: EventRepository,
	private val clock: Clock,
	private val ids: IdGenerator,
) {

	suspend operator fun invoke(eventId: EventId, concept: String, amount: Money): EventResult {
		val inputErrors = buildList {
			if (concept.isBlank()) add(EventError.BlankConcept)
			if (amount.cents < 0) add(EventError.NegativeAmount)
		}
		if (inputErrors.isNotEmpty()) return EventResult.Rejected(inputErrors)
		if (!repository.requireDetail(eventId).event.isOpen) return rejected(EventError.EventClosed)
		repository.addExpense(EventExpense(ExpenseId(ids.next()), eventId, concept.trim(), amount, clock.now()))
		return EventResult.Done
	}
}

/** Removes an expense from an open event. */
class DeleteExpenseUseCase(private val repository: EventRepository, private val clock: Clock) {

	suspend operator fun invoke(eventId: EventId, id: ExpenseId): EventResult {
		if (!repository.requireDetail(eventId).event.isOpen) return rejected(EventError.EventClosed)
		repository.deleteExpense(id, clock.now())
		return EventResult.Done
	}
}
