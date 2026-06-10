package com.github.kr328.clash.common.compat

import kotlin.test.Test
import kotlin.test.assertEquals

class UriPermissionFlagsTest {
  @Test
  fun combinesReadAndWriteGrantFlagsWhenBothPermissionsAreRequested() {
    assertEquals(
      READ_GRANT_FLAG or WRITE_GRANT_FLAG,
      tabbyUriPermissionGrantFlags(
        read = true,
        write = true,
        readFlag = READ_GRANT_FLAG,
        writeFlag = WRITE_GRANT_FLAG,
      ),
    )
  }

  @Test
  fun keepsOnlyReadGrantFlagWhenWritePermissionIsNotRequested() {
    assertEquals(
      READ_GRANT_FLAG,
      tabbyUriPermissionGrantFlags(
        read = true,
        write = false,
        readFlag = READ_GRANT_FLAG,
        writeFlag = WRITE_GRANT_FLAG,
      ),
    )
  }

  @Test
  fun keepsOnlyWriteGrantFlagWhenReadPermissionIsNotRequested() {
    assertEquals(
      WRITE_GRANT_FLAG,
      tabbyUriPermissionGrantFlags(
        read = false,
        write = true,
        readFlag = READ_GRANT_FLAG,
        writeFlag = WRITE_GRANT_FLAG,
      ),
    )
  }

  @Test
  fun returnsNoGrantFlagsWhenNoPermissionIsRequested() {
    assertEquals(
      0,
      tabbyUriPermissionGrantFlags(
        read = false,
        write = false,
        readFlag = READ_GRANT_FLAG,
        writeFlag = WRITE_GRANT_FLAG,
      ),
    )
  }
}

private const val READ_GRANT_FLAG = 1
private const val WRITE_GRANT_FLAG = 1 shl 1
