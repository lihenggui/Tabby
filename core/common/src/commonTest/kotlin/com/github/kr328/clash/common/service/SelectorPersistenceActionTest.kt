package com.github.kr328.clash.common.service

import kotlin.test.Test
import kotlin.test.assertEquals

class SelectorPersistenceActionTest {
  @Test
  fun persistsSelectionWhenSelectorPatchSucceeds() {
    assertEquals(
      TabbySelectorPersistenceAction.PersistSelection,
      tabbySelectorPersistenceAction(selectorPatched = true),
    )
  }

  @Test
  fun removesSelectionWhenSelectorPatchFails() {
    assertEquals(
      TabbySelectorPersistenceAction.RemoveSelection,
      tabbySelectorPersistenceAction(selectorPatched = false),
    )
  }
}
