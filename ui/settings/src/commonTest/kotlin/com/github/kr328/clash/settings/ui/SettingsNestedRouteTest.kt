package com.github.kr328.clash.settings.ui

import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsNestedRouteTest {
  @Test
  fun overrideSettingsMainRouteIsANavKey() {
    val route: NavKey = OverrideSettingsRoute.Main

    assertEquals(OverrideSettingsRoute.Main, route)
  }

  @Test
  fun metaFeatureSettingsMainRouteIsANavKey() {
    val route: NavKey = MetaFeatureSettingsRoute.Main

    assertEquals(MetaFeatureSettingsRoute.Main, route)
  }
}
