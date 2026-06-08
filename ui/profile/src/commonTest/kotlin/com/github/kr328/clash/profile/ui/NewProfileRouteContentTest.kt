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
  fun builtInQrProviderKeepsPlatformScannerAction() {
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
  fun externalProviderCreateActionBuildsExternalProfileCreateRequest() {
    assertEquals(
      NewProfileCreateRequest(
        type = Profile.Type.External,
        name = "External profile",
        source = "content://provider/profile.yaml",
      ),
      newProfileCreateRequestFromExternalProviderResultAction(
        NewProfileExternalProviderResultAction.CreateProfile("External profile"),
        source = "content://provider/profile.yaml",
      ),
    )
  }

  @Test
  fun externalProviderIgnoredResultDoesNotBuildCreateRequest() {
    assertEquals(
      null,
      newProfileCreateRequestFromExternalProviderResultAction(
        NewProfileExternalProviderResultAction.Ignore,
        source = "content://provider/profile.yaml",
      ),
    )
  }
}
