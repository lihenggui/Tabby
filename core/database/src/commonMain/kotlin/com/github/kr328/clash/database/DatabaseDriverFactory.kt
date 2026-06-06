package com.github.kr328.clash.database

import app.cash.sqldelight.db.SqlDriver

interface DatabaseDriverFactory {
  fun createDriver(): SqlDriver
}

fun createTabbyDatabase(driverFactory: DatabaseDriverFactory): TabbyDatabase {
  return TabbyDatabase(driverFactory.createDriver())
}
