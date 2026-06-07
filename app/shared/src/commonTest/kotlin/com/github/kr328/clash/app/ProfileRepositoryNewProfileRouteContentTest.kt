package com.github.kr328.clash.app

import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.profile.ui.NewProfileRouteBuiltInProvider
import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileRepositoryNewProfileRouteContentTest {
  @Test
  fun builtInFileProviderCreatesFileProfile() {
    assertEquals(
      TabbyNewProfileCreateAction.CreateProfile(Profile.Type.File),
      NewProfileRouteBuiltInProvider.File.toTabbyNewProfileCreateAction(),
    )
  }

  @Test
  fun builtInUrlProviderCreatesUrlProfile() {
    assertEquals(
      TabbyNewProfileCreateAction.CreateProfile(Profile.Type.Url),
      NewProfileRouteBuiltInProvider.Url.toTabbyNewProfileCreateAction(),
    )
  }

  @Test
  fun builtInQrProviderKeepsPlatformScannerAction() {
    assertEquals(
      TabbyNewProfileCreateAction.LaunchQrScanner,
      NewProfileRouteBuiltInProvider.QR.toTabbyNewProfileCreateAction(),
    )
  }
}
