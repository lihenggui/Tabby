package com.github.kr328.clash.common.id

private const val TABBY_UNDEFINED_ID_PREFIX = 0x14000000
private const val TABBY_UNDEFINED_ID_MASK = 0x00FFFFFF

fun tabbyNextUndefinedId(current: Int): Int {
  return ((current and TABBY_UNDEFINED_ID_MASK) + 1) or TABBY_UNDEFINED_ID_PREFIX
}
