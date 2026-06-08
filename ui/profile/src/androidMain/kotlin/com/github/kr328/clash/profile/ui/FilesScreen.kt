package com.github.kr328.clash.profile.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.common.util.grantPermissions
import com.github.kr328.clash.engine.android.AndroidProfileRepository
import com.github.kr328.clash.glue.remote.FilesClient
import com.github.kr328.clash.glue.remote.FilesClientDocument
import com.github.kr328.clash.glue.util.fileName
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.MutableSharedFlow

@Composable
internal fun FilesScreen(
  uuid: Uuid,
  modifier: Modifier = Modifier,
  onFinish: () -> Unit,
) {
  val context = LocalContext.current
  val profileRepository = remember { AndroidProfileRepository() }
  val filesClient = remember(context) { FilesClient(context) }
  val documentClient = remember(filesClient) { AndroidProfileFilesDocumentClient(filesClient) }
  val snackbarHostState = remember { SnackbarHostState() }
  val lifecycleOwner = LocalLifecycleOwner.current
  val refreshEvents = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }
  val importResults = remember {
    MutableSharedFlow<ProfileFilesImportResult<Uri>>(extraBufferCapacity = 1)
  }
  val exportResults = remember {
    MutableSharedFlow<ProfileFilesExportResult<Uri>>(extraBufferCapacity = 1)
  }

  var pendingImportTarget by remember { mutableStateOf<ProfileFilesDocument?>(null) }
  var pendingExportSource by remember { mutableStateOf<ProfileFilesDocument?>(null) }

  val openFileLauncher = rememberLauncherForActivityResult(StartActivityForResult()) {}

  val importLauncher =
    rememberLauncherForActivityResult(GetContent()) { uri ->
      importResults.tryEmit(
        profileFilesImportResultFromPlatformPayload(
          source = uri,
          sourceFileName = uri?.fileName,
          pendingTargetDocument = pendingImportTarget,
        )
      )
      pendingImportTarget = null
    }

  val exportLauncher =
    rememberLauncherForActivityResult(CreateDocument("text/plain")) { uri ->
      exportResults.tryEmit(
        profileFilesExportResultFromPlatformPayload(
          output = uri,
          pendingSourceDocument = pendingExportSource,
        )
      )
      pendingExportSource = null
    }

  DisposableEffect(lifecycleOwner, refreshEvents) {
    val observer = LifecycleEventObserver { _, event ->
      if (
        profileFilesRefreshRequestedFromStartEvent(isStartEvent = event == Lifecycle.Event.ON_START)
      ) {
        refreshEvents.tryEmit(Unit)
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  ProfileRepositoryFilesRouteContent(
    profileRepository = profileRepository,
    documentClient = documentClient,
    uuid = uuid,
    onFinish = onFinish,
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    importResults = importResults,
    exportResults = exportResults,
    refreshEvents = refreshEvents,
    formatElapsedMillis = { elapsed -> elapsedTimeTextString(context, elapsed) },
    onOpenFile = { documentId ->
      openFileLauncher.launch(
        Intent(Intent.ACTION_VIEW)
          .setDataAndType(filesClient.buildDocumentUri(documentId), "text/plain")
          .grantPermissions()
      )
    },
    onRequestImport = { target ->
      pendingImportTarget = target
      importLauncher.launch("*/*")
    },
    onRequestExport = { source ->
      pendingExportSource = source
      exportLauncher.launch(source.name)
    },
    onActionError = { cause -> Log.e("Profile files action failed: ${cause.message}", cause) },
  )
}

private class AndroidProfileFilesDocumentClient(private val client: FilesClient) :
  ProfileFilesDocumentClient<Uri, Uri> {
  override suspend fun list(parentDocumentId: String): List<ProfileFilesDocument> {
    return client.list(parentDocumentId).map(FilesClientDocument::toProfileFilesDocument)
  }

  override suspend fun renameDocument(documentId: String, name: String) {
    client.renameDocument(documentId, name)
  }

  override suspend fun deleteDocument(documentId: String) {
    client.deleteDocument(documentId)
  }

  override suspend fun importDocument(parentDocumentId: String, source: Uri, name: String) {
    client.importDocument(parentDocumentId, source, name)
  }

  override suspend fun replaceDocument(documentId: String, source: Uri) {
    client.copyDocument(documentId, source)
  }

  override suspend fun exportDocument(output: Uri, documentId: String) {
    client.copyDocument(output, documentId)
  }
}

private fun FilesClientDocument.toProfileFilesDocument(): ProfileFilesDocument {
  return profileFilesDocumentFromPlatformPayload(
    id = id,
    name = name,
    sizeBytes = size,
    lastModified = lastModified,
    isDirectory = isDirectory,
  )
}
