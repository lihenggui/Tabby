package com.github.kr328.clash.common.service

enum class TabbyServiceCreateAction {
  StartService,
  StopDuplicate,
}

fun tabbyServiceCreateAction(serviceRunning: Boolean): TabbyServiceCreateAction {
  return if (serviceRunning) {
    TabbyServiceCreateAction.StopDuplicate
  } else {
    TabbyServiceCreateAction.StartService
  }
}
