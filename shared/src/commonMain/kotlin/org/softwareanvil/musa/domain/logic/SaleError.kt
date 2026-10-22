package org.softwareanvil.musa.domain.logic

import org.softwareanvil.musa.domain.model.SaleLineId
import org.softwareanvil.musa.domain.model.VariantId

/** Reasons a sale, or deleting one, is rejected. */
sealed interface SaleError {
	data object NoLines : SaleError
	data object EventChannel : SaleError
	data object NegativeShipping : SaleError
	data object ManagedByEvent : SaleError
	data class InvalidQuantity(val lineId: SaleLineId) : SaleError
	data class NegativePrice(val lineId: SaleLineId) : SaleError

	/** The catalogue only has [available] units of the variant, counting every line that sells it. */
	data class NotEnoughStock(val variantId: VariantId, val available: Int) : SaleError
}
