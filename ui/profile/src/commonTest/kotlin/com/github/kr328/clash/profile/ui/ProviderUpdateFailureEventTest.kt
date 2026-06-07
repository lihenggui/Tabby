package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Provider
import kotlin.test.Test
import kotlin.test.assertEquals

class ProviderUpdateFailureEventTest {
  @Test
  fun failureEventUsesProviderNameAndThrowableMessage() {
    assertEquals(
      ProviderUpdateFailureEvent(providerName = "Remote", errorMessage = "network failed"),
      providerUpdateFailureEvent(
        provider = provider("Remote"),
        cause = Throwable("network failed"),
      ),
    )
  }

  @Test
  fun failureEventFallsBackToThrowableString() {
    assertEquals(
      ProviderUpdateFailureEvent(providerName = "Remote", errorMessage = "fallback"),
      providerUpdateFailureEvent(provider = provider("Remote"), cause = FallbackThrowable()),
    )
  }

  private fun provider(name: String): Provider {
    return Provider(
      name = name,
      type = Provider.Type.Proxy,
      vehicleType = Provider.VehicleType.HTTP,
      updatedAt = 0,
    )
  }

  private class FallbackThrowable : Throwable() {
    override fun toString(): String {
      return "fallback"
    }
  }
}
