package org.softwareanvil.musa.domain.repository

/** Whole-database operations for development tools; never used by regular features. */
interface DatabaseMaintenance {

	/** True when there are no products and no events, deleted ones included. */
	suspend fun isEmpty(): Boolean

	/** Removes every row of every table in one transaction; this cannot be undone. */
	suspend fun wipeAll()
}
