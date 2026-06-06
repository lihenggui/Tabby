package com.github.kr328.clash.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

class AndroidDatabaseDriverFactory(private val context: Context) : DatabaseDriverFactory {
  override fun createDriver(): SqlDriver {
    return AndroidSqliteDriver(TabbyDatabase.Schema, context, DATABASE_NAME)
  }
}

private const val DATABASE_NAME = "tabby.db"
