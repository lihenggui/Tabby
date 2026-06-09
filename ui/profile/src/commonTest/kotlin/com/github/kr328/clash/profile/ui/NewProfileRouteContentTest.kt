package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile
import kotlin.test.Test
import kotlin.test.assertEquals

class NewProfileRouteContentTest {
  @Test
  fun builtInFileProviderCreatesFileProfile() {
    assertEquals(
      NewProfileRouteCreateAction.CreateProfile(Profile.Type.File),
      newProfileRouteCreateAction(NewProfileRouteBuiltInProvider.File),
    )
  }

  @Test
  fun builtInUrlProviderCreatesUrlProfile() {
    assertEquals(
      NewProfileRouteCreateAction.CreateProfile(Profile.Type.Url),
      newProfileRouteCreateAction(NewProfileRouteBuiltInProvider.Url),
    )
  }

  @Test
  fun builtInQrProviderKeepsQrScannerAction() {
    assertEquals(
      NewProfileRouteCreateAction.LaunchQrScanner,
      newProfileRouteCreateAction(NewProfileRouteBuiltInProvider.QR),
    )
  }

  @Test
  fun createProfileNameUsesProvidedNameBeforeDefaultName() {
    assertEquals(
      "External profile",
      newProfileCreateProfileName(
        NewProfileCreateRequest(
          type = Profile.Type.External,
          name = "External profile",
          source = "content://provider/profile.yaml",
        ),
        defaultName = "New profile",
      ),
    )
  }

  @Test
  fun createProfileNameFallsBackToDefaultName() {
    assertEquals(
      "New profile",
      newProfileCreateProfileName(
        NewProfileCreateRequest(
          type = Profile.Type.Url,
          source = "https://example.com/profile.yaml",
        ),
        defaultName = "New profile",
      ),
    )
  }

  @Test
  fun qrCreateActionBuildsUrlProfileCreateRequest() {
    assertEquals(
      NewProfileCreateRequest(
        type = Profile.Type.Url,
        source = "https://example.com/profile.yaml",
      ),
      newProfileCreateRequestFromQrAction(
        ProfileQrAction.CreateUrlProfile("https://example.com/profile.yaml")
      ),
    )
  }

  @Test
  fun qrMessageActionsDoNotBuildCreateRequests() {
    assertEquals(null, newProfileCreateRequestFromQrAction(ProfileQrAction.Ignore))
    assertEquals(
      null,
      newProfileCreateRequestFromQrAction(ProfileQrAction.ShowMissingPermission),
    )
    assertEquals(null, newProfileCreateRequestFromQrAction(ProfileQrAction.ShowScanError))
  }

  @Test
  fun qrScanActionCreatesUrlProfileCreateRequest() {
    assertEquals(
      NewProfileQrScanAction.CreateProfile(
        NewProfileCreateRequest(
          type = Profile.Type.Url,
          source = "https://example.com/profile.yaml",
        )
      ),
      newProfileQrScanAction(
        action = ProfileQrAction.CreateUrlProfile("https://example.com/profile.yaml"),
        missingPermissionMessage = "Camera permission denied",
        scanErrorMessage = "QR scan failed",
      ),
    )
  }

  @Test
  fun qrScanResultActionCreatesUrlProfileCreateRequest() {
    assertEquals(
      NewProfileQrScanAction.CreateProfile(
        NewProfileCreateRequest(
          type = Profile.Type.Url,
          source = "https://example.com/from-result.yaml",
        )
      ),
      newProfileQrScanAction(
        result =
          ProfileQrScanResult(
            kind = ProfileQrResultKind.Success,
            rawValue = "https://example.com/from-result.yaml",
          ),
        missingPermissionMessage = "Camera permission denied",
        scanErrorMessage = "QR scan failed",
      ),
    )
  }

  @Test
  fun qrScanActionShowsMessagesForPermissionAndScanErrors() {
    assertEquals(
      NewProfileQrScanAction.ShowMessage("Camera permission denied"),
      newProfileQrScanAction(
        action = ProfileQrAction.ShowMissingPermission,
        missingPermissionMessage = "Camera permission denied",
        scanErrorMessage = "QR scan failed",
      ),
    )
    assertEquals(
      NewProfileQrScanAction.ShowMessage("QR scan failed"),
      newProfileQrScanAction(
        action = ProfileQrAction.ShowScanError,
        missingPermissionMessage = "Camera permission denied",
        scanErrorMessage = "QR scan failed",
      ),
    )
  }

  @Test
  fun qrScanActionIgnoresNoopActions() {
    assertEquals(
      NewProfileQrScanAction.Ignore,
      newProfileQrScanAction(
        action = ProfileQrAction.Ignore,
        missingPermissionMessage = "Camera permission denied",
        scanErrorMessage = "QR scan failed",
      ),
    )
  }

  @Test
  fun externalProviderCreateActionBuildsExternalProfileCreateRequest() {
    assertEquals(
      NewProfileCreateRequest(
        type = Profile.Type.External,
        name = "External profile",
        source = "content://provider/profile.yaml",
      ),
      newProfileCreateRequestFromExternalProviderResultAction(
        NewProfileExternalProviderResultAction.CreateProfile(
          source = "content://provider/profile.yaml",
          name = "External profile",
        ),
        sourceText = { it },
      ),
    )
  }

  @Test
  fun externalProviderIgnoredResultDoesNotBuildCreateRequest() {
    assertEquals(
      null,
      newProfileCreateRequestFromExternalProviderResultAction<String>(
        NewProfileExternalProviderResultAction.Ignore,
        sourceText = { it },
      ),
    )
  }

  @Test
  fun externalProviderResultBuildsExternalProfileCreateRequest() {
    assertEquals(
      NewProfileCreateRequest(
        type = Profile.Type.External,
        name = "External profile",
        source = "content://provider/profile.yaml",
      ),
      newProfileCreateRequestFromExternalProviderResult(
        NewProfileExternalProviderResult(
          resultAccepted = true,
          source = "content://provider/profile.yaml",
          name = "External profile",
        ),
        sourceText = { it },
      ),
    )
  }

  @Test
  fun externalProviderResultIgnoresRejectedOrMissingSourcePayloads() {
    assertEquals(
      null,
      newProfileCreateRequestFromExternalProviderResult(
        NewProfileExternalProviderResult(
          resultAccepted = false,
          source = "content://provider/profile.yaml",
          name = null,
        ),
        sourceText = { it },
      ),
    )
    assertEquals(
      null,
      newProfileCreateRequestFromExternalProviderResult(
        NewProfileExternalProviderResult<String>(
          resultAccepted = true,
          source = null,
          name = null,
        ),
        sourceText = { it },
      ),
    )
  }
}
