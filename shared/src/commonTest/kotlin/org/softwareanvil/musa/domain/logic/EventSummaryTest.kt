package org.softwareanvil.musa.domain.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import org.softwareanvil.musa.domain.model.EventDetail
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.fakes.event
import org.softwareanvil.musa.fakes.expense
import org.softwareanvil.musa.fakes.sale

class EventSummaryTest {

	@Test
	fun `adds up gross, cost of goods, expenses and profit`() {
		val detail = EventDetail(
			event = event(),
			stock = emptyList(),
			sales = listOf(sale("s1", quantity = 2, price = 1000, cost = 300), sale("s2", quantity = 1, price = 800, cost = 200)),
			expenses = listOf(expense("x1", amount = 500), expense("x2", amount = 300)),
		)
		val summary = EventSummary.of(detail)
		assertEquals(3, summary.unitsSold)
		assertEquals(Money(2800), summary.gross)
		assertEquals(Money(800), summary.costOfGoods)
		assertEquals(Money(800), summary.expenses)
		assertEquals(Money(1200), summary.profit)
	}

	@Test
	fun `is zero for an event without sales or expenses`() {
		val summary = EventSummary.of(EventDetail(event(), emptyList(), emptyList(), emptyList()))
		assertEquals(EventSummary(0, Money.ZERO, Money.ZERO, Money.ZERO), summary)
		assertEquals(Money.ZERO, summary.profit)
	}

	@Test
	fun `has a negative profit when there are only expenses`() {
		val summary = EventSummary.of(EventDetail(event(), emptyList(), emptyList(), listOf(expense(amount = 5000))))
		assertEquals(Money(-5000), summary.profit)
	}
}
