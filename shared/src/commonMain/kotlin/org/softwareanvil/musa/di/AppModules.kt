package org.softwareanvil.musa.di

import kotlin.time.Clock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import org.softwareanvil.musa.data.repository.DatabaseMaintenanceImpl
import org.softwareanvil.musa.data.repository.EventRepositoryImpl
import org.softwareanvil.musa.data.repository.ProductRepositoryImpl
import org.softwareanvil.musa.data.repository.SaleRepositoryImpl
import org.softwareanvil.musa.data.repository.TagRepositoryImpl
import org.softwareanvil.musa.db.MusaDatabase
import org.softwareanvil.musa.demo.DemoSeeder
import org.softwareanvil.musa.demo.FillWithDemoDataUseCase
import org.softwareanvil.musa.domain.logic.EventValidator
import org.softwareanvil.musa.domain.logic.IdGenerator
import org.softwareanvil.musa.domain.logic.ProductValidator
import org.softwareanvil.musa.domain.logic.SaleValidator
import org.softwareanvil.musa.domain.logic.UuidGenerator
import org.softwareanvil.musa.domain.repository.DatabaseMaintenance
import org.softwareanvil.musa.domain.repository.EventRepository
import org.softwareanvil.musa.domain.repository.ProductRepository
import org.softwareanvil.musa.domain.repository.SaleRepository
import org.softwareanvil.musa.domain.repository.TagRepository
import org.softwareanvil.musa.domain.usecase.AddExpenseUseCase
import org.softwareanvil.musa.domain.usecase.AdjustStockUseCase
import org.softwareanvil.musa.domain.usecase.ArchiveProductUseCase
import org.softwareanvil.musa.domain.usecase.CloseEventUseCase
import org.softwareanvil.musa.domain.usecase.DeleteEventUseCase
import org.softwareanvil.musa.domain.usecase.DeleteExpenseUseCase
import org.softwareanvil.musa.domain.usecase.DeleteProductUseCase
import org.softwareanvil.musa.domain.usecase.DeleteSaleUseCase
import org.softwareanvil.musa.domain.usecase.DuplicateProductUseCase
import org.softwareanvil.musa.domain.usecase.GetEventUseCase
import org.softwareanvil.musa.domain.usecase.GetProductUseCase
import org.softwareanvil.musa.domain.usecase.MoveStockToEventUseCase
import org.softwareanvil.musa.domain.usecase.ObserveEventDetailUseCase
import org.softwareanvil.musa.domain.usecase.ObserveEventsUseCase
import org.softwareanvil.musa.domain.usecase.ObserveProductsUseCase
import org.softwareanvil.musa.domain.usecase.ObserveSalesUseCase
import org.softwareanvil.musa.domain.usecase.ObserveTagsUseCase
import org.softwareanvil.musa.domain.usecase.RecordEventSaleUseCase
import org.softwareanvil.musa.domain.usecase.RecordSaleUseCase
import org.softwareanvil.musa.domain.usecase.ReturnStockFromEventUseCase
import org.softwareanvil.musa.domain.usecase.SaveEventUseCase
import org.softwareanvil.musa.domain.usecase.SaveProductUseCase
import org.softwareanvil.musa.domain.usecase.UndoSaleUseCase
import org.softwareanvil.musa.domain.usecase.WipeDatabaseUseCase

/** Database and repositories. */
val dataModule = module {
	single { MusaDatabase(get()) }
	single<CoroutineDispatcher> { Dispatchers.IO }
	single<ProductRepository> { ProductRepositoryImpl(get(), get()) }
	single<TagRepository> { TagRepositoryImpl(get(), get()) }
	single<EventRepository> { EventRepositoryImpl(get(), get()) }
	single<SaleRepository> { SaleRepositoryImpl(get(), get()) }
	single<DatabaseMaintenance> { DatabaseMaintenanceImpl(get(), get()) }
}

/** Pure logic and use cases. */
val domainModule = module {
	single<Clock> { Clock.System }
	single<IdGenerator> { UuidGenerator() }
	single { ProductValidator() }
	factory { SaveProductUseCase(get(), get(), get(), get(), get()) }
	factory { DuplicateProductUseCase(get(), get(), get()) }
	factory { AdjustStockUseCase(get(), get()) }
	factory { ArchiveProductUseCase(get(), get()) }
	factory { DeleteProductUseCase(get(), get()) }
	factory { ObserveProductsUseCase(get()) }
	factory { GetProductUseCase(get()) }
	factory { ObserveTagsUseCase(get()) }
	single { EventValidator() }
	factory { SaveEventUseCase(get(), get(), get()) }
	factory { MoveStockToEventUseCase(get(), get(), get()) }
	factory { ReturnStockFromEventUseCase(get(), get()) }
	factory { RecordEventSaleUseCase(get(), get(), get(), get()) }
	factory { UndoSaleUseCase(get(), get(), get()) }
	factory { AddExpenseUseCase(get(), get(), get()) }
	factory { DeleteExpenseUseCase(get(), get()) }
	factory { CloseEventUseCase(get(), get()) }
	factory { DeleteEventUseCase(get(), get()) }
	factory { ObserveEventsUseCase(get()) }
	factory { ObserveEventDetailUseCase(get()) }
	factory { GetEventUseCase(get()) }
	factory { WipeDatabaseUseCase(get()) }
	single { SaleValidator() }
	factory { RecordSaleUseCase(get(), get(), get(), get()) }
	factory { DeleteSaleUseCase(get(), get()) }
	factory { ObserveSalesUseCase(get()) }
}

/** Toy data for the demo database and the development tools. */
val demoModule = module {
	factory { DemoSeeder(get(), get(), get(), get(), get(), get(), get(), get()) }
	factory { FillWithDemoDataUseCase(get(), get()) }
}

/** Starts dependency injection; platforms say whether to show [devTools] and pass extra config such as the Android context. */
fun initKoin(devTools: Boolean = false, config: KoinAppDeclaration = {}) {
	startKoin {
		config()
		modules(module { single { AppConfig(devTools) } }, platformModule, dataModule, domainModule, uiModule, demoModule)
	}
}

/** Fills a fresh demo database with toy data; call once, after [initKoin]. */
suspend fun seedDemoData() = KoinPlatform.getKoin().get<DemoSeeder>().seed()
