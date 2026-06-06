package com.github.kr328.clash.app

data class TabbyRecentsTaskAction(val excludeFromRecents: Boolean)

fun tabbyRecentsTaskAction(hideFromRecents: Boolean): TabbyRecentsTaskAction =
  TabbyRecentsTaskAction(excludeFromRecents = hideFromRecents)
