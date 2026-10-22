package org.softwareanvil.musa.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.softwareanvil.musa.domain.logic.ProductQuery
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductStatus
import org.softwareanvil.musa.domain.model.VariantId
import org.softwareanvil.musa.fakes.FakeProductRepository
import org.softwareanvil.musa.fakes.FixedClock
import org.softwareanvil.musa.fakes.SequentialIds
import org.softwareanvil.musa.fakes.physicalProduct
import org.softwareanvil.musa.fakes.physicalVariant

class ProductActionsTest {

	private val original = physicalProduct(variants = listOf(physicalVariant(sku = "LAM-1", stock = 7)))
	private val repository = FakeProductRepository(listOf(original))
	private val clock = FixedClock()

	@Test
	fun `duplicates as a draft without stock or sku`() = runTest {
		val copyId = DuplicateProductUseCase(repository, clock, SequentialIds("copy"))(original.id, "Copia de Lámina gato")
		val copy = repository.findById(copyId) as PhysicalProduct
		assertEquals("Copia de Lámina gato", copy.name)
		assertEquals(ProductStatus.DRAFT, copy.status)
		assertEquals(0, copy.totalStock)
		assertNull(copy.variants.single().sku)
		assertEquals(7, (repository.findById(original.id) as PhysicalProduct).totalStock)
	}

	@Test
	fun `archives and restores a product`() = runTest {
		val archive = ArchiveProductUseCase(repository, clock)
		archive(original.id, archived = true)
		assertEquals(ProductStatus.ARCHIVED, repository.findById(original.id)?.status)
		archive(original.id, archived = false)
		assertEquals(ProductStatus.ACTIVE, repository.findById(original.id)?.status)
	}

	@Test
	fun `deletes a product`() = runTest {
		DeleteProductUseCase(repository, clock)(original.id)
		assertNull(repository.findById(original.id))
	}

	@Test
	fun `observes the filtered catalogue and whether it is empty`() = runTest {
		val query = MutableStateFlow(ProductQuery(text = "nada"))
		val result = ObserveProductsUseCase(repository)(query).first()
		assertEquals(emptyList(), result.products)
		assertEquals(false, result.isCatalogueEmpty)
	}

	@Test
	fun `adjusts stock of a variant`() = runTest {
		AdjustStockUseCase(repository, clock)(VariantId("v1"), delta = -2)
		assertEquals(5, (repository.findById(ProductId("p1")) as PhysicalProduct).totalStock)
	}
}
