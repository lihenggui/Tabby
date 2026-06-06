package com.github.kr328.clash.settingsstore

import com.github.kr328.clash.common.store.StoreProvider
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.containsValue
import com.russhwolf.settings.serialization.decodeValue
import com.russhwolf.settings.serialization.encodeValue
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer

@OptIn(ExperimentalSettingsApi::class, ExperimentalSerializationApi::class)
class SettingsStoreProvider(private val settings: Settings) : StoreProvider {
  override fun contains(key: String): Boolean {
    return settings.hasKey(key) || settings.containsValue(stringSetSerializer, key)
  }

  override fun getInt(key: String, defaultValue: Int): Int {
    return settings.getInt(key, defaultValue)
  }

  override fun setInt(key: String, value: Int) {
    settings.putInt(key, value)
  }

  override fun getLong(key: String, defaultValue: Long): Long {
    return settings.getLong(key, defaultValue)
  }

  override fun setLong(key: String, value: Long) {
    settings.putLong(key, value)
  }

  override fun getString(key: String, defaultValue: String): String {
    return settings.getString(key, defaultValue)
  }

  override fun setString(key: String, value: String) {
    settings.putString(key, value)
  }

  override fun getStringSet(key: String, defaultValue: Set<String>): Set<String> {
    return settings.decodeValue(
      serializer = stringSetSerializer,
      key = key,
      defaultValue = defaultValue,
    )
  }

  override fun setStringSet(key: String, value: Set<String>) {
    settings.encodeValue(
      serializer = stringSetSerializer,
      key = key,
      value = value,
    )
  }

  override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
    return settings.getBoolean(key, defaultValue)
  }

  override fun setBoolean(key: String, value: Boolean) {
    settings.putBoolean(key, value)
  }
}

fun Settings.asStoreProvider(): StoreProvider {
  return SettingsStoreProvider(this)
}

private val stringSetSerializer = SetSerializer(String.serializer())
