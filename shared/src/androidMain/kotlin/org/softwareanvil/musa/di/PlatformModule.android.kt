package org.softwareanvil.musa.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.softwareanvil.musa.data.db.DATABASE_NAME
import org.softwareanvil.musa.db.MusaDatabase

actual val platformModule = module {
	single<SqlDriver> { AndroidSqliteDriver(MusaDatabase.Schema, androidContext(), DATABASE_NAME) }
}
