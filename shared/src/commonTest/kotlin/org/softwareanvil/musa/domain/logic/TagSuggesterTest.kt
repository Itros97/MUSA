package org.softwareanvil.musa.domain.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import org.softwareanvil.musa.fakes.tag

class TagSuggesterTest {

	private val all = listOf(tag("Pokémon"), tag("Navidad"), tag("Gatos"))

	@Test
	fun `suggests unselected tags when nothing is typed`() =
		assertEquals(listOf("Navidad", "Gatos"), TagSuggester.suggest(all, listOf("pokemon"), "").map { it.name })

	@Test
	fun `matches typed text ignoring accents`() =
		assertEquals(listOf("Pokémon"), TagSuggester.suggest(all, emptyList(), "poke").map { it.name })
}
