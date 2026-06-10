package com.github.kr328.clash.settingsstore

import com.github.kr328.clash.common.store.StoreProvider

class StoreProviderMigration(
  private val source: StoreProvider,
  private val destination: StoreProvider,
  private val migratedKey: String,
) {
  fun migrate(block: StoreProviderMigrationScope.() -> Unit) {
    if (destination.getBoolean(migratedKey, false)) return

    StoreProviderMigrationScope(source, destination).block()
    destination.setBoolean(migratedKey, true)
  }
}

class StoreProviderMigrationScope
internal constructor(
  private val source: StoreProvider,
  private val destination: StoreProvider,
) {
  fun int(key: String, defaultValue: Int) {
    if (source.contains(key)) destination.setInt(key, source.getInt(key, defaultValue))
  }

  fun long(key: String, defaultValue: Long) {
    if (source.contains(key)) destination.setLong(key, source.getLong(key, defaultValue))
  }

  fun string(key: String, defaultValue: String) {
    if (source.contains(key)) destination.setString(key, source.getString(key, defaultValue))
  }

  fun stringSet(key: String, defaultValue: Set<String>) {
    if (source.contains(key)) {
      destination.setStringSet(key, source.getStringSet(key, defaultValue))
    }
  }

  fun boolean(key: String, defaultValue: Boolean) {
    if (source.contains(key)) destination.setBoolean(key, source.getBoolean(key, defaultValue))
  }
}
