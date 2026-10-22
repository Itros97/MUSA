package org.softwareanvil.musa.di

import kotlin.time.Clock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.softwareanvil.musa.data.repository.ProductRepositoryImpl
import org.softwareanvil.musa.data.repository.TagRepositoryImpl
import org.softwareanvil.musa.db.MusaDatabase
import org.softwareanvil.musa.domain.logic.IdGenerator
import org.softwareanvil.musa.domain.logic.ProductValidator
import org.softwareanvil.musa.domain.logic.UuidGenerator
import org.softwareanvil.musa.domain.repository.ProductRepository
import org.softwareanvil.musa.domain.repository.TagRepository
import org.softwareanvil.musa.domain.usecase.AdjustStockUseCase
import org.softwareanvil.musa.domain.usecase.ArchiveProductUseCase
import org.softwareanvil.musa.domain.usecase.DeleteProductUseCase
import org.softwareanvil.musa.domain.usecase.DuplicateProductUseCase
import org.softwareanvil.musa.domain.usecase.GetProductUseCase
import org.softwareanvil.musa.domain.usecase.ObserveProductsUseCase
import org.softwareanvil.musa.domain.usecase.ObserveTagsUseCase
import org.softwareanvil.musa.domain.usecase.SaveProductUseCase

/** Database and repositories. */
val dataModule = module {
	single { MusaDatabase(get()) }
	single<CoroutineDispatcher> { Dispatchers.IO }
	single<ProductRepository> { ProductRepositoryImpl(get(), get()) }
	single<TagRepository> { TagRepositoryImpl(get(), get()) }
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
}

/** Starts dependency injection; platforms pass extra config such as the Android context. */
fun initKoin(config: KoinAppDeclaration = {}) {
	startKoin {
		config()
		modules(platformModule, dataModule, domainModule, uiModule)
	}
}
