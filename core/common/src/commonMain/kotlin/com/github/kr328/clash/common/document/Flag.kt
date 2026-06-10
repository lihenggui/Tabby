package com.github.kr328.clash.common.document

enum class Flag {
  Writable,
  Deletable,
  Virtual,
}

fun tabbyDocumentPlatformFlags(
  flags: Set<Flag>,
  writableFlag: Int,
  deletableFlag: Int,
  virtualFlag: Int,
): Int {
  var platformFlags = 0

  flags.forEach {
    platformFlags =
      when (it) {
        Flag.Writable -> platformFlags or writableFlag
        Flag.Deletable -> platformFlags or deletableFlag
        Flag.Virtual -> platformFlags or virtualFlag
      }
  }

  return platformFlags
}
