package org.softwareanvil.musa.domain.logic

import kotlin.math.absoluteValue
import org.softwareanvil.musa.domain.model.Money

/** Formats [Money] the Spanish way: "1.234,50 €". */
object MoneyFormatter {

	/** Display text with currency; whole amounts drop the decimals ("12 €"). */
	fun format(money: Money): String = "${formatForInput(money)} €"

	/** Text without currency, used to prefill editable fields. */
	fun formatForInput(money: Money): String {
		val absolute = money.cents.absoluteValue
		val euros = groupThousands(absolute / 100)
		val cents = absolute % 100
		val sign = if (money.cents < 0) "-" else ""
		return if (cents == 0L) "$sign$euros" else "$sign$euros,${cents.toString().padStart(2, '0')}"
	}

	private fun groupThousands(value: Long): String =
		value.toString().reversed().chunked(3).joinToString(".").reversed()
}
