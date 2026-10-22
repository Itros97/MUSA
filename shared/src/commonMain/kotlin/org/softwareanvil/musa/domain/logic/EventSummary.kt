package org.softwareanvil.musa.domain.logic

import org.softwareanvil.musa.domain.model.EventDetail
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.sumOfMoney

/** Totals of an event: what came in, what the goods sold cost and what attending cost. */
data class EventSummary(
	val unitsSold: Int,
	val gross: Money,
	val costOfGoods: Money,
	val expenses: Money,
) {

	/** Gross minus cost of goods minus expenses; negative when the event lost money. */
	val profit: Money get() = gross - costOfGoods - expenses

	companion object {

		/** Adds up the sales and expenses of [detail]. */
		fun of(detail: EventDetail) = EventSummary(
			unitsSold = detail.sales.sumOf { it.units },
			gross = detail.sales.sumOfMoney { it.total },
			costOfGoods = detail.sales.sumOfMoney { it.cost },
			expenses = detail.expenses.sumOfMoney { it.amount },
		)
	}
}
