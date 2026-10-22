package org.softwareanvil.musa.domain.usecase

import kotlin.time.Clock
import org.softwareanvil.musa.domain.logic.EventError
import org.softwareanvil.musa.domain.logic.IdGenerator
import org.softwareanvil.musa.domain.model.EventId
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.Sale
import org.softwareanvil.musa.domain.model.SaleChannel
import org.softwareanvil.musa.domain.model.SaleId
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.domain.repository.EventRepository
import org.softwareanvil.musa.domain.repository.SaleRepository

/** Sells units from an event's stock at [unitPrice]; the cost is copied from the variant at that moment. */
class RecordEventSaleUseCase(
	private val eventRepository: EventRepository,
	private val saleRepository: SaleRepository,
	private val clock: Clock,
	private val ids: IdGenerator,
) {

	suspend operator fun invoke(eventId: EventId, variantId: VariantId, quantity: Int, unitPrice: Money): EventResult {
		val inputErrors = buildList {
			if (quantity <= 0) add(EventError.InvalidQuantity)
			if (unitPrice.cents < 0) add(EventError.NegativePrice)
		}
		if (inputErrors.isNotEmpty()) return EventResult.Rejected(inputErrors)
		val detail = eventRepository.requireDetail(eventId)
		if (!detail.event.isOpen) return rejected(EventError.EventClosed)
		val line = checkNotNull(detail.lineOf(variantId)) { "Variant ${variantId.value} was not taken to ${eventId.value}" }
		if (quantity > line.remaining) return rejected(EventError.ExceedsRemaining(line.remaining))
		saleRepository.record(
			Sale(
				id = SaleId(ids.next()),
				variantId = variantId,
				productName = line.productName,
				variantName = line.variantName,
				quantity = quantity,
				unitPrice = unitPrice,
				unitCost = line.cost,
				channel = SaleChannel.EVENT,
				eventId = eventId,
				soldAt = clock.now(),
			),
		)
		return EventResult.Done
	}
}

/** Deletes a sale, so its units count as remaining again; sales of closed events are kept. */
class UndoSaleUseCase(
	private val saleRepository: SaleRepository,
	private val eventRepository: EventRepository,
	private val clock: Clock,
) {

	suspend operator fun invoke(id: SaleId): EventResult {
		val sale = checkNotNull(saleRepository.findById(id)) { "Cannot undo missing sale ${id.value}" }
		val event = sale.eventId?.let { eventRepository.findById(it) }
		if (event?.isOpen == false) return rejected(EventError.EventClosed)
		saleRepository.delete(id, clock.now())
		return EventResult.Done
	}
}
