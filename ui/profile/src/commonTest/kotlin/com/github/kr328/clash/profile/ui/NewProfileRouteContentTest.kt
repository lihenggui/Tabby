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
}
