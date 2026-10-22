package org.softwareanvil.musa.domain.logic

import org.softwareanvil.musa.domain.model.TagColor

/** Rules that make "pokemon ", "Pokémon" and "POKEMON" the same tag. */
object TagNormalizer {

	private val SPACES = Regex("""\s+""")

	/** Trims and collapses inner spaces, keeping the casing the artist typed. */
	fun displayName(raw: String): String = raw.trim().replace(SPACES, " ")

	/** Key used to detect duplicates: folded display name. */
	fun key(raw: String): String = TextNormalizer.fold(displayName(raw))

	/** Removes blanks and duplicates, keeping the first spelling of each tag. */
	fun distinct(names: List<String>): List<String> = names
		.map(::displayName)
		.filter { it.isNotEmpty() }
		.distinctBy(::key)

	/** Picks a stable colour from the key, so the same tag always gets the same colour. */
	fun colorFor(key: String): TagColor {
		// FNV-1a: a plain *31 hash barely changes modulo 8, so most tags ended up the same colour.
		val hash = key.fold(FNV_OFFSET) { acc, char -> (acc xor char.code) * FNV_PRIME }
		return TagColor.entries[(hash xor (hash ushr 16)).mod(TagColor.entries.size)]
	}

	private const val FNV_OFFSET = -2128831035
	private const val FNV_PRIME = 16777619
}
