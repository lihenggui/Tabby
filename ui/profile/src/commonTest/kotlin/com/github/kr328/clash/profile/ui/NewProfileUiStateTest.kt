package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NewProfileUiStateTest {
  @Test
  fun defaultProvidersAreEmpty() {
    val state = NewProfileUiState<TestProvider>()

    assertTrue(state.providers.isEmpty())
  }

  @Test
  fun providerReplacementKeepsOrder() {
    val providers = listOf(testProvider("file"), testProvider("url"), testProvider("external"))
    val state = NewProfileUiState<TestProvider>().withNewProfileProviders(providers)

    assertEquals(providers, state.providers)
  }

  @Test
  fun providerReplacementCanClearProviders() {
    val state =
      NewProfileUiState(providers = listOf(testProvider("file")))
        .withNewProfileProviders(emptyList())

    assertTrue(state.providers.isEmpty())
  }

  private fun testProvider(id: String): TestProvider {
    return TestProvider(id)
  }

  private data class TestProvider(val id: String)
}
