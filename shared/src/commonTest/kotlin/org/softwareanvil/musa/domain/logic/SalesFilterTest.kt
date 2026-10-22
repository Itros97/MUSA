package org.softwareanvil.musa.domain.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.SaleChannel
import org.softwareanvil.musa.fakes.sale

class SalesFilterTest {

	private val zone = TimeZone.of("Europe/Madrid")
	private val today = LocalDate(2026, 1, 15)

	private fun on(id: String, date: LocalDate, hour: Int = 12, channel: SaleChannel = SaleChannel.ONLINE_SHOP) =
		sale(id, eventId = null, channel = channel, soldAt = date.atTime(LocalTime(hour, 0)).toInstant(zone))

	private val lastYearEnd = on("dec", LocalDate(2025, 12, 31), hour = 23)
	private val monthStart = on("jan1", LocalDate(2026, 1, 1), hour = 0)
	private val midMonth = on("jan15", LocalDate(2026, 1, 15), channel = SaleChannel.SOCIAL)
	private val all = listOf(lastYearEnd, monthStart, midMonth)

	private fun ids(query: SalesQuery) = SalesFilter.apply(all, query, today, zone).map { it.id.value }

	@Test
	fun `this month starts on the first local day`() = assertEquals(listOf("jan15", "jan1"), ids(SalesQuery(SalesPeriod.THIS_MONTH)))

	@Test
	fun `last month crosses back into last year`() = assertEquals(listOf("dec"), ids(SalesQuery(SalesPeriod.LAST_MONTH)))

	@Test
	fun `this year leaves out december`() = assertEquals(listOf("jan15", "jan1"), ids(SalesQuery(SalesPeriod.THIS_YEAR)))

	@Test
	fun `all keeps every sale newest first`() = assertEquals(listOf("jan15", "jan1", "dec"), ids(SalesQuery(SalesPeriod.ALL)))

	@Test
	fun `filters by channel`() = assertEquals(listOf("jan15"), ids(SalesQuery(SalesPeriod.ALL, SaleChannel.SOCIAL)))

	@Test
	fun `summary adds up the filtered sales`() {
		val summary = SalesSummary.of(SalesFilter.apply(all, SalesQuery(SalesPeriod.THIS_MONTH), today, zone))
		assertEquals(SalesSummary(count = 2, units = 2, revenue = Money(2000), cost = Money(600)), summary)
		assertEquals(Money(1400), summary.margin)
	}

	@Test
	fun `summary of nothing is zero`() = assertEquals(SalesSummary(0, 0, Money.ZERO, Money.ZERO), SalesSummary.of(emptyList()))
}
