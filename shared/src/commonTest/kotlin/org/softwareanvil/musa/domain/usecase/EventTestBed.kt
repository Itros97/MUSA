package org.softwareanvil.musa.domain.usecase

import org.softwareanvil.musa.domain.model.Event
import org.softwareanvil.musa.domain.model.PhysicalProduct
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.fakes.FakeEventRepository
import org.softwareanvil.musa.fakes.FakeProductRepository
import org.softwareanvil.musa.fakes.FakeSaleRepository
import org.softwareanvil.musa.fakes.FixedClock
import org.softwareanvil.musa.fakes.SequentialIds
import org.softwareanvil.musa.fakes.event
import org.softwareanvil.musa.fakes.physicalProduct
import org.softwareanvil.musa.fakes.physicalVariant

/** A catalogue with one print (variant v1, 10 units at 10 € costing 3 €) and one open event e1. */
class EventTestBed(vararg events: Event = arrayOf(event())) {

	val clock = FixedClock()
	val ids = SequentialIds("new")
	val products = FakeProductRepository(listOf(physicalProduct(variants = listOf(physicalVariant(stock = 10)))))
	val sales = FakeSaleRepository()
	val events = FakeEventRepository(products, sales, events.toList())

	val moveStock = MoveStockToEventUseCase(this.events, products, clock)
	val returnStock = ReturnStockFromEventUseCase(this.events, clock)
	val recordSale = RecordEventSaleUseCase(this.events, sales, clock, ids)
	val undoSale = UndoSaleUseCase(sales, this.events, clock)
	val close = CloseEventUseCase(this.events, clock)
	val delete = DeleteEventUseCase(this.events, clock)

	suspend fun catalogueStock(): Int = (products.findById(ProductId("p1")) as PhysicalProduct).totalStock
}
