package org.softwareanvil.musa.domain.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.softwareanvil.musa.domain.model.DigitalDetails
import org.softwareanvil.musa.domain.model.PhysicalDetails
import org.softwareanvil.musa.domain.model.PhysicalVariant
import org.softwareanvil.musa.domain.model.ProductDetails
import org.softwareanvil.musa.domain.model.ProductDraft
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductStatus
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.fakes.digitalVariant
import org.softwareanvil.musa.fakes.physicalVariant

class ProductValidatorTest {

	private val validator = ProductValidator()

	private fun draft(name: String = "Lámina", details: ProductDetails = physical()) =
		ProductDraft(ProductId("p1"), name, "", emptyList(), ProductStatus.ACTIVE, details)

	private fun physical(vararg variants: PhysicalVariant = arrayOf(physicalVariant())) =
		PhysicalDetails(material = "", variants = variants.toList())

	@Test
	fun `accepts a single unnamed variant`() = assertEquals(emptyList(), validator.validate(draft()))

	@Test
	fun `requires a name`() = assertEquals(listOf(ValidationError.BlankName), validator.validate(draft(name = "  ")))

	@Test
	fun `requires at least one variant`() =
		assertEquals(listOf(ValidationError.NoVariants), validator.validate(draft(details = physical(*emptyArray()))))

	@Test
	fun `requires names when there are several variants`() {
		val errors = validator.validate(draft(details = physical(physicalVariant("a", "A4"), physicalVariant("b", " "))))
		assertEquals(listOf(ValidationError.BlankVariantName(VariantId("b"))), errors)
	}

	@Test
	fun `rejects repeated variant names ignoring case`() {
		val errors = validator.validate(draft(details = physical(physicalVariant("a", "A4"), physicalVariant("b", "a4"))))
		assertEquals(listOf(ValidationError.DuplicateVariantName(VariantId("b"))), errors)
	}

	@Test
	fun `rejects negative stock`() =
		assertEquals(
			listOf(ValidationError.NegativeStock(VariantId("v1"))),
			validator.validate(draft(details = physical(physicalVariant(stock = -1)))),
		)

	@Test
	fun `rejects zero measures`() {
		val variant = physicalVariant().copy(weightGrams = 0)
		assertEquals(listOf(ValidationError.InvalidMeasure(VariantId("v1"))), validator.validate(draft(details = physical(variant))))
	}

	@Test
	fun `rejects a sku repeated inside the product`() {
		val errors = validator.validate(
			draft(details = physical(physicalVariant("a", "A4", sku = "LAM"), physicalVariant("b", "A3", sku = "lam"))),
		)
		assertEquals(listOf(ValidationError.DuplicateSku(VariantId("b"), ownerName = null)), errors)
	}

	@Test
	fun `rejects negative turnaround`() {
		val details = DigitalDetails(deliveryFormat = "PNG", turnaroundDays = -2, variants = listOf(digitalVariant()))
		assertTrue(ValidationError.NegativeTurnaround in validator.validate(draft(details = details)))
	}
}
