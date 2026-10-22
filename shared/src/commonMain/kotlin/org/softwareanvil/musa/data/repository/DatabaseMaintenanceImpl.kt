package org.softwareanvil.musa.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.softwareanvil.musa.db.MusaDatabase
import org.softwareanvil.musa.domain.repository.DatabaseMaintenance

/** SQLDelight-backed [DatabaseMaintenance]; the wipe notifies every observer of the cleared tables. */
class DatabaseMaintenanceImpl(
	private val database: MusaDatabase,
	private val dispatcher: CoroutineDispatcher,
) : DatabaseMaintenance {

	override suspend fun isEmpty(): Boolean = withContext(dispatcher) {
		database.productQueries.countAll().executeAsOne() == 0L && database.eventQueries.countAll().executeAsOne() == 0L
	}

	override suspend fun wipeAll() = withContext(dispatcher) {
		database.transaction {
			with(database) {
				saleLineQueries.deleteAll()
				saleQueries.deleteAll()
				eventExpenseQueries.deleteAll()
				eventStockQueries.deleteAll()
				eventQueries.deleteAll()
				productTagQueries.deleteAll()
				tagQueries.deleteAll()
				physicalVariantQueries.deleteAll()
				productVariantQueries.deleteAll()
				physicalProductQueries.deleteAll()
				digitalProductQueries.deleteAll()
				productQueries.deleteAll()
			}
		}
	}
}
