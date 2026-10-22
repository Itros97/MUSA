package org.softwareanvil.musa.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.test.runTest
import org.softwareanvil.musa.domain.logic.ProductValidator
import org.softwareanvil.musa.domain.logic.ValidationError
import org.softwareanvil.musa.domain.model.PhysicalDetails
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.ProductDraft
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductStatus
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.fakes.EPOCH
import org.softwareanvil.musa.fakes.FakeProductRepository
import org.softwareanvil.musa.fakes.FakeTagRepository
import org.softwareanvil.musa.fakes.FixedClock
import org.softwareanvil.musa.fakes.SequentialIds
import org.softwareanvil.musa.fakes.physicalProduct
import org.softwareanvil.musa.fakes.physicalVariant
import org.softwareanvil.musa.fakes.tag

class SaveProductUseCaseTest {

	private val products = FakeProductRepository()
	private val tags = FakeTagRepository(listOf(tag("Pokémon")))
	private val clock = FixedClock()
	private val save = SaveProductUseCase(products, tags, ProductValidator(), clock, SequentialIds("tag"))

	private fun draft(
		id: String = "p1",
		name: String = "Lámina",
		tagNames: List<String> = emptyList(),
		sku: String? = null,
	) = ProductDraft(
		id = ProductId(id),
		name = name,
		description = "",
		tagNames = tagNames,
		status = ProductStatus.ACTIVE,
		details = PhysicalDetails(material = "Papel", variants = listOf(physicalVariant(sku = sku))),
	)

	@Test
	fun `stores a new product with creation time`() = runTest {
		val result = assertIs<SaveResult.Saved>(save(draft()))
		assertEquals(EPOCH, result.product.createdAt)
		assertEquals(result.product, products.findById(ProductId("p1")))
	}

	@Test
	fun `keeps creation time and refreshes update time when editing`() = runTest {
		save(draft())
		clock.now = EPOCH + 1.days
		val saved = assertIs<SaveResult.Saved>(save(draft(name = "Lámina nueva"))).product
		assertEquals(EPOCH, saved.createdAt)
		assertEquals(EPOCH + 1.days, saved.updatedAt)
	}

	@Test
	fun `trims name and empties blank sku`() = runTest {
		val saved = assertIs<SaveResult.Saved>(save(draft(name = "  Lámina  ", sku = "  "))).product
		assertEquals("Lámina", saved.name)
		assertEquals(null, (saved as PhysicalProduct).variants.single().sku)
	}

	@Test
	fun `reuses existing tags and creates new ones once`() = runTest {
		val saved = assertIs<SaveResult.Saved>(save(draft(tagNames = listOf("pokemon", "Navidad", "navidad ")))).product
		assertEquals(listOf("Pokémon", "Navidad"), saved.tags.map { it.name })
		assertEquals(2, tags.tags.value.size)
	}

	@Test
	fun `rejects a sku used by another product`() = runTest {
		products.save(physicalProduct(id = "other", name = "Pegatina", variants = listOf(physicalVariant(sku = "LAM-1"))))
		val result = assertIs<SaveResult.Invalid>(save(draft(sku = "LAM-1")))
		assertEquals(listOf(ValidationError.DuplicateSku(VariantId("v1"), "Pegatina")), result.errors)
	}

	@Test
	fun `does not store invalid drafts`() = runTest {
		assertIs<SaveResult.Invalid>(save(draft(name = "")))
		assertEquals(null, products.findById(ProductId("p1")))
	}
}
