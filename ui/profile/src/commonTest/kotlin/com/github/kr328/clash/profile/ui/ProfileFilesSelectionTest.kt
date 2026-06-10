package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileFilesSelectionTest {
  @Test
  fun returnsAllFilesOutsideBaseDirectory() {
    val files =
      listOf(
        file(id = "root/config.yaml", size = 0),
        file(id = "root/providers", size = 0),
      )

    assertEquals(files, files.selectVisibleProfileFiles(inBaseDirectory = false))
  }

  @Test
  fun returnsAllBaseDirectoryFilesWhenConfigFileIsMissing() {
    val files =
      listOf(
        file(id = "root/providers", size = 0),
        file(id = "root/rules", size = 0),
      )

    assertEquals(files, files.selectVisibleProfileFiles(inBaseDirectory = true))
  }

  @Test
  fun returnsAllBaseDirectoryFilesWhenConfigFileHasContent() {
    val files =
      listOf(
        file(id = "root/config.yaml", size = 128),
        file(id = "root/providers", size = 0),
      )

    assertEquals(files, files.selectVisibleProfileFiles(inBaseDirectory = true))
  }

  @Test
  fun returnsOnlyEmptyConfigFileInBaseDirectory() {
    val config = file(id = "root/config.yaml", size = 0)
    val files = listOf(file(id = "root/providers", size = 0), config)

    assertEquals(listOf(config), files.selectVisibleProfileFiles(inBaseDirectory = true))
  }

  @Test
  fun matchesConfigFileByDocumentIdSuffix() {
    val config = file(id = "profile:documents/config.yaml", size = 0)
    val files = listOf(config, file(id = "profile:documents/config.yaml.bak", size = 0))

    assertEquals(listOf(config), files.selectVisibleProfileFiles(inBaseDirectory = true))
  }

  private fun List<TestFile>.selectVisibleProfileFiles(inBaseDirectory: Boolean): List<TestFile> {
    return selectVisibleProfileFiles(
      files = this,
      inBaseDirectory = inBaseDirectory,
      id = TestFile::id,
      size = TestFile::size,
    )
  }

  private fun file(id: String, size: Long): TestFile {
    return TestFile(id = id, size = size)
  }

  private data class TestFile(val id: String, val size: Long)
}
