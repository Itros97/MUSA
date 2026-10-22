package org.softwareanvil.musa.domain.logic

import kotlin.test.Test
import kotlin.test.assertEquals
import org.softwareanvil.musa.domain.model.Product
import org.softwareanvil.musa.domain.model.ProductKind
import org.softwareanvil.musa.domain.model.ProductStatus
import org.softwareanvil.musa.fakes.digitalProduct
import org.softwareanvil.musa.fakes.physicalProduct
import org.softwareanvil.musa.fakes.physicalVariant
import org.softwareanvil.musa.fakes.tag

class ProductFilterTest {

	private val pokemon = tag("Pokémon")
	private val christmas = tag("Navidad")
	private val print = physicalProduct(id = "print", name = "Lámina Pikachu", tags = listOf(pokemon, christmas))
	private val sticker = physicalProduct(
		id = "sticker",
		name = "Pegatina",
		tags = listOf(pokemon),
		variants = listOf(physicalVariant(sku = "PEG-01", stock = 1, minStock = 2)),
	)
	private val commission = digitalProduct(id = "commission", name = "Comisión busto")
	private val archived = physicalProduct(id = "old", name = "Antigua", status = ProductStatus.ARCHIVED)
	private val all = listOf(sticker, commission, print, archived)

	private fun names(query: ProductQuery): List<String> = ProductFilter.apply(all, query).map(Product::name)

	@Test
	fun `hides archived products and sorts by name ignoring accents`() =
		assertEquals(listOf("Comisión busto", "Lámina Pikachu", "Pegatina"), names(ProductQuery()))

	@Test
	fun `shows only archived products when asked`() =
		assertEquals(listOf("Antigua"), names(ProductQuery(archived = true)))

	@Test
	fun `searches without case or accents`() =
		assertEquals(listOf("Lámina Pikachu"), names(ProductQuery(text = "LAMINA")))

	@Test
	fun `searches by sku`() = assertEquals(listOf("Pegatina"), names(ProductQuery(text = "peg-01")))

	@Test
	fun `filters by kind`() =
		assertEquals(listOf("Comisión busto"), names(ProductQuery(kind = ProductKind.DIGITAL)))

	@Test
	fun `requires every selected tag`() =
		assertEquals(listOf("Lámina Pikachu"), names(ProductQuery(tagIds = setOf(pokemon.id, christmas.id))))

	@Test
	fun `keeps only low stock physical products`() =
		assertEquals(listOf("Pegatina"), names(ProductQuery(lowStockOnly = true)))
}
