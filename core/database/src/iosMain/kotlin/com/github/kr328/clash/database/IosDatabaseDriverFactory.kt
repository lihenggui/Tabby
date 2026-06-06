package com.github.kr328.clash.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

class IosDatabaseDriverFactory(private val name: String = DATABASE_NAME) : DatabaseDriverFactory {
  override fun createDriver(): SqlDriver {
    return NativeSqliteDriver(TabbyDatabase.Schema, name)
  }
}

private const val DATABASE_NAME = "tabby.db"
