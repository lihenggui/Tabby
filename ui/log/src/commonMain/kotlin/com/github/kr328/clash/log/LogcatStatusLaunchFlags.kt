package com.github.kr328.clash.log

internal fun logcatStatusLaunchFlags(
  newTaskFlag: Int,
  singleTopFlag: Int,
  clearTopFlag: Int,
): Int {
  return newTaskFlag or singleTopFlag or clearTopFlag
}
