package org.softwareanvil.project.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import org.softwareanvil.project.data.repository.CommissionRepositoryImpl
import org.softwareanvil.project.domain.repository.CommissionRepository
import org.softwareanvil.project.domain.usecase.*

val dataModule = module {
    single<CommissionRepository> { CommissionRepositoryImpl(get()) }
}

val domainModule = module {
    factoryOf(::CreateCommissionUseCase)
    factoryOf(::UpdateCommissionUseCase)
    factoryOf(::UpdateStatusUseCase)
    factoryOf(::DeleteCommissionUseCase)
    factoryOf(::GetCommissionsUseCase)
}