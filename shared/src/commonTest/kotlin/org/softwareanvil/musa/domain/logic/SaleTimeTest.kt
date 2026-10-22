package org.softwareanvil.musa.domain.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.softwareanvil.musa.fakes.EPOCH
import org.softwareanvil.musa.fakes.FixedClock

class SaleTimeTest {

	private val zone = TimeZone.of("Europe/Madrid")
	private val clock = FixedClock(EPOCH)

	@Test
	fun `today is stored at the current moment`() {
		val today = EPOCH.toLocalDateTime(zone).date
		assertEquals(EPOCH, SaleTime.at(today, clock, zone))
	}

	@Test
	fun `another day is stored at its local noon`() {
		val stored = SaleTime.at(LocalDate(2026, 3, 29), clock, zone).toLocalDateTime(zone)
		assertEquals(LocalDate(2026, 3, 29), stored.date)
		assertEquals(12, stored.hour)
	}
}
