package org.softwareanvil.musa.domain.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

class DateFormatterTest {

	@Test
	fun `formats a single date`() = assertEquals("8 ene 2026", DateFormatter.format(LocalDate(2026, 1, 8)))

	@Test
	fun `formats a one-day range as one date`() =
		assertEquals("18 abr 2026", DateFormatter.formatRange(LocalDate(2026, 4, 18), LocalDate(2026, 4, 18)))

	@Test
	fun `shares month and year inside one month`() =
		assertEquals("18–19 abr 2026", DateFormatter.formatRange(LocalDate(2026, 4, 18), LocalDate(2026, 4, 19)))

	@Test
	fun `shares the year across months`() =
		assertEquals("30 abr – 2 may 2026", DateFormatter.formatRange(LocalDate(2026, 4, 30), LocalDate(2026, 5, 2)))

	@Test
	fun `writes both years across years`() =
		assertEquals("30 dic 2026 – 2 ene 2027", DateFormatter.formatRange(LocalDate(2026, 12, 30), LocalDate(2027, 1, 2)))
}
