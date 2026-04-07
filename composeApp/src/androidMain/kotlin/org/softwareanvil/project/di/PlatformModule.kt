package org.softwareanvil.project.di

import org.koin.dsl.module
import org.softwareanvil.project.data.driver.DriverFactory
import org.softwareanvil.project.db.MusaDatabase

val platformModule = module {
    single { DriverFactory(get()).createDriver() }
    single { MusaDatabase(get()) }
}