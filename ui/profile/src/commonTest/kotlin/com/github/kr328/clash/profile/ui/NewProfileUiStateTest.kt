package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

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

  @Test
  fun createEventStateEmitsOnlyLaunchEvents() {
    val externalProvider = "external-provider-intent"

    assertEquals(
      null,
      newProfileCreateEventState<String>(NewProfileCreateAction.CreateProfile(Profile.Type.File)),
    )
    assertEquals(
      NewProfileEventState.LaunchQRScanner,
      newProfileCreateEventState<String>(NewProfileCreateAction.LaunchQRScanner),
    )
    assertEquals(
      NewProfileEventState.LaunchExternalProvider(externalProvider),
      newProfileCreateEventState(
        NewProfileCreateAction.LaunchExternalProvider,
        externalProvider,
      ),
    )
  }

  @Test
  fun detailEventStateOpensAppSettingsOnlyForDetailActions() {
    val appSettingsTarget = "package:com.example.provider"

    assertEquals(
      NewProfileEventState.OpenAppSettings(appSettingsTarget),
      newProfileDetailEventState(
        NewProfileDetailAction.OpenAppSettings("com.example.provider"),
        appSettingsTarget,
      ),
    )
    assertEquals(
      null,
      newProfileDetailEventState(NewProfileDetailAction.Ignore, appSettingsTarget),
    )
  }

  @Test
  fun qrEventStateShowsMessagesOnlyForMessageActions() {
    val missingPermissionMessage = "Camera permission denied"
    val scanErrorMessage = "QR scan failed"

    assertEquals(
      null,
      newProfileQrEventState(
        ProfileQrAction.CreateUrlProfile("https://example.com/config.yaml"),
        missingPermissionMessage = missingPermissionMessage,
        scanErrorMessage = scanErrorMessage,
      ),
    )
    assertEquals(
      null,
      newProfileQrEventState(
        ProfileQrAction.Ignore,
        missingPermissionMessage = missingPermissionMessage,
        scanErrorMessage = scanErrorMessage,
      ),
    )
    assertEquals(
      NewProfileEventState.ShowMessage(missingPermissionMessage),
      newProfileQrEventState(
        ProfileQrAction.ShowMissingPermission,
        missingPermissionMessage = missingPermissionMessage,
        scanErrorMessage = scanErrorMessage,
      ),
    )
    assertEquals(
      NewProfileEventState.ShowMessage(scanErrorMessage),
      newProfileQrEventState(
        ProfileQrAction.ShowScanError,
        missingPermissionMessage = missingPermissionMessage,
        scanErrorMessage = scanErrorMessage,
      ),
    )
  }

  @Test
  fun newProfileProviderSelectionActionSelectsProviderByIndex() {
    val providers =
      listOf(
        testProvider("file", detail = false),
        testProvider("external", detail = true),
      )

    assertEquals(
      NewProfileProviderSelectionAction.SelectProvider(providers[1]),
      newProfileProviderSelectionAction(providers, index = 1),
    )
  }

  @Test
  fun newProfileProviderSelectionActionIgnoresMissingIndex() {
    assertEquals(
      NewProfileProviderSelectionAction.Ignore,
      newProfileProviderSelectionAction(listOf(testProvider("file")), index = 1),
    )
  }

  @Test
  fun newProfileProviderDetailSelectionActionSelectsDetailProvider() {
    val providers =
      listOf(
        testProvider("file", detail = false),
        testProvider("external", detail = true),
      )

    assertEquals(
      NewProfileProviderSelectionAction.SelectProvider(providers[1]),
      newProfileProviderDetailSelectionAction(providers, index = 1) { provider ->
        provider.takeIf { it.detail }
      },
    )
  }

  @Test
  fun newProfileProviderDetailSelectionActionIgnoresNonDetailProvider() {
    assertEquals(
      NewProfileProviderSelectionAction.Ignore,
      newProfileProviderDetailSelectionAction(
        listOf(testProvider("file", detail = false)),
        index = 0,
      ) { provider ->
        provider.takeIf { it.detail }
      },
    )
  }

  @Test
  fun newProfileProviderDetailSelectionActionIgnoresMissingIndex() {
    assertEquals(
      NewProfileProviderSelectionAction.Ignore,
      newProfileProviderDetailSelectionAction(
        listOf(testProvider("external", detail = true)),
        index = 1,
      ) { provider ->
        provider.takeIf { it.detail }
      },
    )
  }

  @Test
  fun eventStateCarriesPlatformPayloadsAsGenericValues() {
    val externalProvider = "external-provider-intent"
    val appSettingsTarget = "package:com.example.provider"
    val uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")
    val launchExternalEvent: NewProfileEventState<String, String> =
      NewProfileEventState.LaunchExternalProvider(externalProvider)
    val openAppSettingsEvent: NewProfileEventState<String, String> =
      NewProfileEventState.OpenAppSettings(appSettingsTarget)
    val launchPropertiesEvent: NewProfileEventState<String, String> =
      NewProfileEventState.LaunchProperties(uuid)

    assertEquals(NewProfileEventState.LaunchExternalProvider(externalProvider), launchExternalEvent)
    assertEquals(NewProfileEventState.OpenAppSettings(appSettingsTarget), openAppSettingsEvent)
    assertEquals(NewProfileEventState.LaunchProperties(uuid), launchPropertiesEvent)
  }

  @Test
  fun errorEventStateFallsBackToUnknownMessage() {
    assertEquals(
      NewProfileEventState.ShowMessage("create failed"),
      newProfileErrorEventState("create failed", "Unknown error"),
    )
    assertEquals(
      NewProfileEventState.ShowMessage("Unknown error"),
      newProfileErrorEventState(null, "Unknown error"),
    )
  }

  private fun testProvider(id: String): TestProvider {
    return testProvider(id, detail = false)
  }

  private fun testProvider(id: String, detail: Boolean): TestProvider {
    return TestProvider(id, detail)
  }

  private data class TestProvider(val id: String, val detail: Boolean)
}
