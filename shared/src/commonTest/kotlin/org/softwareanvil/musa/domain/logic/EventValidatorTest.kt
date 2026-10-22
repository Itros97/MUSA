package org.softwareanvil.musa.domain.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import org.softwareanvil.musa.domain.model.EventDraft
import org.softwareanvil.musa.domain.model.EventId

class EventValidatorTest {

	private val validator = EventValidator()
	private val draft = EventDraft(EventId("e1"), "Feria", "", LocalDate(2026, 5, 1), LocalDate(2026, 5, 2))

	@Test
	fun `accepts a named event that ends after it starts`() = assertEquals(emptyList(), validator.validate(draft))

	@Test
	fun `accepts a one-day event`() = assertEquals(emptyList(), validator.validate(draft.copy(endDate = draft.startDate)))

	@Test
	fun `rejects a blank name`() = assertEquals(listOf(EventError.BlankName), validator.validate(draft.copy(name = "  ")))

	@Test
	fun `rejects an end date before the start date`() =
		assertEquals(listOf(EventError.EndBeforeStart), validator.validate(draft.copy(endDate = LocalDate(2026, 4, 30))))
}
