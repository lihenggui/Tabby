package com.github.kr328.clash.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json

class ModelSerializationTest {
  @Test
  fun serializesSettingsEnums() {
    assertEquals(
      AccessControlMode.DenySelected,
      Json.decodeFromString(Json.encodeToString(AccessControlMode.DenySelected)),
    )
    assertEquals(DarkMode.ForceDark, Json.decodeFromString(Json.encodeToString(DarkMode.ForceDark)))
    assertEquals(ProxySort.Delay, Json.decodeFromString(Json.encodeToString(ProxySort.Delay)))
  }

  @Test
  fun serializesTrafficValueClass() {
    val traffic = Traffic.fromBytes(upload = 2_048, download = 4_096)

    assertEquals(traffic, Json.decodeFromString(Json.encodeToString(traffic)))
  }
}
