package org.softwareanvil.musa.domain.model

/** An amount in euros, stored as whole cents to avoid rounding errors. */
data class Money(val cents: Long) : Comparable<Money> {

	operator fun plus(other: Money) = Money(cents + other.cents)

	operator fun minus(other: Money) = Money(cents - other.cents)

	override fun compareTo(other: Money) = cents.compareTo(other.cents)

	companion object {
		val ZERO = Money(0)
	}
}
