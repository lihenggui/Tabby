package com.github.kr328.clash.app

import com.github.kr328.clash.core.model.ConfigurationOverride
import kotlin.test.Test
import kotlin.test.assertEquals

class TabbyOverrideSettingsPersistActionTest {
  @Test
  fun resetRequestPlansClearPersistOverride() {
    assertEquals(
      TabbyOverrideSettingsPersistAction.Clear,
      tabbyOverrideSettingsPersistAction(
        resetRequested = true,
        configuration = ConfigurationOverride(httpPort = 7890),
      ),
    )
  }

  @Test
  fun normalExitPlansPatchPersistOverride() {
    val configuration = ConfigurationOverride(httpPort = 7890)

    assertEquals(
      TabbyOverrideSettingsPersistAction.Patch(configuration),
      tabbyOverrideSettingsPersistAction(resetRequested = false, configuration = configuration),
    )
  }
}
