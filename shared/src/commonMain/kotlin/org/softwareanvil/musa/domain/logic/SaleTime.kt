package org.softwareanvil.musa.domain.logic

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.todayIn

/** Turns the day picked in the sale editor into the moment the sale is stored at. */
object SaleTime {

	private val NOON = LocalTime(12, 0)

	/** Now when [date] is today; noon of that day otherwise, so it never shifts to another day. */
	fun at(date: LocalDate, clock: Clock, timeZone: TimeZone): Instant =
		if (date == clock.todayIn(timeZone)) clock.now() else date.atTime(NOON).toInstant(timeZone)
}
