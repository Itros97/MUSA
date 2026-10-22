package org.softwareanvil.musa.domain.logic

/** Parses whole counts and centimetre measures typed in the editor. */
object NumberParser {

	private val COUNT = Regex("""^\d{1,6}$""")
	private val CENTIMETRES = Regex("""^(\d{1,5})(?:[.,](\d))?$""")

	/** Parses a non-negative whole number such as a stock count; null when invalid. */
	fun parseCount(text: String): Int? = text.trim().takeIf { COUNT.matches(it) }?.toInt()

	/** Parses centimetres with at most one decimal ("29,7") into millimetres; null when invalid. */
	fun parseCentimetres(text: String): Int? {
		val match = CENTIMETRES.matchEntire(text.trim()) ?: return null
		val (whole, decimal) = match.destructured
		return whole.toInt() * 10 + (decimal.toIntOrNull() ?: 0)
	}

	/** Formats millimetres back as centimetres ("29,7", "30"). */
	fun formatCentimetres(millimetres: Int): String =
		if (millimetres % 10 == 0) "${millimetres / 10}" else "${millimetres / 10},${millimetres % 10}"
}
