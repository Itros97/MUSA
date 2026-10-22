package org.softwareanvil.musa.domain.logic

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.softwareanvil.musa.domain.model.Sale
import org.softwareanvil.musa.domain.model.SaleChannel

/** Time ranges the sales list can show. */
enum class SalesPeriod { THIS_MONTH, LAST_MONTH, THIS_YEAR, ALL }

/** Filters of the sales list; a null [channel] means every channel. */
data class SalesQuery(val period: SalesPeriod = SalesPeriod.THIS_MONTH, val channel: SaleChannel? = null)

/** Applies a [SalesQuery] relative to [today], newest sale first. */
object SalesFilter {

	/** Returns the sales of the period and channel, newest first. */
	fun apply(sales: List<Sale>, query: SalesQuery, today: LocalDate, timeZone: TimeZone): List<Sale> {
		val range = rangeOf(query.period, today)
		return sales
			.filter { query.channel == null || it.channel == query.channel }
			.filter { range == null || it.soldAt.toLocalDateTime(timeZone).date in range }
			.sortedByDescending { it.soldAt }
	}

	/** First day included and first day excluded, or null for every date. */
	private fun rangeOf(period: SalesPeriod, today: LocalDate): OpenEndRange<LocalDate>? {
		val monthStart = LocalDate(today.year, today.month, 1)
		return when (period) {
			SalesPeriod.THIS_MONTH -> monthStart..<monthStart.plus(1, DateTimeUnit.MONTH)
			SalesPeriod.LAST_MONTH -> monthStart.minus(1, DateTimeUnit.MONTH)..<monthStart
			SalesPeriod.THIS_YEAR -> LocalDate(today.year, 1, 1)..<LocalDate(today.year + 1, 1, 1)
			SalesPeriod.ALL -> null
		}
	}
}
