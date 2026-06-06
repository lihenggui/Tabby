package com.github.kr328.clash.log.ui

import com.github.kr328.clash.log.model.LogFile
import kotlin.test.Test
import kotlin.test.assertEquals

class LogListItemMapperTest {
  @Test
  fun mapsLogFilesInInputOrderUsingCreatedFormatter() {
    val items =
      toLogListItems(
        files =
          listOf(
            LogFile(fileName = "clash-20.log", created = 20),
            LogFile(fileName = "clash-10.log", created = 10),
            LogFile(fileName = "clash-30.log", created = 30),
          ),
        formatCreated = { "created:$it" },
      )

    assertEquals(
      listOf(
        LogListItem(fileName = "clash-20.log", createdText = "created:20"),
        LogListItem(fileName = "clash-10.log", createdText = "created:10"),
        LogListItem(fileName = "clash-30.log", createdText = "created:30"),
      ),
      items,
    )
  }

  @Test
  fun createsOpenFileActionForSelectedLogListItem() {
    val action =
      logListItemOpenAction(LogListItem(fileName = "clash-42.log", createdText = "ignored"))

    assertEquals(
      LogListItemOpenAction.OpenFile(LogFile(fileName = "clash-42.log", created = 42)),
      action,
    )
  }

  @Test
  fun ignoresInvalidSelectedLogListItem() {
    val action =
      logListItemOpenAction(LogListItem(fileName = "clash-latest.log", createdText = "ignored"))

    assertEquals(LogListItemOpenAction.Ignore, action)
  }
}
