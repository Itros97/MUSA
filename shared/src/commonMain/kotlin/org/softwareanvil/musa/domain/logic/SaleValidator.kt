package org.softwareanvil.musa.domain.logic

import org.softwareanvil.musa.domain.model.SaleChannel
import org.softwareanvil.musa.domain.model.SaleDraft

/** Checks the rules a sale draft must meet before saving; stock is checked by the use case. */
class SaleValidator {

	/** Returns every problem found; an empty list means the draft is valid. */
	fun validate(draft: SaleDraft): List<SaleError> = buildList {
		if (draft.lines.isEmpty()) add(SaleError.NoLines)
		if (draft.channel == SaleChannel.EVENT) add(SaleError.EventChannel)
		if (draft.shippingCharged.cents < 0 || draft.shippingCost.cents < 0) add(SaleError.NegativeShipping)
		draft.lines.forEach { line ->
			if (line.quantity <= 0) add(SaleError.InvalidQuantity(line.id))
			if (line.unitPrice.cents < 0) add(SaleError.NegativePrice(line.id))
		}
	}
}
