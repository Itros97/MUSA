package org.softwareanvil.musa.domain.logic

import kotlinx.datetime.LocalDate

/** Formats dates the Spanish way with short month names: "18 abr 2026". */
object DateFormatter {

	private val MONTHS = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

	/** One date: "18 abr 2026". */
	fun format(date: LocalDate): String = "${date.day} ${month(date)} ${date.year}"

	/** A range that repeats only what changes: "18–19 abr 2026", "30 abr – 2 may 2026". */
	fun formatRange(start: LocalDate, end: LocalDate): String = when {
		start == end -> format(start)
		start.year != end.year -> "${format(start)} – ${format(end)}"
		start.month != end.month -> "${start.day} ${month(start)} – ${format(end)}"
		else -> "${start.day}–${format(end)}"
	}

	private fun month(date: LocalDate) = MONTHS[date.month.ordinal]
}
