package org.softwareanvil.project.data.driver

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.softwareanvil.project.db.MusaDatabase

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:musa.db")
        MusaDatabase.Schema.create(driver)
        return driver
    }
}