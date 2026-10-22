package org.softwareanvil.musa.ui.events

import org.softwareanvil.musa.domain.logic.DateFormatter
import org.softwareanvil.musa.domain.logic.EventError
import org.softwareanvil.musa.domain.model.Event
import org.softwareanvil.musa.domain.model.EventStatus

/** What the artist reads when an action is rejected. */
fun EventError.message(): String = when (this) {
	EventError.BlankName -> "Ponle un nombre"
	EventError.EndBeforeStart -> "No puede terminar antes de empezar"
	EventError.InvalidQuantity -> "La cantidad tiene que ser al menos 1"
	EventError.NegativePrice -> "El precio no puede ser negativo"
	EventError.BlankConcept -> "Escribe en qué fue el gasto"
	EventError.NegativeAmount -> "El importe no puede ser negativo"
	EventError.EventClosed -> "El evento está cerrado"
	EventError.HasSales -> "Tiene ventas: ciérralo en vez de eliminarlo"
	is EventError.NotEnoughStock -> "En el catálogo solo quedan $available"
	is EventError.ExceedsRemaining -> "En el evento solo quedan $remaining"
}

/** Spanish name of each status. */
fun EventStatus.label(): String = when (this) {
	EventStatus.OPEN -> "Abierto"
	EventStatus.CLOSED -> "Cerrado"
}

/** "18–19 abr 2026 · Pabellón 2", without the place when it is empty. */
fun Event.whenAndWhereLabel(): String =
	listOf(DateFormatter.formatRange(startDate, endDate), location).filter { it.isNotBlank() }.joinToString(" · ")

/** "Lámina gato · A4", or just the product name when the variant has none. */
fun itemLabel(productName: String, variantName: String): String =
	if (variantName.isBlank()) productName else "$productName · $variantName"
