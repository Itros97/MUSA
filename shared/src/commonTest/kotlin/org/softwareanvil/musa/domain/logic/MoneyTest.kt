package org.softwareanvil.musa.domain.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.sumOfMoney

class MoneyParserTest {

	@Test
	fun `parses whole euros`() = assertEquals(Money(1200), MoneyParser.parse("12"))

	@Test
	fun `parses comma decimals`() = assertEquals(Money(1250), MoneyParser.parse("12,5"))

	@Test
	fun `parses dot decimals with two digits`() = assertEquals(Money(1205), MoneyParser.parse("12.05"))

	@Test
	fun `ignores euro sign and spaces`() = assertEquals(Money(800), MoneyParser.parse(" 8 € "))

	@Test
	fun `rejects empty text`() = assertNull(MoneyParser.parse(""))

	@Test
	fun `rejects letters`() = assertNull(MoneyParser.parse("abc"))

	@Test
	fun `rejects negative amounts`() = assertNull(MoneyParser.parse("-3"))

	@Test
	fun `rejects more than two decimals`() = assertNull(MoneyParser.parse("1,234"))
}

class MoneyFormatterTest {

	@Test
	fun `drops decimals for whole amounts`() = assertEquals("12 €", MoneyFormatter.format(Money(1200)))

	@Test
	fun `pads cents to two digits`() = assertEquals("12,05 €", MoneyFormatter.format(Money(1205)))

	@Test
	fun `groups thousands with dots`() = assertEquals("1.234,50 €", MoneyFormatter.format(Money(123450)))

	@Test
	fun `keeps the sign of negative amounts`() = assertEquals("-3,50 €", MoneyFormatter.format(Money(-350)))

	@Test
	fun `formats input text without currency`() = assertEquals("9,90", MoneyFormatter.formatForInput(Money(990)))
}

class MarginTest {

	@Test
	fun `computes profit and percent of price`() =
		assertEquals(Margin(Money(650), 65), Margin.of(price = Money(1000), cost = Money(350)))

	@Test
	fun `flags negative margins`() = assertEquals(true, Margin.of(Money(500), Money(800)).isNegative)

	@Test
	fun `gives full margin when cost is zero`() = assertEquals(100, Margin.of(Money(4000), Money.ZERO).percent)

	@Test
	fun `has no percent when price is zero`() = assertNull(Margin.of(Money.ZERO, Money(100)).percent)
}

class MoneyArithmeticTest {

	@Test
	fun `multiplies by a quantity`() = assertEquals(Money(3600), Money(1200) * 3)

	@Test
	fun `adds up picked amounts`() = assertEquals(Money(350), listOf(100L, 250L).sumOfMoney { Money(it) })

	@Test
	fun `adds up an empty list to zero`() = assertEquals(Money.ZERO, emptyList<Long>().sumOfMoney { Money(it) })
}
