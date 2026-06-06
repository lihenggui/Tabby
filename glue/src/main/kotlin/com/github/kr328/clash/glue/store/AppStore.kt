package com.github.kr328.clash.glue.store

import android.content.Context
import com.github.kr328.clash.common.store.Store
import com.github.kr328.clash.common.store.asStoreProvider
import com.github.kr328.clash.settingsstore.StoreProviderMigration
import com.github.kr328.clash.settingsstore.settingsStoreProvider

class AppStore(context: Context) {
  private val store = Store(createStoreProvider(context))

  var updatedAt: Long by store.long(key = "updated_at", defaultValue = -1)

  companion object {
    private const val LEGACY_FILE_NAME = "app"
    private const val SETTINGS_FILE_NAME = "settings_app"
    private const val MIGRATED_KEY = "__migrated_from_shared_preferences_v1"

    private fun createStoreProvider(context: Context) =
      context.settingsStoreProvider(SETTINGS_FILE_NAME).also { destination ->
        StoreProviderMigration(
            source =
              context
                .getSharedPreferences(LEGACY_FILE_NAME, Context.MODE_PRIVATE)
                .asStoreProvider(),
            destination = destination,
            migratedKey = MIGRATED_KEY,
          )
          .migrate { long("updated_at", defaultValue = -1) }
      }
  }
}
