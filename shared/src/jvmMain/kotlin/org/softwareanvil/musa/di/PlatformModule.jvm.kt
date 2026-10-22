package org.softwareanvil.musa.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File
import java.util.Properties
import org.koin.dsl.module
import org.softwareanvil.musa.data.db.DATABASE_NAME
import org.softwareanvil.musa.data.db.DEMO_DATABASE_NAME
import org.softwareanvil.musa.data.db.DemoMode
import org.softwareanvil.musa.data.db.DesktopDataDirectory
import org.softwareanvil.musa.db.MusaDatabase

actual val platformModule = module {
	single<SqlDriver> {
		val file = File(DesktopDataDirectory.resolve(), if (DemoMode.isEnabled) DEMO_DATABASE_NAME else DATABASE_NAME)
		// The demo database is rebuilt on every start, so it always matches the current schema.
		if (DemoMode.isEnabled) check(!file.exists() || file.delete()) { "Cannot reset demo database $file" }
		JdbcSqliteDriver("jdbc:sqlite:${file.absolutePath}", Properties(), MusaDatabase.Schema)
	}
}
