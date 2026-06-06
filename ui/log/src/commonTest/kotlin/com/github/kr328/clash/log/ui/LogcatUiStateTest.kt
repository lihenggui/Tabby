package com.github.kr328.clash.log.ui

import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.log.model.LogFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogcatUiStateTest {
  @Test
  fun logcatInitialActionStartsStreamingWhenNoFileNameIsProvided() {
    assertEquals(
      LogcatInitialAction.StartStreaming,
      logcatInitialAction(null),
    )
  }

  @Test
  fun logcatInitialActionLoadsParsedLogFile() {
    assertEquals(
      LogcatInitialAction.LoadFile(LogFile("clash-1234.log", 1234)),
      logcatInitialAction("clash-1234.log"),
    )
  }

  @Test
  fun logcatInitialActionRejectsInvalidFileName() {
    assertEquals(
      LogcatInitialAction.InvalidFile,
      logcatInitialAction("clash.log"),
    )
  }

  @Test
  fun streamingUpdatesPreserveMessagesAndExportProgress() {
    val message = logMessage(1)
    val state =
      LogcatUiState(
          messages = listOf(message),
          exportProgress = LogcatExportProgress(visible = true),
        )
        .withStreaming(false)

    assertFalse(state.streaming)
    assertEquals(listOf(message), state.messages)
    assertTrue(state.exportProgress.visible)
  }

  @Test
  fun messagesUpdatesPreserveStreamingAndExportProgress() {
    val progress = LogcatExportProgress(visible = true, max = 3)
    val messages = listOf(logMessage(1), logMessage(2))
    val state = LogcatUiState(streaming = false, exportProgress = progress).withMessages(messages)

    assertFalse(state.streaming)
    assertEquals(messages, state.messages)
    assertEquals(progress, state.exportProgress)
  }

  @Test
  fun exportStartShowsIndeterminateProgressWithMax() {
    val state = LogcatUiState().withExportStarted(max = 4)

    assertEquals(
      LogcatExportProgress(
        visible = true,
        isIndeterminate = true,
        progress = 0,
        max = 4,
      ),
      state.exportProgress,
    )
  }

  @Test
  fun exportProgressSwitchesToDeterminateAndPreservesMax() {
    val state = LogcatUiState().withExportStarted(max = 4).withExportProgress(progress = 2)

    assertEquals(
      LogcatExportProgress(
        visible = true,
        isIndeterminate = false,
        progress = 2,
        max = 4,
      ),
      state.exportProgress,
    )
  }

  @Test
  fun exportFinishResetsProgressOnly() {
    val message = logMessage(1)
    val state =
      LogcatUiState(streaming = false, messages = listOf(message))
        .withExportStarted(max = 1)
        .withExportProgress(progress = 1)
        .withExportFinished()

    assertFalse(state.streaming)
    assertEquals(listOf(message), state.messages)
    assertEquals(LogcatExportProgress(), state.exportProgress)
  }

  private fun logMessage(time: Long): LogMessage {
    return LogMessage(LogMessage.Level.Info, "message-$time", time)
  }
}
