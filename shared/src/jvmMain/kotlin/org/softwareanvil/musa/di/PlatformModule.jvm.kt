package org.softwareanvil.musa.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File
import java.util.Properties
import org.koin.dsl.module
import org.softwareanvil.musa.data.db.DATABASE_NAME
import org.softwareanvil.musa.data.db.DesktopDataDirectory
import org.softwareanvil.musa.db.MusaDatabase

actual val platformModule = module {
	single<SqlDriver> {
		val file = File(DesktopDataDirectory.resolve(), DATABASE_NAME)
		JdbcSqliteDriver("jdbc:sqlite:${file.absolutePath}", Properties(), MusaDatabase.Schema)
	}
}
