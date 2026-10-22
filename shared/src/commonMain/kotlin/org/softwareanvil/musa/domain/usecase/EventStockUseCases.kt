package org.softwareanvil.musa.domain.usecase

import kotlin.time.Clock
import org.softwareanvil.musa.domain.logic.EventError
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.domain.repository.EventRepository
import org.softwareanvil.musa.domain.repository.ProductRepository

/** Takes units of a physical variant from the catalogue to an open event. */
class MoveStockToEventUseCase(
	private val eventRepository: EventRepository,
	private val productRepository: ProductRepository,
	private val clock: Clock,
) {

	suspend operator fun invoke(eventId: EventId, variantId: VariantId, quantity: Int): EventResult {
		if (quantity <= 0) return rejected(EventError.InvalidQuantity)
		if (!eventRepository.requireDetail(eventId).event.isOpen) return rejected(EventError.EventClosed)
		val available = checkNotNull(productRepository.findStock(variantId)) {
			"Variant ${variantId.value} is not a physical variant in the catalogue"
		}
		if (quantity > available) return rejected(EventError.NotEnoughStock(available))
		eventRepository.moveStock(eventId, variantId, quantity, clock.now())
		return EventResult.Done
	}
}

/** Brings unsold units of a variant back from an open event to the catalogue. */
class ReturnStockFromEventUseCase(private val repository: EventRepository, private val clock: Clock) {

	suspend operator fun invoke(eventId: EventId, variantId: VariantId, quantity: Int): EventResult {
		if (quantity <= 0) return rejected(EventError.InvalidQuantity)
		val detail = repository.requireDetail(eventId)
		if (!detail.event.isOpen) return rejected(EventError.EventClosed)
		val remaining = detail.lineOf(variantId)?.remaining ?: 0
		if (quantity > remaining) return rejected(EventError.ExceedsRemaining(remaining))
		repository.returnStock(eventId, variantId, quantity, clock.now())
		return EventResult.Done
	}
}
