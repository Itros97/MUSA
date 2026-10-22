package org.softwareanvil.musa.domain.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TextNormalizerTest {

	@Test
	fun `removes accents and case`() = assertEquals("lamina nino", TextNormalizer.fold("Lámina NIÑO"))
}

class TagNormalizerTest {

	@Test
	fun `trims and collapses spaces keeping casing`() =
		assertEquals("Fan Art", TagNormalizer.displayName("  Fan   Art "))

	@Test
	fun `treats case and accent variants as the same tag`() =
		assertEquals(listOf("Pokémon"), TagNormalizer.distinct(listOf("Pokémon", "pokemon ", "POKEMON")))

	@Test
	fun `drops blank names`() = assertEquals(listOf("Gatos"), TagNormalizer.distinct(listOf(" ", "Gatos", "")))

	@Test
	fun `gives the same colour to the same key`() =
		assertEquals(TagNormalizer.colorFor("navidad"), TagNormalizer.colorFor(TagNormalizer.key("Navidad ")))
}

class NumberParserTest {

	@Test
	fun `parses whole counts`() = assertEquals(12, NumberParser.parseCount(" 12 "))

	@Test
	fun `rejects negative counts`() = assertNull(NumberParser.parseCount("-1"))

	@Test
	fun `rejects decimal counts`() = assertNull(NumberParser.parseCount("1,5"))

	@Test
	fun `converts centimetres with one decimal to millimetres`() =
		assertEquals(297, NumberParser.parseCentimetres("29,7"))

	@Test
	fun `rejects centimetres with two decimals`() = assertNull(NumberParser.parseCentimetres("29,75"))

	@Test
	fun `formats millimetres as centimetres`() {
		assertEquals("29,7", NumberParser.formatCentimetres(297))
		assertEquals("30", NumberParser.formatCentimetres(300))
	}
}
