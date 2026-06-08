package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class NewProfileUiStateTest {
  @Test
  fun initialStatesUseDefaultUiStateAndIdleEvent() {
    val state: NewProfileUiState<TestProvider> = newProfileInitialUiState()
    val event: NewProfileEventState<String, String> = newProfileInitialEventState()

    assertEquals(NewProfileUiState(), state)
    assertEquals(NewProfileEventState.Idle, event)
  }

  @Test
  fun newProfileEventPlatformActionMapsEventStates() {
    val externalProvider = "external-provider-intent"
    val appSettingsTarget = "package:com.example.provider"
    val uuid = Uuid.parse("00000000-0000-0000-0000-000000000007")

    assertEquals(
      NewProfileEventPlatformAction.Ignore,
      newProfileEventPlatformAction(NewProfileEventState.Idle),
    )
    assertEquals(
      NewProfileEventPlatformAction.LaunchQRScanner,
      newProfileEventPlatformAction(NewProfileEventState.LaunchQRScanner),
    )
    assertEquals(
      NewProfileEventPlatformAction.LaunchExternalProvider(externalProvider),
      newProfileEventPlatformAction(NewProfileEventState.LaunchExternalProvider(externalProvider)),
    )
    assertEquals(
      NewProfileEventPlatformAction.LaunchProperties(uuid),
      newProfileEventPlatformAction(NewProfileEventState.LaunchProperties(uuid)),
    )
    assertEquals(
      NewProfileEventPlatformAction.OpenAppSettings(appSettingsTarget),
      newProfileEventPlatformAction(NewProfileEventState.OpenAppSettings(appSettingsTarget)),
    )
    assertEquals(
      NewProfileEventPlatformAction.ShowMessage("create failed"),
      newProfileEventPlatformAction(NewProfileEventState.ShowMessage("create failed")),
    )
    assertEquals(
      NewProfileEventPlatformAction.Finish,
      newProfileEventPlatformAction(NewProfileEventState.Finish),
    )
  }

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
  fun builtInProviderKindsKeepDefaultCreationOrder() {
    assertEquals(
      listOf(
        NewProfileProviderKind.File,
        NewProfileProviderKind.Url,
        NewProfileProviderKind.QR,
      ),
      newProfileBuiltInProviderKinds(),
    )
  }

  @Test
  fun builtInProviderPresentationsMapDefaultTextAndGraphicTokens() {
    assertEquals(
      NewProfileBuiltInProviderPresentation(
        nameToken = NewProfileProviderTextToken.File,
        summaryToken = NewProfileProviderTextToken.ImportFromFile,
        graphicToken = NewProfileProviderGraphicToken.File,
      ),
      newProfileBuiltInProviderPresentation(NewProfileProviderKind.File),
    )
    assertEquals(
      NewProfileBuiltInProviderPresentation(
        nameToken = NewProfileProviderTextToken.Url,
        summaryToken = NewProfileProviderTextToken.ImportFromUrl,
        graphicToken = NewProfileProviderGraphicToken.Url,
      ),
      newProfileBuiltInProviderPresentation(NewProfileProviderKind.Url),
    )
    assertEquals(
      NewProfileBuiltInProviderPresentation(
        nameToken = NewProfileProviderTextToken.Qr,
        summaryToken = NewProfileProviderTextToken.ImportFromQr,
        graphicToken = NewProfileProviderGraphicToken.Qr,
      ),
      newProfileBuiltInProviderPresentation(NewProfileProviderKind.QR),
    )
  }

  @Test
  fun providerTextPlatformTokenMapsTextTokens() {
    assertEquals(
      "file",
      newProfileProviderTextPlatformToken(
        token = NewProfileProviderTextToken.File,
        file = "file",
        url = "url",
        qr = "qr",
        importFromFile = "import-file",
        importFromUrl = "import-url",
        importFromQr = "import-qr",
      ),
    )
    assertEquals(
      "url",
      newProfileProviderTextPlatformToken(
        token = NewProfileProviderTextToken.Url,
        file = "file",
        url = "url",
        qr = "qr",
        importFromFile = "import-file",
        importFromUrl = "import-url",
        importFromQr = "import-qr",
      ),
    )
    assertEquals(
      "qr",
      newProfileProviderTextPlatformToken(
        token = NewProfileProviderTextToken.Qr,
        file = "file",
        url = "url",
        qr = "qr",
        importFromFile = "import-file",
        importFromUrl = "import-url",
        importFromQr = "import-qr",
      ),
    )
    assertEquals(
      "import-file",
      newProfileProviderTextPlatformToken(
        token = NewProfileProviderTextToken.ImportFromFile,
        file = "file",
        url = "url",
        qr = "qr",
        importFromFile = "import-file",
        importFromUrl = "import-url",
        importFromQr = "import-qr",
      ),
    )
    assertEquals(
      "import-url",
      newProfileProviderTextPlatformToken(
        token = NewProfileProviderTextToken.ImportFromUrl,
        file = "file",
        url = "url",
        qr = "qr",
        importFromFile = "import-file",
        importFromUrl = "import-url",
        importFromQr = "import-qr",
      ),
    )
    assertEquals(
      "import-qr",
      newProfileProviderTextPlatformToken(
        token = NewProfileProviderTextToken.ImportFromQr,
        file = "file",
        url = "url",
        qr = "qr",
        importFromFile = "import-file",
        importFromUrl = "import-url",
        importFromQr = "import-qr",
      ),
    )
  }

  @Test
  fun providerGraphicPlatformTokenMapsGraphicTokens() {
    assertEquals(
      "file",
      newProfileProviderGraphicPlatformToken(
        token = NewProfileProviderGraphicToken.File,
        file = "file",
        url = "url",
        qr = "qr",
      ),
    )
    assertEquals(
      "url",
      newProfileProviderGraphicPlatformToken(
        token = NewProfileProviderGraphicToken.Url,
        file = "file",
        url = "url",
        qr = "qr",
      ),
    )
    assertEquals(
      "qr",
      newProfileProviderGraphicPlatformToken(
        token = NewProfileProviderGraphicToken.Qr,
        file = "file",
        url = "url",
        qr = "qr",
      ),
    )
  }

  @Test
  fun builtInProviderPresentationIgnoresExternalProviderKind() {
    assertEquals(null, newProfileBuiltInProviderPresentation(NewProfileProviderKind.External))
  }

  @Test
  fun providerItemUsesCommonDetailRuleForProviderKind() {
    assertEquals(false, newProfileProviderHasDetail(NewProfileProviderKind.File))
    assertEquals(false, newProfileProviderHasDetail(NewProfileProviderKind.Url))
    assertEquals(false, newProfileProviderHasDetail(NewProfileProviderKind.QR))
    assertEquals(true, newProfileProviderHasDetail(NewProfileProviderKind.External))
  }

  @Test
  fun providerItemCopiesPresentationAndDefaultsDetailFromProviderKind() {
    assertEquals(
      NewProfileProviderItem(
        name = "External",
        summary = "External provider",
        iconPainter = null,
        hasDetail = true,
      ),
      newProfileProviderItem(
        name = "External",
        summary = "External provider",
        iconPainter = null,
        kind = NewProfileProviderKind.External,
      ),
    )
    assertEquals(
      NewProfileProviderItem(
        name = "File",
        summary = "Import from file",
        iconPainter = null,
        hasDetail = false,
      ),
      newProfileProviderItem(
        name = "File",
        summary = "Import from file",
        iconPainter = null,
        kind = NewProfileProviderKind.File,
      ),
    )
  }

  @Test
  fun providerItemAllowsRouteSpecificDetailOverride() {
    assertEquals(
      NewProfileProviderItem(
        name = "External",
        summary = "External provider",
        iconPainter = null,
        hasDetail = false,
      ),
      newProfileProviderItem(
        name = "External",
        summary = "External provider",
        iconPainter = null,
        kind = NewProfileProviderKind.External,
        hasDetail = false,
      ),
    )
  }

  @Test
  fun providerListKeepsBuiltInProvidersBeforeExternalProviders() {
    val externalProviders = listOf(testProvider("external-a"), testProvider("external-b"))

    val providers =
      newProfileProviderList(externalProviders = externalProviders) { kind ->
        when (kind) {
          NewProfileProviderKind.File -> testProvider("file")
          NewProfileProviderKind.Url -> testProvider("url")
          NewProfileProviderKind.QR -> testProvider("qr")
          NewProfileProviderKind.External -> null
        }
      }

    assertEquals(
      listOf("file", "url", "qr", "external-a", "external-b"),
      providers.map(TestProvider::id),
    )
  }

  @Test
  fun providerListOmitsUnavailableBuiltInProviders() {
    val providers =
      newProfileProviderList(externalProviders = listOf(testProvider("external"))) { kind ->
        when (kind) {
          NewProfileProviderKind.File -> testProvider("file")
          NewProfileProviderKind.Url -> null
          NewProfileProviderKind.QR -> testProvider("qr")
          NewProfileProviderKind.External -> error("External is not a built-in provider")
        }
      }

    assertEquals(listOf("file", "qr", "external"), providers.map(TestProvider::id))
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
  fun externalProviderPresentationUsesComponentKeyBeforeFallbacks() {
    assertEquals(
      NewProfileExternalProviderPresentation(
        key = "com.example.provider/.ProviderActivity",
        name = "Example Provider",
        summary = "Import from Example",
        hasDetail = true,
      ),
      newProfileExternalProviderPresentationFromPlatformPayload(
        componentKey = "com.example.provider/.ProviderActivity",
        packageName = "com.example.provider",
        name = "Example Provider",
        summary = "Import from Example",
      ),
    )
  }

  @Test
  fun externalProviderPresentationFallsBackToPackageThenNameForStableKey() {
    assertEquals(
      NewProfileExternalProviderPresentation(
        key = "com.example.provider",
        name = "Example Provider",
        summary = "Import from Example",
        hasDetail = true,
      ),
      newProfileExternalProviderPresentationFromPlatformPayload(
        componentKey = null,
        packageName = "com.example.provider",
        name = "Example Provider",
        summary = "Import from Example",
      ),
    )
    assertEquals(
      NewProfileExternalProviderPresentation(
        key = "Example Provider",
        name = "Example Provider",
        summary = "Import from Example",
        hasDetail = false,
      ),
      newProfileExternalProviderPresentationFromPlatformPayload(
        componentKey = null,
        packageName = null,
        name = "Example Provider",
        summary = "Import from Example",
      ),
    )
  }

  @Test
  fun externalProviderPlatformPayloadKeepsOpaquePlatformValues() {
    assertEquals(
      NewProfileExternalProviderPlatformPayload(
        componentKey = "com.example.provider/.ProviderActivity",
        packageName = "com.example.provider",
        name = "Example Provider",
        summary = "Import from Example",
        icon = "provider-icon",
        launchTarget = "provider-intent",
      ),
      newProfileExternalProviderFromPlatformPayload(
        componentKey = "com.example.provider/.ProviderActivity",
        packageName = "com.example.provider",
        name = "Example Provider",
        summary = "Import from Example",
        icon = "provider-icon",
        launchTarget = "provider-intent",
      ),
    )
  }

  @Test
  fun externalProviderPlatformPayloadMapsToPresentation() {
    val provider =
      newProfileExternalProviderFromPlatformPayload(
        componentKey = "com.example.provider/.ProviderActivity",
        packageName = "com.example.provider",
        name = "Example Provider",
        summary = "Import from Example",
        icon = "provider-icon",
        launchTarget = "provider-intent",
      )

    assertEquals(
      NewProfileExternalProviderPresentation(
        key = "com.example.provider/.ProviderActivity",
        name = "Example Provider",
        summary = "Import from Example",
        hasDetail = true,
      ),
      newProfileExternalProviderPresentationFromPlatformPayload(provider),
    )
  }

  @Test
  fun newProfileExternalProviderPlatformPayloadKeepsAcceptedSourceMetadata() {
    assertEquals(
      NewProfileExternalProviderResult(
        resultAccepted = true,
        sourceSelected = true,
        name = "External config",
      ),
      newProfileExternalProviderResultFromPlatformPayload(
        resultAccepted = true,
        sourceSelected = true,
        name = "External config",
      ),
    )
  }

  @Test
  fun newProfileExternalProviderPlatformPayloadDropsNameWhenResultCannotCreateProfile() {
    assertEquals(
      NewProfileExternalProviderResult(
        resultAccepted = false,
        sourceSelected = true,
        name = null,
      ),
      newProfileExternalProviderResultFromPlatformPayload(
        resultAccepted = false,
        sourceSelected = true,
        name = "Ignored",
      ),
    )
    assertEquals(
      NewProfileExternalProviderResult(
        resultAccepted = true,
        sourceSelected = false,
        name = null,
      ),
      newProfileExternalProviderResultFromPlatformPayload(
        resultAccepted = true,
        sourceSelected = false,
        name = "Ignored",
      ),
    )
  }

  @Test
  fun newProfileExternalProviderPlatformResultKeepsAcceptedSelectedSourceMetadata() {
    assertEquals(
      NewProfileExternalProviderResult(
        resultAccepted = true,
        sourceSelected = true,
        name = "External config",
      ),
      newProfileExternalProviderResultFromPlatformResult(
        resultCode = 10,
        acceptedResultCode = 10,
        source = "content://provider/profile.yaml",
        name = "External config",
      ),
    )
  }

  @Test
  fun newProfileExternalProviderPlatformResultDropsNameWhenResultCannotCreateProfile() {
    assertEquals(
      NewProfileExternalProviderResult(
        resultAccepted = false,
        sourceSelected = true,
        name = null,
      ),
      newProfileExternalProviderResultFromPlatformResult(
        resultCode = 20,
        acceptedResultCode = 10,
        source = "content://provider/profile.yaml",
        name = "Ignored",
      ),
    )
    assertEquals(
      NewProfileExternalProviderResult(
        resultAccepted = true,
        sourceSelected = false,
        name = null,
      ),
      newProfileExternalProviderResultFromPlatformResult(
        resultCode = 10,
        acceptedResultCode = 10,
        source = null,
        name = "Ignored",
      ),
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
    assertEquals(
      NewProfileExternalProviderResultAction.CreateProfile("External config"),
      newProfileExternalProviderResultAction(
        NewProfileExternalProviderResult(
          resultAccepted = true,
          sourceSelected = true,
          name = "External config",
        )
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
    assertEquals(
      NewProfileExternalProviderResultAction.Ignore,
      newProfileExternalProviderResultAction(
        NewProfileExternalProviderResult(
          resultAccepted = false,
          sourceSelected = true,
          name = "Ignored",
        )
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
    assertEquals(
      NewProfileExternalProviderResultAction.Ignore,
      newProfileExternalProviderResultAction(
        NewProfileExternalProviderResult(
          resultAccepted = true,
          sourceSelected = false,
          name = "Ignored",
        )
      ),
    )
  }

  @Test
  fun newProfileExternalProviderResultAcceptedFromPlatformResultCodeMapsAcceptedCodeOnly() {
    assertEquals(
      true,
      newProfileExternalProviderResultAcceptedFromPlatformResultCode(
        resultCode = 10,
        acceptedResultCode = 10,
      ),
    )
    assertEquals(
      false,
      newProfileExternalProviderResultAcceptedFromPlatformResultCode(
        resultCode = 20,
        acceptedResultCode = 10,
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
  fun launchPropertiesEventStateCarriesCreatedProfileUuid() {
    val uuid = Uuid.parse("00000000-0000-0000-0000-000000000009")

    assertEquals(
      NewProfileEventState.LaunchProperties(uuid),
      newProfileLaunchPropertiesEventState(uuid),
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

  @Test
  fun consumedEventStateResetsToIdle() {
    val event: NewProfileEventState<String, String> = newProfileConsumedEventState()

    assertEquals(NewProfileEventState.Idle, event)
  }

  private fun testProvider(id: String): TestProvider {
    return testProvider(id, detail = false)
  }

  private fun testProvider(id: String, detail: Boolean): TestProvider {
    return TestProvider(id, detail)
  }

  private data class TestProvider(val id: String, val detail: Boolean)
}
