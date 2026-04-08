package org.softwareanvil.project.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.softwareanvil.project.data.repository.CommissionRepositoryImpl
import org.softwareanvil.project.domain.repository.CommissionRepository
import org.softwareanvil.project.domain.usecase.CreateCommissionUseCase
import org.softwareanvil.project.domain.usecase.UpdateCommissionUseCase
import org.softwareanvil.project.domain.usecase.UpdateStatusUseCase
import org.softwareanvil.project.domain.usecase.DeleteCommissionUseCase
import org.softwareanvil.project.domain.usecase.GetCommissionsUseCase
import org.softwareanvil.project.presentation.commissionlist.CommissionListViewModel
import org.softwareanvil.project.presentation.commissionform.CommissionFormViewModel
import org.softwareanvil.project.presentation.commissiondetail.CommissionDetailViewModel
import org.softwareanvil.project.presentation.dashboard.DashboardViewModel

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

val presentationModule = module {
    viewModelOf(::CommissionListViewModel)
    viewModelOf(::CommissionFormViewModel)
    viewModelOf(::CommissionDetailViewModel)
    viewModelOf(::DashboardViewModel)
}