package com.github.kr328.clash.app

import com.github.kr328.clash.core.model.Profile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class TabbyInstallProfileRequestTest {
  @Test
  fun tabbyInstallProfileRequestReturnsNullWhenSourceIsMissing() {
    assertNull(
      tabbyInstallProfileRequest(
        source = null,
        type = "url",
        name = "Profile",
        defaultName = "Default",
      )
    )
  }

  @Test
  fun tabbyInstallProfileRequestKeepsUrlTypeAsDefault() {
    assertEquals(
      TabbyInstallProfileRequest(
        type = Profile.Type.Url,
        name = "Default",
        source = "https://example.com/config.yaml",
      ),
      tabbyInstallProfileRequest(
        source = "https://example.com/config.yaml",
        type = null,
        name = null,
        defaultName = "Default",
      ),
    )

    assertEquals(
      Profile.Type.Url,
      tabbyInstallProfileRequest(
          source = "https://example.com/config.yaml",
          type = "unsupported",
          name = "Profile",
          defaultName = "Default",
        )
        ?.type,
    )
  }

  @Test
  fun tabbyInstallProfileRequestParsesFileTypeCaseInsensitively() {
    assertEquals(
      TabbyInstallProfileRequest(
        type = Profile.Type.File,
        name = "Profile",
        source = "content://profiles/config.yaml",
      ),
      tabbyInstallProfileRequest(
        source = "content://profiles/config.yaml",
        type = "FiLe",
        name = "Profile",
        defaultName = "Default",
      ),
    )
  }

  @Test
  fun tabbyInstallProfileResultActionOpensInstalledProfileProperties() {
    val uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")

    assertEquals(
      TabbyInstallProfileResultAction.OpenRoute(
        TabbyExternalRouteAction.OpenProfileProperties(uuid)
      ),
      tabbyInstallProfileResultAction(uuid),
    )
  }
}
