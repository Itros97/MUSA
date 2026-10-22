package org.softwareanvil.musa.domain.repository

import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import org.softwareanvil.musa.domain.model.Sale
import org.softwareanvil.musa.domain.model.SaleId

/**
 * Storage for sales of every channel; deleted sales are never returned.
 * Sales outside events move catalogue stock: recording takes their physical units, deleting gives them back.
 */
interface SaleRepository {

	/** Emits every sale with its lines, newest first, each time any changes. */
	fun observeAll(): Flow<List<Sale>>

	/** Stores a new sale and, outside events, takes its physical units from the catalogue in the same transaction. */
	suspend fun record(sale: Sale)

	/** Returns the sale, or null if it does not exist or was deleted. */
	suspend fun findById(id: SaleId): Sale?

	/** Soft-deletes the sale and, outside events, gives its physical units back in the same transaction. */
	suspend fun delete(id: SaleId, at: Instant)
}
