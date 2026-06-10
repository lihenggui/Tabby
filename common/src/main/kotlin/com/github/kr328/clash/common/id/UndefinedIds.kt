package com.github.kr328.clash.common.id

object UndefinedIds {
  private var current: Int = 0

  @Synchronized
  fun next(): Int {
    current = tabbyNextUndefinedId(current)

    return current
  }
}
