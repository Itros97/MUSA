package org.softwareanvil.project.data.driver

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.softwareanvil.project.db.MusaDatabase

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(MusaDatabase.Schema, "musa.db")
    }
}