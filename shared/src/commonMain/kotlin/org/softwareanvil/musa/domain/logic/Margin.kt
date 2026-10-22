package org.softwareanvil.musa.domain.logic

import org.softwareanvil.musa.domain.model.Money

/** What the artist earns per unit: price minus cost, and that profit as a share of the price. */
data class Margin(val amount: Money, val percent: Int?) {

	val isNegative: Boolean get() = amount.cents < 0

	companion object {

		/** Computes the margin; [percent] is null when the price is zero. */
		fun of(price: Money, cost: Money): Margin {
			val amount = price - cost
			val percent = price.cents.takeIf { it > 0 }?.let { (amount.cents * 100 / it).toInt() }
			return Margin(amount, percent)
		}
	}
}
