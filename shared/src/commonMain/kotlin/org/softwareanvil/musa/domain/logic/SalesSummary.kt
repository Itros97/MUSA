package org.softwareanvil.musa.domain.logic

import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.Sale
import org.softwareanvil.musa.domain.model.sumOfMoney

/** Totals of a set of sales: how many, how many units, what came in, what it cost and the margin left. */
data class SalesSummary(val count: Int, val units: Int, val revenue: Money, val cost: Money) {

	val margin: Money get() = revenue - cost

	companion object {

		/** Adds up [sales]. */
		fun of(sales: List<Sale>) = SalesSummary(
			count = sales.size,
			units = sales.sumOf { it.units },
			revenue = sales.sumOfMoney { it.total },
			cost = sales.sumOfMoney { it.cost },
		)
	}
}
