package org.softwareanvil.musa.ui.sales

import org.softwareanvil.musa.domain.logic.SaleError
import org.softwareanvil.musa.domain.logic.SalesPeriod
import org.softwareanvil.musa.domain.model.SaleChannel

/** Channels the artist can pick in the sale editor; event sales are only made from their event. */
val SELECTABLE_CHANNELS = SaleChannel.entries - SaleChannel.EVENT

/** Spanish name of each channel. */
fun SaleChannel.label(): String = when (this) {
	SaleChannel.EVENT -> "Evento"
	SaleChannel.ONLINE_SHOP -> "Tienda online"
	SaleChannel.SOCIAL -> "Redes y mensajes"
	SaleChannel.MARKETPLACE -> "Marketplace"
	SaleChannel.IN_PERSON -> "En persona"
	SaleChannel.OTHER -> "Otro"
}

/** Spanish name of each period filter. */
fun SalesPeriod.label(): String = when (this) {
	SalesPeriod.THIS_MONTH -> "Este mes"
	SalesPeriod.LAST_MONTH -> "Mes pasado"
	SalesPeriod.THIS_YEAR -> "Este año"
	SalesPeriod.ALL -> "Todo"
}

/** What the artist reads when a sale is rejected. */
fun SaleError.message(): String = when (this) {
	SaleError.NoLines -> "Añade al menos un producto"
	SaleError.EventChannel -> "Las ventas de evento se apuntan desde su evento"
	SaleError.NegativeShipping -> "El envío no puede ser negativo"
	SaleError.ManagedByEvent -> "Esta venta es de un evento: deshazla desde el evento"
	is SaleError.InvalidQuantity -> "La cantidad tiene que ser al menos 1"
	is SaleError.NegativePrice -> "El precio no puede ser negativo"
	is SaleError.NotEnoughStock -> "En el catálogo solo quedan $available"
}
