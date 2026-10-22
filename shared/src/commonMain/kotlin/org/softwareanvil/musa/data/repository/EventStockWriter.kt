package org.softwareanvil.musa.data.repository

import org.softwareanvil.musa.data.mapper.EventMapper
import org.softwareanvil.musa.db.MusaDatabase

/** Moves units between the catalogue and an event's stock; callers wrap every call in a transaction. */
internal class EventStockWriter(private val database: MusaDatabase) {

	private val catalogue = CatalogueStockWriter(database)
	private val queries = database.eventStockQueries

	/** Takes [quantity] units out of the catalogue and adds them to the event line. */
	fun move(eventId: String, variantId: String, quantity: Long, at: Long) {
		catalogue.adjust(variantId, -quantity, at)
		val taken = queries.selectTaken(eventId, variantId).executeAsOneOrNull()
		if (taken == null) {
			queries.insert(eventId, variantId, quantity, at)
		} else {
			queries.updateTaken(taken = taken + quantity, at = at, eventId = eventId, variantId = variantId)
		}
	}

	/** Takes [quantity] units off the event line and puts them back in the catalogue. */
	fun giveBack(eventId: String, variantId: String, quantity: Long, at: Long) {
		val taken = checkNotNull(queries.selectTaken(eventId, variantId).executeAsOneOrNull()) {
			"Variant $variantId was not taken to event $eventId"
		}
		queries.updateTaken(taken = taken - quantity, at = at, eventId = eventId, variantId = variantId)
		catalogue.adjust(variantId, quantity, at)
	}

	/** Puts every unsold unit of the event back in the catalogue. */
	fun giveBackRemaining(eventId: String, at: Long) {
		queries.selectByEvent(eventId, EventMapper::toStockLine).executeAsList()
			.filter { it.remaining > 0 }
			.forEach { giveBack(eventId, it.variantId.value, it.remaining.toLong(), at) }
	}
}
