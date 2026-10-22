package org.softwareanvil.musa.ui.events

import kotlin.test.Test
import kotlin.test.assertEquals
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.fakes.digitalProduct
import org.softwareanvil.musa.fakes.event
import org.softwareanvil.musa.fakes.physicalProduct
import org.softwareanvil.musa.fakes.physicalVariant
import org.softwareanvil.musa.ui.events.detail.StockOption

class EventTextsTest {

	@Test
	fun `joins dates and place`() = assertEquals("18–19 abr 2026 · Pabellón 2", event().whenAndWhereLabel())

	@Test
	fun `drops an empty place`() = assertEquals("18–19 abr 2026", event().copy(location = "").whenAndWhereLabel())

	@Test
	fun `names an unnamed variant after its product`() = assertEquals("Chapa", itemLabel("Chapa", " "))

	@Test
	fun `offers only physical variants with stock`() {
		val print = physicalProduct(variants = listOf(physicalVariant("a4", "A4", stock = 2), physicalVariant("a3", "A3", stock = 0)))
		val options = StockOption.from(listOf(print, digitalProduct()))
		assertEquals(listOf(StockOption(VariantId("a4"), "Lámina gato · A4", 2)), options)
	}
}
