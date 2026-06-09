package com.github.kr328.clash.profile.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.kr328.clash.engine.api.ProfileRepository
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.unknown

@Composable
fun <SourceT : Any, OutputT : Any> ProfileRepositoryFilesRouteContent(
  profileRepository: ProfileRepository,
  documentClient: ProfileFilesDocumentClient<SourceT, OutputT>,
  uuid: Uuid,
  onFinish: () -> Unit,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  importResults: Flow<ProfileFilesImportResult<SourceT>> = emptyFlow(),
  exportResults: Flow<ProfileFilesExportResult<OutputT>> = emptyFlow(),
  refreshEvents: Flow<Unit> = emptyFlow(),
  formatElapsedMillis: @Composable (Long) -> String = { elapsedTimeTextString(it) },
  onOpenFile: (documentId: String) -> Unit = {},
  onRequestImport: (ProfileFilesDocument?) -> Unit = {},
  onRequestExport: (ProfileFilesDocument) -> Unit = {},
  onActionError: (Throwable) -> Unit = {},
) {
  val scope = rememberCoroutineScope()
  val unknownMessage = stringResource(SharedRes.string.unknown)
  var location by
    remember(profileRepository, documentClient, uuid) {
      mutableStateOf(ProfileFilesLocation())
    }
  var uiState by
    remember(profileRepository, documentClient, uuid) {
      mutableStateOf(profileFilesInitialUiState<ProfileFilesDocument>())
    }
  var eventState by
    remember(profileRepository, documentClient, uuid) {
      mutableStateOf(profileFilesInitialEventState<ProfileFilesDocument, String>())
    }
  var currentTimeMillis by
    remember(profileRepository, documentClient, uuid) {
      mutableLongStateOf(tabbyCurrentTimeMillis())
    }
  var fetchJob by remember(profileRepository, documentClient, uuid) { mutableStateOf<Job?>(null) }

  fun showError(cause: Throwable) {
    onActionError(cause)
    eventState = profileFilesErrorEventState(cause.message, unknownMessage)
  }

  suspend fun fetchFiles() {
    val action =
      when (val fetchAction = profileFilesFetchAction(location)) {
        is ProfileFilesFetchAction.Fetch -> fetchAction
        ProfileFilesFetchAction.Ignore -> return
      }

    try {
      val files =
        selectVisibleProfileFiles(
          files = documentClient.list(action.documentId),
          inBaseDirectory = action.inBaseDirectory,
          id = ProfileFilesDocument::id,
          size = ProfileFilesDocument::sizeBytes,
        )

      uiState =
        uiState.withConfigFiles(
          configFiles = files,
          currentInBaseDir = action.inBaseDirectory,
        )
    } catch (cause: CancellationException) {
      throw cause
    } catch (cause: Exception) {
      showError(cause)
    }
  }

  fun launchFetch() {
    fetchJob?.cancel()
    fetchJob = scope.launch { fetchFiles() }
  }

  fun launchDocumentAction(block: suspend () -> Unit) {
    scope.launch {
      var refreshAfterAction = true
      try {
        block()
      } catch (cause: CancellationException) {
        refreshAfterAction = false
        throw cause
      } catch (cause: Exception) {
        showError(cause)
      } finally {
        if (refreshAfterAction) {
          launchFetch()
        }
      }
    }
  }

  fun openDocument(document: ProfileFilesDocument) {
    when (val action = profileFileOpenAction(document.id, document.isDirectory)) {
      is ProfileFileOpenAction.EnterDirectory -> {
        location = location.enterDirectory(action.documentId)
        launchFetch()
      }
      is ProfileFileOpenAction.OpenFile ->
        profileFileOpenEventState(action, action.documentId)?.let { eventState = it }
    }
  }

  fun back() {
    when (val action = profileFilesBackAction(location)) {
      is ProfileFilesBackAction.LeaveDirectory -> {
        location = action.location
        launchFetch()
      }
      ProfileFilesBackAction.Finish -> profileFilesBackEventState(action)?.let { eventState = it }
    }
  }

  fun handleImportResult(result: ProfileFilesImportResult<SourceT>) {
    when (
      val action =
        profileFileImportResolvedAction(
          source = result.source,
          sourceFileName = result.sourceFileName,
          targetDocumentId = result.targetDocumentId,
          parentDocumentId = location.currentDocumentId,
        )
    ) {
      is ProfileFileImportResolvedAction.ImportNewFile ->
        launchDocumentAction {
          documentClient.importDocument(action.parentDocumentId, action.source, action.fileName)
        }
      is ProfileFileImportResolvedAction.ReplaceFile ->
        launchDocumentAction {
          documentClient.replaceDocument(action.targetDocumentId, action.source)
        }
      ProfileFileImportResolvedAction.Ignore -> Unit
    }
  }

  fun handleExportResult(result: ProfileFilesExportResult<OutputT>) {
    when (
      val action =
        profileFileExportResolvedAction(
          output = result.output,
          sourceDocumentId = result.sourceDocumentId,
        )
    ) {
      is ProfileFileExportResolvedAction.ExportFile ->
        launchDocumentAction {
          documentClient.exportDocument(action.output, action.sourceDocumentId)
        }
      ProfileFileExportResolvedAction.Ignore -> Unit
    }
  }

  LaunchedEffect(profileRepository, documentClient, uuid) {
    when (val action = profileFilesInitAction(location, uuid.toString())) {
      is ProfileFilesInitAction.Initialize -> location = action.location
      ProfileFilesInitAction.Ignore -> Unit
    }

    val action =
      try {
        profileFilesLoadedAction(profileRepository.queryByUuid(uuid))
      } catch (cause: CancellationException) {
        throw cause
      } catch (cause: Exception) {
        showError(cause)
        null
      }

    when (action) {
      is ProfileFilesLoadedAction.LoadFiles -> {
        uiState = uiState.withConfigurationEditable(action.configurationEditable)
        launchFetch()
      }
      ProfileFilesLoadedAction.Finish ->
        profileFilesLoadedEventState(action)?.let { eventState = it }
      null -> Unit
    }
  }
  LaunchedEffect(profileRepository, documentClient, uuid, refreshEvents) {
    refreshEvents.collect {
      if (location.initialized) {
        launchFetch()
      }
    }
  }
  LaunchedEffect(profileRepository, documentClient, uuid, importResults) {
    importResults.collect(::handleImportResult)
  }
  LaunchedEffect(profileRepository, documentClient, uuid, exportResults) {
    exportResults.collect(::handleExportResult)
  }
  LaunchedEffect(profileRepository, documentClient, uuid) {
    while (true) {
      delay(1.minutes)
      currentTimeMillis = tabbyCurrentTimeMillis()
    }
  }
  LaunchedEffect(eventState) {
    when (val action = profileFilesEventPlatformAction(eventState)) {
      ProfileFilesEventPlatformAction.Ignore -> Unit
      ProfileFilesEventPlatformAction.Finish -> onFinish()
      is ProfileFilesEventPlatformAction.OpenFile -> onOpenFile(action.uri)
      is ProfileFilesEventPlatformAction.RequestImport -> onRequestImport(action.targetConfigFile)
      is ProfileFilesEventPlatformAction.RequestExport -> onRequestExport(action.sourceConfigFile)
      is ProfileFilesEventPlatformAction.ShowMessage ->
        snackbarHostState.showSnackbar(message = action.message)
    }
    eventState = profileFilesConsumedEventState()
  }

  val documents = uiState.configFiles
  val documentById = remember(documents) { documents.associateBy(ProfileFilesDocument::id) }

  fun handleSelectedRouteItem(item: ProfileFileRouteItem, action: (ProfileFilesDocument) -> Unit) {
    documentById[item.id]?.let(action)
  }

  FilesRouteContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    files = documents.map(ProfileFilesDocument::toProfileFileRouteItem),
    currentTimeMillis = currentTimeMillis,
    currentInBaseDir = uiState.currentInBaseDir,
    configurationEditable = uiState.configurationEditable,
    formatElapsedMillis = formatElapsedMillis,
    onBack = ::back,
    onOpen = { item -> handleSelectedRouteItem(item, ::openDocument) },
    onNew = { eventState = profileFileImportRequestEventState(null) },
    onImport = { item ->
      handleSelectedRouteItem(item) { document ->
        eventState = profileFileImportRequestEventState(document)
      }
    },
    onExport = { item ->
      handleSelectedRouteItem(item) { document ->
        eventState = profileFileExportRequestEventState(document)
      }
    },
    onRename = { item, name ->
      handleSelectedRouteItem(item) { document ->
        launchDocumentAction { documentClient.renameDocument(document.id, name) }
      }
    },
    onDelete = { item ->
      handleSelectedRouteItem(item) { document ->
        launchDocumentAction { documentClient.deleteDocument(document.id) }
      }
    },
  )
}
