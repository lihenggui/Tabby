package com.github.kr328.clash.common.compat

fun tabbyUriPermissionGrantFlags(
  read: Boolean,
  write: Boolean,
  readFlag: Int,
  writeFlag: Int,
): Int {
  var flags = 0

  if (read) flags = flags or readFlag

  if (write) flags = flags or writeFlag

  return flags
}
