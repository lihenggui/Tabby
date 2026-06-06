package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail

class FileListItemMapperTest {
  @Test
  fun mapsProfileFilesInInputOrderUsingFormatters() {
    val files =
      listOf(
        TestProfileFile(
          id = "first",
          name = "first.yaml",
          sizeBytes = 128,
          lastModified = 700,
          isDirectory = false,
        ),
        TestProfileFile(
          id = "providers",
          name = "providers",
          sizeBytes = 4096,
          lastModified = 600,
          isDirectory = true,
        ),
        TestProfileFile(
          id = "second",
          name = "second.yaml",
          sizeBytes = 256,
          lastModified = 500,
          isDirectory = false,
        ),
      )

    val items =
      toFileListItems(
        files = files,
        currentTime = 1000,
        id = TestProfileFile::id,
        name = TestProfileFile::name,
        sizeBytes = TestProfileFile::sizeBytes,
        lastModified = TestProfileFile::lastModified,
        isDirectory = TestProfileFile::isDirectory,
        formatBytes = { "${it}B" },
        formatElapsedMillis = { "elapsed:$it" },
      )

    assertEquals(
      listOf(
        FileListItem(
          id = "first",
          name = "first.yaml",
          sizeBytes = 128,
          isDirectory = false,
          sizeText = "128B",
          updatedAtText = "elapsed:300",
        ),
        FileListItem(
          id = "providers",
          name = "providers",
          sizeBytes = 4096,
          isDirectory = true,
          sizeText = null,
          updatedAtText = null,
        ),
        FileListItem(
          id = "second",
          name = "second.yaml",
          sizeBytes = 256,
          isDirectory = false,
          sizeText = "256B",
          updatedAtText = "elapsed:500",
        ),
      ),
      items,
    )
  }

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

  @Test
  fun selectsProfileFileForListItemId() {
    val file = TestProfileFile(id = "config", name = "config.yaml")
    val item =
      FileListItem(
        id = "config",
        name = "config.yaml",
        sizeBytes = 128,
        isDirectory = false,
        sizeText = "128B",
        updatedAtText = "updated",
      )

    assertEquals(
      ProfileFileListItemSelection.Select(file),
      profileFileListItemSelection(item, mapOf(file.id to file)),
    )
  }

  @Test
  fun ignoresListItemWhenProfileFileIdIsMissing() {
    val item =
      FileListItem(
        id = "missing",
        name = "missing.yaml",
        sizeBytes = 128,
        isDirectory = false,
        sizeText = "128B",
        updatedAtText = "updated",
      )

    assertEquals(
      ProfileFileListItemSelection.Ignore,
      profileFileListItemSelection(
        item = item,
        fileById = mapOf("config" to TestProfileFile(id = "config", name = "config.yaml")),
      ),
    )
  }

  private data class TestProfileFile(
    val id: String,
    val name: String,
    val sizeBytes: Long = 0,
    val lastModified: Long = 0,
    val isDirectory: Boolean = false,
  )
}
