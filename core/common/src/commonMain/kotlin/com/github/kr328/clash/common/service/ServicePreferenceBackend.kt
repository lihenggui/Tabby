package com.github.kr328.clash.common.service

enum class TabbyServicePreferenceBackend {
  Direct,
  MultiProcess,
}

fun tabbyServicePreferenceBackend(localServiceContext: Boolean): TabbyServicePreferenceBackend {
  return if (localServiceContext) {
    TabbyServicePreferenceBackend.Direct
  } else {
    TabbyServicePreferenceBackend.MultiProcess
  }
}
