package org.softwareanvil.musa.domain.repository

import kotlin.time.Instant
import org.softwareanvil.musa.domain.model.Sale
import org.softwareanvil.musa.domain.model.SaleId

/** Storage for sales of every channel; deleted sales are never returned. */
interface SaleRepository {

	/** Stores a new sale. */
	suspend fun record(sale: Sale)

	/** Returns the sale, or null if it does not exist or was deleted. */
	suspend fun findById(id: SaleId): Sale?

	/** Soft-deletes the sale so a future sync can propagate it. */
	suspend fun delete(id: SaleId, at: Instant)
}
