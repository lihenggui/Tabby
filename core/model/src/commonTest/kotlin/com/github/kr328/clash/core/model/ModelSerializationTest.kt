package com.github.kr328.clash.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid
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
  fun serializesNestedModelEnums() {
    assertEquals(
      FetchStatus.Action.FetchProviders,
      Json.decodeFromString(Json.encodeToString(FetchStatus.Action.FetchProviders)),
    )
    assertEquals(
      Profile.Type.External,
      Json.decodeFromString(Json.encodeToString(Profile.Type.External)),
    )
    assertEquals(Provider.Type.Rule, Json.decodeFromString(Json.encodeToString(Provider.Type.Rule)))
    assertEquals(
      Provider.VehicleType.HTTP,
      Json.decodeFromString(Json.encodeToString(Provider.VehicleType.HTTP)),
    )
    assertEquals(
      Proxy.Type.Selector,
      Json.decodeFromString(Json.encodeToString(Proxy.Type.Selector)),
    )
  }

  @Test
  fun serializesTrafficValueClass() {
    val traffic = Traffic.fromBytes(upload = 2_048, download = 4_096)

    assertEquals(traffic, Json.decodeFromString(Json.encodeToString(traffic)))
  }

  @Test
  fun serializesModelsWithNestedEnums() {
    val fetchStatus =
      FetchStatus(
        action = FetchStatus.Action.Verifying,
        args = listOf("config.yaml"),
        progress = 1,
        max = 2,
      )
    val profile =
      Profile(
        uuid = Uuid.parse("00000000-0000-0000-0000-000000000001"),
        name = "Profile",
        type = Profile.Type.Url,
        source = "https://example.com/config.yaml",
        active = true,
        interval = 60,
        upload = 1,
        download = 2,
        total = 3,
        expire = 4,
        updatedAt = 5,
        imported = true,
        pending = false,
      )
    val provider =
      Provider(
        name = "Provider",
        type = Provider.Type.Proxy,
        vehicleType = Provider.VehicleType.File,
        updatedAt = 123,
      )
    val proxy =
      Proxy(
        name = "Proxy",
        title = "Proxy",
        subtitle = "Selector",
        type = Proxy.Type.Selector,
        delay = 10,
      )

    assertEquals(fetchStatus, Json.decodeFromString(Json.encodeToString(fetchStatus)))
    assertEquals(profile, Json.decodeFromString(Json.encodeToString(profile)))
    assertEquals(provider, Json.decodeFromString(Json.encodeToString(provider)))
    assertEquals(proxy, Json.decodeFromString(Json.encodeToString(proxy)))
  }
}
