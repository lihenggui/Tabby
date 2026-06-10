package com.github.kr328.clash.common.compat

fun tabbyReceiverRegistrationFlags(
  permission: String?,
  exportedFlag: Int,
  notExportedFlag: Int,
): Int {
  return if (permission == null) exportedFlag else notExportedFlag
}
