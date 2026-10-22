package org.softwareanvil.musa.ui.products

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.PhysicalDetails
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductKind
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.fakes.physicalProduct
import org.softwareanvil.musa.fakes.physicalVariant
import org.softwareanvil.musa.fakes.tag
import org.softwareanvil.musa.ui.products.edit.FormField
import org.softwareanvil.musa.ui.products.edit.ProductForm
import org.softwareanvil.musa.ui.products.edit.ProductFormMapper
import org.softwareanvil.musa.ui.products.edit.ProductFormReducer
import org.softwareanvil.musa.ui.products.edit.VariantField
import org.softwareanvil.musa.ui.products.edit.VariantForm

class ProductFormMapperTest {

	private val variantId = VariantId("v1")

	private fun form(vararg values: Pair<VariantField, String>) = ProductForm(
		id = ProductId("p1"),
		kind = ProductKind.PHYSICAL,
		name = "Lámina",
		variants = listOf(VariantForm(variantId, mapOf(*values))),
	)

	@Test
	fun `turns a stored product into a draft with the same data`() {
		val product = physicalProduct(variants = listOf(physicalVariant(price = 1250).copy(widthMm = 297)))
		val parsed = ProductFormMapper.parse(ProductFormMapper.fromProduct(product))
		assertEquals(emptyMap(), parsed.errors)
		assertEquals(product.variants, parsed.draft.details.variants)
	}

	@Test
	fun `parses euros and centimetres`() {
		val parsed = ProductFormMapper.parse(
			form(VariantField.PRICE to "12,5", VariantField.COST to "3", VariantField.STOCK to "4", VariantField.WIDTH to "29,7"),
		)
		assertEquals(emptyMap(), parsed.errors)
		val variant = assertIs<PhysicalDetails>(parsed.draft.details).variants.single()
		assertEquals(Money(1250), variant.price)
		assertEquals(297, variant.widthMm)
		assertEquals(null, variant.minStock)
	}

	@Test
	fun `reports every unreadable field at once`() {
		val errors = ProductFormMapper.parse(form(VariantField.PRICE to "abc", VariantField.STOCK to "1,5", VariantField.WEIGHT to "0")).errors
		assertEquals(
			setOf(VariantField.PRICE, VariantField.COST, VariantField.STOCK, VariantField.WEIGHT).map { FormField.Variant(variantId, it) }.toSet(),
			errors.keys,
		)
	}
}

class ProductFormReducerTest {

	private val form = ProductForm(
		id = ProductId("p1"),
		kind = ProductKind.PHYSICAL,
		variants = listOf(VariantForm(VariantId("v1")).with(VariantField.PRICE, "10").with(VariantField.COST, "2")),
	)

	@Test
	fun `adds tags with the spelling of an existing tag`() =
		assertEquals(listOf("Pokémon"), ProductFormReducer.addTag(form, " pokemon", listOf(tag("Pokémon"))).tagNames)

	@Test
	fun `ignores a tag already selected`() {
		val once = ProductFormReducer.addTag(form, "Gatos", emptyList())
		assertEquals(listOf("Gatos"), ProductFormReducer.addTag(once, "GATOS", emptyList()).tagNames)
	}

	@Test
	fun `steps stock without going below zero`() =
		assertEquals("0", ProductFormReducer.stepStock(form, VariantId("v1"), -1).variants.single()[VariantField.STOCK])

	@Test
	fun `new variants copy the first price and cost`() {
		val variant = ProductFormReducer.addVariant(form, VariantId("v2")).variants.last()
		assertEquals("10", variant[VariantField.PRICE])
		assertEquals("2", variant[VariantField.COST])
	}

	@Test
	fun `keeps the last variant`() = assertEquals(form, ProductFormReducer.removeVariant(form, VariantId("v1")))
}
