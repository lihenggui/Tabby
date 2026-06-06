package com.github.kr328.clash.settingsstore

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsStoreProviderTest {
  @Test
  fun settingsBackedProviderStoresSupportedTypes() {
    val provider = MapSettings().asStoreProvider()

    provider.setInt("int", 1)
    provider.setLong("long", 2L)
    provider.setString("string", "value")
    provider.setStringSet("set", setOf("a", "b"))
    provider.setBoolean("boolean", true)

    assertEquals(1, provider.getInt("int", 0))
    assertEquals(2L, provider.getLong("long", 0L))
    assertEquals("value", provider.getString("string", ""))
    assertEquals(setOf("a", "b"), provider.getStringSet("set", emptySet()))
    assertTrue(provider.getBoolean("boolean", false))
    assertTrue(provider.contains("int"))
  }

  @Test
  fun migrationCopiesOnlyExistingKeysAndSetsFlag() {
    val source = MapSettings().asStoreProvider()
    val destination = MapSettings().asStoreProvider()

    source.setBoolean("enabled", true)
    source.setStringSet("packages", setOf("one"))

    StoreProviderMigration(
        source = source,
        destination = destination,
        migratedKey = "migrated",
      )
      .migrate {
        boolean("enabled", defaultValue = false)
        int("missing", defaultValue = 7)
        stringSet("packages", defaultValue = emptySet())
      }

    assertTrue(destination.getBoolean("enabled", false))
    assertEquals(setOf("one"), destination.getStringSet("packages", emptySet()))
    assertFalse(destination.contains("missing"))
    assertTrue(destination.getBoolean("migrated", false))
  }
}
