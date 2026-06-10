package com.github.kr328.clash.settingsstore

import android.content.Context
import android.content.SharedPreferences
import com.github.kr328.clash.common.store.StoreProvider
import com.russhwolf.settings.SharedPreferencesSettings

fun SharedPreferences.asSettingsStoreProvider(): StoreProvider {
  return SharedPreferencesSettings(this).asStoreProvider()
}

fun Context.settingsStoreProvider(name: String): StoreProvider {
  return getSharedPreferences(name, Context.MODE_PRIVATE).asSettingsStoreProvider()
}
