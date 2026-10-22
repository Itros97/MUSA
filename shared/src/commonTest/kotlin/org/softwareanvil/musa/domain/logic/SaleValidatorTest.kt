package org.softwareanvil.musa.domain.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.SaleChannel
import org.softwareanvil.musa.domain.model.SaleDraft
import org.softwareanvil.musa.domain.model.SaleLineDraft
import org.softwareanvil.musa.domain.model.SaleLineId
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.fakes.EPOCH

class SaleValidatorTest {

	private val validator = SaleValidator()
	private val line = SaleLineDraft(SaleLineId("l1"), VariantId("v1"), quantity = 1, unitPrice = Money(1000))
	private val draft = SaleDraft(SaleChannel.ONLINE_SHOP, "", "", listOf(line), Money(450), Money(380), EPOCH)

	@Test
	fun `accepts a sale with lines`() = assertEquals(emptyList(), validator.validate(draft))

	@Test
	fun `rejects a sale without lines`() = assertEquals(listOf(SaleError.NoLines), validator.validate(draft.copy(lines = emptyList())))

	@Test
	fun `rejects the event channel`() =
		assertEquals(listOf(SaleError.EventChannel), validator.validate(draft.copy(channel = SaleChannel.EVENT)))

	@Test
	fun `rejects negative shipping`() =
		assertEquals(listOf(SaleError.NegativeShipping), validator.validate(draft.copy(shippingCost = Money(-1))))

	@Test
	fun `points at the line with a bad quantity or price`() {
		val bad = line.copy(quantity = 0, unitPrice = Money(-1))
		assertEquals(
			listOf(SaleError.InvalidQuantity(line.id), SaleError.NegativePrice(line.id)),
			validator.validate(draft.copy(lines = listOf(bad))),
		)
	}
}
