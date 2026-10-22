package org.softwareanvil.musa.domain.logic

/** Reasons an event, or an action on it, is rejected. */
sealed interface EventError {
	data object BlankName : EventError
	data object EndBeforeStart : EventError
	data object InvalidQuantity : EventError
	data object NegativePrice : EventError
	data object BlankConcept : EventError
	data object NegativeAmount : EventError
	data object EventClosed : EventError
	data object HasSales : EventError

	/** The catalogue only has [available] units of the variant. */
	data class NotEnoughStock(val available: Int) : EventError

	/** Only [remaining] units of the variant are left at the event. */
	data class ExceedsRemaining(val remaining: Int) : EventError
}
