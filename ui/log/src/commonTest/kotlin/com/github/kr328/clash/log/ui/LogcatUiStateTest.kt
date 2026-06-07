package com.github.kr328.clash.log.ui

import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.log.model.LogFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogcatUiStateTest {
  @Test
  fun createsInitialLogcatUiState() {
    assertEquals(LogcatUiState(), logcatInitialUiState())
  }

  @Test
  fun createsInitialLogcatEventState() {
    assertEquals(LogcatEventState.Idle, logcatInitialEventState())
  }

  @Test
  fun logcatEventPlatformActionMapsEventStates() {
    assertEquals(
      LogcatEventPlatformAction.Ignore,
      logcatEventPlatformAction(LogcatEventState.Idle),
    )
    assertEquals(
      LogcatEventPlatformAction.Close,
      logcatEventPlatformAction(LogcatEventState.Close),
    )
    assertEquals(
      LogcatEventPlatformAction.InvalidFile,
      logcatEventPlatformAction(LogcatEventState.InvalidFile),
    )
    assertEquals(
      LogcatEventPlatformAction.OpenLogs,
      logcatEventPlatformAction(LogcatEventState.OpenLogs),
    )
    assertEquals(
      LogcatEventPlatformAction.RequestExport("clash-1234.log"),
      logcatEventPlatformAction(LogcatEventState.RequestExport("clash-1234.log")),
    )
    assertEquals(
      LogcatEventPlatformAction.ShowMessage("exported"),
      logcatEventPlatformAction(LogcatEventState.ShowMessage("exported")),
    )
  }

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
  fun logcatInitialEventStateOnlyRejectsInvalidFiles() {
    assertEquals(
      null,
      logcatInitialEventState(LogcatInitialAction.StartStreaming),
    )
    assertEquals(
      null,
      logcatInitialEventState(LogcatInitialAction.LoadFile(LogFile("clash-1234.log", 1234))),
    )
    assertEquals(
      LogcatEventState.InvalidFile,
      logcatInitialEventState(LogcatInitialAction.InvalidFile),
    )
  }

  @Test
  fun logcatFailureEventStatesMapToRecoveryEvents() {
    assertEquals(
      LogcatEventState.InvalidFile,
      logcatLoadFileFailureEventState(),
    )
    assertEquals(
      LogcatEventState.OpenLogs,
      logcatStartStreamingFailureEventState(),
    )
  }

  @Test
  fun consumedEventStateResetsToIdle() {
    assertEquals(LogcatEventState.Idle, logcatConsumedEventState())
  }

  @Test
  fun appendMessageKeepsMessagesWithinCapacity() {
    val first = logMessage(1)
    val second = logMessage(2)
    val third = logMessage(3)

    assertEquals(
      listOf(second, third),
      logcatMessagesAfterAppend(listOf(first, second), third, capacity = 2),
    )
  }

  @Test
  fun appendMessagePreservesMessagesBelowCapacity() {
    val first = logMessage(1)
    val second = logMessage(2)

    assertEquals(
      listOf(first, second),
      logcatMessagesAfterAppend(listOf(first), second, capacity = 2),
    )
  }

  @Test
  fun appendMessageRejectsInvalidCapacity() {
    assertFailsWith<IllegalArgumentException> {
      logcatMessagesAfterAppend(emptyList(), logMessage(1), capacity = 0)
    }
  }

  @Test
  fun logcatCloseActionStopsStreamingAndOpensLogsWhenStreaming() {
    assertEquals(
      LogcatCloseAction.StopStreamingAndOpenLogs,
      logcatCloseAction(LogcatUiState(streaming = true)),
    )
  }

  @Test
  fun logcatCloseActionClosesViewerWhenViewingLocalFile() {
    assertEquals(
      LogcatCloseAction.CloseViewer,
      logcatCloseAction(LogcatUiState(streaming = false)),
    )
  }

  @Test
  fun logcatCloseEventStateMapsCloseActionsToUiEvents() {
    assertEquals(
      LogcatEventState.OpenLogs,
      logcatCloseEventState(LogcatCloseAction.StopStreamingAndOpenLogs),
    )
    assertEquals(
      LogcatEventState.Close,
      logcatCloseEventState(LogcatCloseAction.CloseViewer),
    )
  }

  @Test
  fun logcatDeleteActionDeletesCurrentFileAndIgnoresMissingFile() {
    val file = LogFile("clash-1234.log", 1234)

    assertEquals(
      LogcatDeleteAction.DeleteFile(file),
      logcatDeleteAction(file),
    )
    assertEquals(
      LogcatDeleteAction.Ignore,
      logcatDeleteAction(null),
    )
  }

  @Test
  fun logcatDeleteEventStateClosesOnlyAfterDeletingAFile() {
    val file = LogFile("clash-1234.log", 1234)

    assertEquals(
      LogcatEventState.Close,
      logcatDeleteEventState(LogcatDeleteAction.DeleteFile(file)),
    )
    assertEquals(null, logcatDeleteEventState(LogcatDeleteAction.Ignore))
  }

  @Test
  fun logcatRequestExportActionRequestsCurrentFileNameAndIgnoresMissingFile() {
    val file = LogFile("clash-1234.log", 1234)

    assertEquals(
      LogcatRequestExportAction.RequestExport("clash-1234.log"),
      logcatRequestExportAction(file),
    )
    assertEquals(
      LogcatRequestExportAction.Ignore,
      logcatRequestExportAction(null),
    )
  }

  @Test
  fun logcatRequestExportEventStateRequestsExportOnlyForExportAction() {
    assertEquals(
      LogcatEventState.RequestExport("clash-1234.log"),
      logcatRequestExportEventState(LogcatRequestExportAction.RequestExport("clash-1234.log")),
    )
    assertEquals(null, logcatRequestExportEventState(LogcatRequestExportAction.Ignore))
  }

  @Test
  fun logcatExportPlatformPayloadMapsDestinationSelection() {
    assertEquals(
      LogcatExportResult(destinationSelected = true),
      logcatExportResultFromPlatformPayload(destinationSelected = true),
    )
    assertEquals(
      LogcatExportResult(destinationSelected = false),
      logcatExportResultFromPlatformPayload(destinationSelected = false),
    )
  }

  @Test
  fun logcatExportActionExportsCurrentFileOnlyWhenDestinationExists() {
    val file = LogFile("clash-1234.log", 1234)

    assertEquals(
      LogcatExportAction.ExportFile(file),
      logcatExportAction(
        currentFile = file,
        result = LogcatExportResult(destinationSelected = true),
      ),
    )
    assertEquals(
      LogcatExportAction.Ignore,
      logcatExportAction(
        currentFile = file,
        result = LogcatExportResult(destinationSelected = false),
      ),
    )
    assertEquals(
      LogcatExportAction.Ignore,
      logcatExportAction(
        currentFile = null,
        result = LogcatExportResult(destinationSelected = true),
      ),
    )
  }

  @Test
  fun logcatExportResultEventStateShowsSuccessOrFallbackErrorMessage() {
    assertEquals(
      LogcatEventState.ShowMessage("exported"),
      logcatExportResultEventState(
        success = true,
        errorMessage = null,
        exportedMessage = "exported",
        unknownMessage = "unknown",
      ),
    )
    assertEquals(
      LogcatEventState.ShowMessage("write failed"),
      logcatExportResultEventState(
        success = false,
        errorMessage = "write failed",
        exportedMessage = "exported",
        unknownMessage = "unknown",
      ),
    )
    assertEquals(
      LogcatEventState.ShowMessage("unknown"),
      logcatExportResultEventState(
        success = false,
        errorMessage = null,
        exportedMessage = "exported",
        unknownMessage = "unknown",
      ),
    )
  }

  @Test
  fun logcatPollActionQueriesSnapshotsOnlyWhenStarted() {
    assertEquals(
      LogcatPollAction.QuerySnapshot(initialSnapshot = true),
      logcatPollAction(started = true, initialSnapshot = true),
    )
    assertEquals(
      LogcatPollAction.QuerySnapshot(initialSnapshot = false),
      logcatPollAction(started = true, initialSnapshot = false),
    )
    assertEquals(
      LogcatPollAction.Ignore,
      logcatPollAction(started = false, initialSnapshot = true),
    )
  }

  @Test
  fun logcatSnapshotActionPreservesStateAndInitialFlagWhenSnapshotIsMissing() {
    val message = logMessage(1)
    val state = LogcatUiState(messages = listOf(message))

    assertEquals(
      LogcatSnapshotAction(
        state = state,
        initialSnapshot = true,
      ),
      logcatSnapshotAction(
        state = state,
        initialSnapshot = true,
        messages = null,
      ),
    )
  }

  @Test
  fun logcatSnapshotActionReplacesMessagesAndClearsInitialFlagWhenSnapshotExists() {
    val oldMessage = logMessage(1)
    val newMessages = listOf(logMessage(2), logMessage(3))

    assertEquals(
      LogcatSnapshotAction(
        state = LogcatUiState(messages = newMessages),
        initialSnapshot = false,
      ),
      logcatSnapshotAction(
        state = LogcatUiState(messages = listOf(oldMessage)),
        initialSnapshot = true,
        messages = newMessages,
      ),
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

  @Test
  fun autoScrollToLatestRequiresMessagesAndBottomViewport() {
    assertTrue(logcatShouldAutoScrollToLatest(messageCount = 1, listAtBottom = true))
    assertFalse(logcatShouldAutoScrollToLatest(messageCount = 0, listAtBottom = true))
    assertFalse(logcatShouldAutoScrollToLatest(messageCount = 1, listAtBottom = false))
  }

  @Test
  fun listViewportIsAtBottomWhenNoItemsAreVisible() {
    assertTrue(
      logcatListViewportIsAtBottom(
        totalItemsCount = 0,
        lastVisibleItemIndex = null,
      )
    )
  }

  @Test
  fun listViewportIsAtBottomOnlyWhenLastItemFitsInsideViewport() {
    assertTrue(
      logcatListViewportIsAtBottom(
        totalItemsCount = 3,
        lastVisibleItemIndex = 2,
        lastVisibleItemOffset = 80,
        lastVisibleItemSize = 20,
        viewportEndOffset = 100,
      )
    )
    assertFalse(
      logcatListViewportIsAtBottom(
        totalItemsCount = 3,
        lastVisibleItemIndex = 1,
        lastVisibleItemOffset = 80,
        lastVisibleItemSize = 20,
        viewportEndOffset = 100,
      )
    )
    assertFalse(
      logcatListViewportIsAtBottom(
        totalItemsCount = 3,
        lastVisibleItemIndex = 2,
        lastVisibleItemOffset = 90,
        lastVisibleItemSize = 20,
        viewportEndOffset = 100,
      )
    )
  }

  private fun logMessage(time: Long): LogMessage {
    return LogMessage(LogMessage.Level.Info, "message-$time", time)
  }
}
