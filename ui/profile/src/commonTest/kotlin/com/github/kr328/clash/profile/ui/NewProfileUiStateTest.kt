package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile
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

  @Test
  fun newProfileCreateActionCreatesFileProfile() {
    assertEquals(
      NewProfileCreateAction.CreateProfile(Profile.Type.File),
      newProfileCreateAction(NewProfileProviderKind.File),
    )
  }

  @Test
  fun newProfileCreateActionCreatesUrlProfile() {
    assertEquals(
      NewProfileCreateAction.CreateProfile(Profile.Type.Url),
      newProfileCreateAction(NewProfileProviderKind.Url),
    )
  }

  @Test
  fun newProfileCreateActionLaunchesQrScanner() {
    assertEquals(
      NewProfileCreateAction.LaunchQRScanner,
      newProfileCreateAction(NewProfileProviderKind.QR),
    )
  }

  @Test
  fun newProfileCreateActionLaunchesExternalProvider() {
    assertEquals(
      NewProfileCreateAction.LaunchExternalProvider,
      newProfileCreateAction(NewProfileProviderKind.External),
    )
  }

  @Test
  fun newProfileDetailActionOpensExternalProviderAppSettings() {
    assertEquals(
      NewProfileDetailAction.OpenAppSettings("com.example.provider"),
      newProfileDetailAction("com.example.provider"),
    )
  }

  @Test
  fun newProfileDetailActionIgnoresMissingExternalProviderPackage() {
    assertEquals(
      NewProfileDetailAction.Ignore,
      newProfileDetailAction(null),
    )
  }

  @Test
  fun newProfileExternalProviderResultActionCreatesExternalProfileForAcceptedSource() {
    assertEquals(
      NewProfileExternalProviderResultAction.CreateProfile("External config"),
      newProfileExternalProviderResultAction(
        resultAccepted = true,
        sourceSelected = true,
        name = "External config",
      ),
    )
  }

  @Test
  fun newProfileExternalProviderResultActionIgnoresRejectedResults() {
    assertEquals(
      NewProfileExternalProviderResultAction.Ignore,
      newProfileExternalProviderResultAction(
        resultAccepted = false,
        sourceSelected = true,
        name = "Ignored",
      ),
    )
  }

  @Test
  fun newProfileExternalProviderResultActionIgnoresMissingSource() {
    assertEquals(
      NewProfileExternalProviderResultAction.Ignore,
      newProfileExternalProviderResultAction(
        resultAccepted = true,
        sourceSelected = false,
        name = "Ignored",
      ),
    )
  }

  private fun testProvider(id: String): TestProvider {
    return TestProvider(id)
  }

  private data class TestProvider(val id: String)
}
