package org.softwareanvil.musa.data.repository

import kotlin.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.softwareanvil.musa.data.mapper.EventMapper
import org.softwareanvil.musa.db.MusaDatabase
import org.softwareanvil.musa.domain.model.Sale
import org.softwareanvil.musa.domain.model.SaleId
import org.softwareanvil.musa.domain.repository.SaleRepository

/** SQLDelight-backed [SaleRepository]. */
class SaleRepositoryImpl(
	private val database: MusaDatabase,
	private val dispatcher: CoroutineDispatcher,
) : SaleRepository {

	override suspend fun record(sale: Sale) = withContext(dispatcher) {
		database.saleQueries.insert(
			id = sale.id.value,
			variant_id = sale.variantId.value,
			product_name = sale.productName,
			variant_name = sale.variantName,
			quantity = sale.quantity.toLong(),
			unit_price_cents = sale.unitPrice.cents,
			unit_cost_cents = sale.unitCost.cents,
			channel = sale.channel.name,
			event_id = sale.eventId?.value,
			sold_at = sale.soldAt.toEpochMilliseconds(),
			updated_at = sale.soldAt.toEpochMilliseconds(),
		)
		Unit
	}

	override suspend fun findById(id: SaleId): Sale? = withContext(dispatcher) {
		database.saleQueries.selectById(id.value).executeAsOneOrNull()?.let(EventMapper::toSale)
	}

	override suspend fun delete(id: SaleId, at: Instant) = withContext(dispatcher) {
		database.saleQueries.softDelete(at = at.toEpochMilliseconds(), id = id.value)
		Unit
	}
}
