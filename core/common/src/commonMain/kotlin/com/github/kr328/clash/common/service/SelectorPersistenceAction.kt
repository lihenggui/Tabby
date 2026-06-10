package com.github.kr328.clash.common.service

enum class TabbySelectorPersistenceAction {
  PersistSelection,
  RemoveSelection,
}

fun tabbySelectorPersistenceAction(selectorPatched: Boolean): TabbySelectorPersistenceAction {
  return if (selectorPatched) {
    TabbySelectorPersistenceAction.PersistSelection
  } else {
    TabbySelectorPersistenceAction.RemoveSelection
  }
}
