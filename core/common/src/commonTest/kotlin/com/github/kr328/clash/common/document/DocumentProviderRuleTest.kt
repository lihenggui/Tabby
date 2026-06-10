package com.github.kr328.clash.common.document

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DocumentProviderRuleTest {
  @Test
  fun nullOpenModeRequestsWriteByDefault() {
    assertTrue(tabbyDocumentOpenModeRequestsWrite(null))
  }

  @Test
  fun openModeRequestsWriteWhenItContainsW() {
    assertTrue(tabbyDocumentOpenModeRequestsWrite("rw"))
    assertTrue(tabbyDocumentOpenModeRequestsWrite("RWT"))
  }

  @Test
  fun readOnlyOpenModeDoesNotRequestWrite() {
    assertFalse(tabbyDocumentOpenModeRequestsWrite("r"))
  }

  @Test
  fun urlProfileConfigurationDocumentsAreReadOnly() {
    assertFalse(tabbyProfileConfigurationDocumentAllowsWritableOpen(profileIsFile = false))
    assertEquals(emptySet(), tabbyProfileConfigurationDocumentFlags(profileIsUrl = true))
  }

  @Test
  fun fileProfileConfigurationDocumentsAreWritable() {
    assertTrue(tabbyProfileConfigurationDocumentAllowsWritableOpen(profileIsFile = true))
    assertEquals(
      setOf(Flag.Writable),
      tabbyProfileConfigurationDocumentFlags(profileIsUrl = false),
    )
  }

  @Test
  fun nonUrlNonFileProfileConfigurationDocumentsExposeWritableFlagButRejectWritableOpen() {
    assertFalse(tabbyProfileConfigurationDocumentAllowsWritableOpen(profileIsFile = false))
    assertEquals(
      setOf(Flag.Writable),
      tabbyProfileConfigurationDocumentFlags(profileIsUrl = false),
    )
  }

  @Test
  fun virtualDirectoryDocumentsExposeVirtualFlagOnly() {
    assertEquals(setOf(Flag.Virtual), tabbyVirtualDirectoryDocumentFlags())
  }

  @Test
  fun providerFileDocumentsExposeWritableAndDeletableFlags() {
    assertEquals(setOf(Flag.Writable, Flag.Deletable), tabbyProviderFileDocumentFlags())
  }

  @Test
  fun documentProviderRootFlagsExposeLocalOnlyChildSupport() {
    assertEquals(
      0b11,
      tabbyDocumentProviderRootFlags(
        localOnlyFlag = 0b01,
        supportsIsChildFlag = 0b10,
      ),
    )
  }

  @Test
  fun documentListSortKeyOrdersDirectoriesBeforeFilesAndThenByName() {
    val documents =
      listOf(
        TestDocument(name = "z-file.yaml", isDirectory = false),
        TestDocument(name = "b-dir", isDirectory = true),
        TestDocument(name = "a-file.yaml", isDirectory = false),
        TestDocument(name = "a-dir", isDirectory = true),
      )

    assertEquals(
      listOf("a-dir", "b-dir", "a-file.yaml", "z-file.yaml"),
      documents
        .sortedWith(compareBy { tabbyDocumentListSortKey(it.isDirectory, it.name) })
        .map(TestDocument::name),
    )
  }

  @Test
  fun nullDocumentIdsAreNotChildDocuments() {
    assertFalse(tabbyDocumentIdIsChild(parentDocumentId = null, documentId = "/profile"))
    assertFalse(tabbyDocumentIdIsChild(parentDocumentId = "/", documentId = null))
  }

  @Test
  fun documentIdIsChildWhenItStartsWithParentDocumentId() {
    assertTrue(tabbyDocumentIdIsChild(parentDocumentId = "/", documentId = "/profile"))
    assertTrue(tabbyDocumentIdIsChild(parentDocumentId = "/profile", documentId = "/profile/file"))
  }

  @Test
  fun documentIdIsNotChildWhenItDoesNotStartWithParentDocumentId() {
    assertFalse(tabbyDocumentIdIsChild(parentDocumentId = "/other", documentId = "/profile/file"))
  }
}

private data class TestDocument(val name: String, val isDirectory: Boolean)
