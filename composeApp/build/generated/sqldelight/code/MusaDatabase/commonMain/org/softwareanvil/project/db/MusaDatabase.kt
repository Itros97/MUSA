package org.softwareanvil.project.db

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import kotlin.Unit
import org.softwareanvil.project.db.composeApp.newInstance
import org.softwareanvil.project.db.composeApp.schema

public interface MusaDatabase : Transacter {
  public val commissionQueries: CommissionQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.Value<Unit>>
      get() = MusaDatabase::class.schema

    public operator fun invoke(driver: SqlDriver): MusaDatabase = MusaDatabase::class.newInstance(driver)
  }
}
