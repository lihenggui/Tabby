package com.github.kr328.clash.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

class DesktopDatabaseDriverFactory(private val path: String) : DatabaseDriverFactory {
  override fun createDriver(): SqlDriver {
    return JdbcSqliteDriver("jdbc:sqlite:$path").also { driver ->
      TabbyDatabase.Schema.create(driver)
    }
  }
}
