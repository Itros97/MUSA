package org.softwareanvil.musa.domain.usecase

import org.softwareanvil.musa.domain.repository.DatabaseMaintenance

/** Empties the whole database; only offered by the development tools. */
class WipeDatabaseUseCase(private val maintenance: DatabaseMaintenance) {
	suspend operator fun invoke() = maintenance.wipeAll()
}
