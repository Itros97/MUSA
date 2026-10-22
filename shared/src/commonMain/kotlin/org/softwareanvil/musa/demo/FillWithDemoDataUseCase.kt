package org.softwareanvil.musa.demo

import org.softwareanvil.musa.domain.repository.DatabaseMaintenance

/** Outcome of filling the database with toy data. */
enum class FillResult { FILLED, NOT_EMPTY }

/** Loads the demo data, but only into an empty database so SKUs never clash with existing rows. */
class FillWithDemoDataUseCase(private val maintenance: DatabaseMaintenance, private val seeder: DemoSeeder) {

	suspend operator fun invoke(): FillResult {
		if (!maintenance.isEmpty()) return FillResult.NOT_EMPTY
		seeder.seed()
		return FillResult.FILLED
	}
}
