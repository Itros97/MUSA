package org.softwareanvil.musa.domain.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.fakes.sale
import org.softwareanvil.musa.fakes.saleLine

class SaleTest {

	private val order = sale(eventId = null, shippingCharged = 450, shippingCost = 380).copy(
		lines = listOf(saleLine("a", quantity = 2, price = 1200, cost = 350), saleLine("b", quantity = 1, price = 300, cost = 60)),
	)

	@Test
	fun `adds lines and shipping charged into the total`() = assertEquals(Money(2700 + 450), order.total)

	@Test
	fun `adds goods and shipping paid into the cost`() = assertEquals(Money(760 + 380), order.cost)

	@Test
	fun `margin is total minus cost`() = assertEquals(Money(3150 - 1140), order.margin)

	@Test
	fun `counts units across lines`() = assertEquals(3, order.units)

	@Test
	fun `margin can be negative`() =
		assertEquals(Money(-200), sale(price = 300, cost = 500, eventId = null).margin)
}
