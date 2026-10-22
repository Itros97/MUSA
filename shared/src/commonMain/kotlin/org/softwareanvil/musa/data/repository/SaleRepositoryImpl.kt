package org.softwareanvil.musa.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.softwareanvil.musa.db.MusaDatabase
import org.softwareanvil.musa.domain.model.Sale
import org.softwareanvil.musa.domain.model.SaleId
import org.softwareanvil.musa.domain.repository.SaleRepository

/** SQLDelight-backed [SaleRepository]; header, lines and stock change in one transaction. */
class SaleRepositoryImpl(
	private val database: MusaDatabase,
	private val dispatcher: CoroutineDispatcher,
) : SaleRepository {

	private val store = SaleRowStore(database)

	// Lines are written in the same transaction as their header, so watching the header is enough.
	override fun observeAll(): Flow<List<Sale>> = database.saleQueries.selectAll()
		.asFlow()
		.mapToList(dispatcher)
		.map { rows -> withContext(dispatcher) { store.withLines(rows) } }

	override suspend fun record(sale: Sale) = withContext(dispatcher) {
		database.transaction { store.insert(sale) }
	}

	override suspend fun findById(id: SaleId): Sale? = withContext(dispatcher) {
		database.saleQueries.selectById(id.value).executeAsOneOrNull()?.let { store.withLines(listOf(it)).single() }
	}

	override suspend fun delete(id: SaleId, at: Instant) = withContext(dispatcher) {
		database.transaction {
			val row = checkNotNull(database.saleQueries.selectById(id.value).executeAsOneOrNull()) { "Cannot delete missing sale ${id.value}" }
			store.delete(store.withLines(listOf(row)).single(), at.toEpochMilliseconds())
		}
	}
}
