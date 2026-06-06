package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail

class FileListItemMapperTest {
  @Test
  fun mapsRegularFileDisplayState() {
    val item =
      toFileListItem(
        id = "file-id",
        name = "config.yaml",
        sizeBytes = 2048,
        lastModified = 700,
        isDirectory = false,
        currentTime = 1000,
        formatBytes = { "${it}B" },
        formatElapsedMillis = { "elapsed:$it" },
      )

    assertEquals("file-id", item.id)
    assertEquals("config.yaml", item.name)
    assertEquals(2048, item.sizeBytes)
    assertEquals(false, item.isDirectory)
    assertEquals("2048B", item.sizeText)
    assertEquals("elapsed:300", item.updatedAtText)
  }

  @Test
  fun hidesDirectorySizeAndUpdatedAtText() {
    val item =
      toFileListItem(
        id = "dir-id",
        name = "providers",
        sizeBytes = 4096,
        lastModified = 700,
        isDirectory = true,
        currentTime = 1000,
        formatBytes = { fail("Directories should not format size") },
        formatElapsedMillis = { fail("Directories should not format updated-at text") },
      )

    assertEquals("dir-id", item.id)
    assertEquals("providers", item.name)
    assertEquals(4096, item.sizeBytes)
    assertEquals(true, item.isDirectory)
    assertNull(item.sizeText)
    assertNull(item.updatedAtText)
  }
}
