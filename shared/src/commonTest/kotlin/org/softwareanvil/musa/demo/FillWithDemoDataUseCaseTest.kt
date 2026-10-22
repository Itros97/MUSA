package org.softwareanvil.musa.demo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.softwareanvil.musa.domain.usecase.WipeDatabaseUseCase

class FillWithDemoDataUseCaseTest {

	private val bed = DemoTestBed()
	private val fill = FillWithDemoDataUseCase(bed.maintenance, bed.seeder)

	@Test
	fun `fills an empty database`() = runTest {
		assertEquals(FillResult.FILLED, fill())
		assertEquals(10, bed.products.products.value.size)
	}

	@Test
	fun `refuses to fill a database that already has data`() = runTest {
		fill()
		assertEquals(FillResult.NOT_EMPTY, fill())
		assertEquals(10, bed.products.products.value.size)
	}

	@Test
	fun `can fill again after wiping`() = runTest {
		fill()
		WipeDatabaseUseCase(bed.maintenance)()
		assertEquals(FillResult.FILLED, fill())
	}
}
