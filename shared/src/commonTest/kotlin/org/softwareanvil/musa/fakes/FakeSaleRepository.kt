package org.softwareanvil.musa.fakes

import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.softwareanvil.musa.domain.model.Sale
import org.softwareanvil.musa.domain.model.SaleId
import org.softwareanvil.musa.domain.repository.SaleRepository

/** Keeps sales in memory; like the real one, sales outside events move physical stock through [products]. */
class FakeSaleRepository(private val products: FakeProductRepository) : SaleRepository {

	val sales = MutableStateFlow(emptyList<Sale>())

	override fun observeAll(): Flow<List<Sale>> = sales.map { all -> all.sortedByDescending { it.soldAt } }

	override suspend fun record(sale: Sale) {
		sales.update { it + sale }
		moveCatalogueStock(sale, direction = -1, sale.soldAt)
	}

	override suspend fun findById(id: SaleId) = sales.value.firstOrNull { it.id == id }

	override suspend fun delete(id: SaleId, at: Instant) {
		val sale = findById(id) ?: return
		sales.update { all -> all.filterNot { it.id == id } }
		moveCatalogueStock(sale, direction = 1, at)
	}

	private suspend fun moveCatalogueStock(sale: Sale, direction: Int, at: Instant) {
		if (sale.eventId != null) return
		sale.lines
			.filter { products.findStock(it.variantId) != null }
			.forEach { products.adjustStock(it.variantId, direction * it.quantity, at) }
	}
}
