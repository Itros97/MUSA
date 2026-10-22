package org.softwareanvil.musa.domain.logic

import org.softwareanvil.musa.domain.model.Money

/** Parses what the artist types in a price field ("12", "12,5", "12.50 €") into [Money]. */
object MoneyParser {

	private val AMOUNT = Regex("""^(\d{1,9})(?:[.,](\d{1,2}))?$""")

	/** Returns the amount, or null when the text is empty, negative or has more than two decimals. */
	fun parse(text: String): Money? {
		val clean = text.replace("€", "").trim()
		val match = AMOUNT.matchEntire(clean) ?: return null
		val (euros, decimals) = match.destructured
		return Money(euros.toLong() * 100 + decimals.padEnd(2, '0').toLong())
	}
}
