package com.github.kr328.clash.profile.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.common.util.grantPermissions
import com.github.kr328.clash.glue.model.ConfigFile
import com.github.kr328.clash.glue.util.elapsedIntervalString
import com.github.kr328.clash.profile.vm.FilesViewModel
import com.github.kr328.clash.ui.lifecycle.viewModelWithLifecycle
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid
import kotlinx.coroutines.delay
import me.saket.bytesize.binaryBytes

@Composable
internal fun FilesScreen(
  uuid: Uuid,
  modifier: Modifier = Modifier,
  viewModel: FilesViewModel = viewModelWithLifecycle(),
  onFinish: () -> Unit,
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val eventState by viewModel.eventState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  var pendingImportTarget by remember { mutableStateOf<ConfigFile?>(null) }
  var pendingExportSource by remember { mutableStateOf<ConfigFile?>(null) }
  var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

  val openFileLauncher = rememberLauncherForActivityResult(StartActivityForResult()) {}

  val importLauncher =
    rememberLauncherForActivityResult(GetContent()) { uri ->
      viewModel.onImportResult(uri, pendingImportTarget)
      pendingImportTarget = null
    }

  val exportLauncher =
    rememberLauncherForActivityResult(CreateDocument("text/plain")) { uri ->
      viewModel.onExportResult(uri, pendingExportSource)
      pendingExportSource = null
    }

  LaunchedEffect(uuid) { viewModel.init(uuid = uuid) }

  LaunchedEffect(Unit) {
    while (true) {
      delay(1.minutes)
      currentTime = System.currentTimeMillis()
    }
  }

  LaunchedEffect(eventState) {
    when (val event = eventState) {
      Idle -> Unit
      Finish -> {
        onFinish()
      }
      is OpenFile -> {
        openFileLauncher.launch(
          Intent(Intent.ACTION_VIEW).setDataAndType(event.uri, "text/plain").grantPermissions()
        )
      }
      is RequestImport -> {
        pendingImportTarget = event.targetConfigFile
        importLauncher.launch("*/*")
      }
      is RequestExport -> {
        pendingExportSource = event.sourceConfigFile
        exportLauncher.launch(event.sourceConfigFile.name)
      }
      is ShowMessage -> {
        snackbarHostState.showSnackbar(message = event.message)
      }
    }
    viewModel.consumeEvent()
  }

  val configFiles = uiState.configFiles
  val configFileById = remember(configFiles) { configFiles.associateBy(ConfigFile::id) }

  BackHandler(onBack = viewModel::onBack)

  FilesContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    files = configFiles.map { it.toFileListItem(context, currentTime) },
    currentInBaseDir = uiState.currentInBaseDir,
    configurationEditable = uiState.configurationEditable,
    onBack = viewModel::onBack,
    onOpen = { item -> configFileById[item.id]?.let(viewModel::onOpen) },
    onNew = { viewModel.onRequestImport(null) },
    onImport = { item -> configFileById[item.id]?.let(viewModel::onRequestImport) },
    onExport = { item -> configFileById[item.id]?.let(viewModel::onRequestExport) },
    onRename = { item, name -> configFileById[item.id]?.let { viewModel.onRename(it, name) } },
    onDelete = { item -> configFileById[item.id]?.let(viewModel::onDelete) },
  )
}

private fun ConfigFile.toFileListItem(context: Context, currentTime: Long): FileListItem {
  return FileListItem(
    id = id,
    name = name,
    sizeBytes = size,
    isDirectory = isDirectory,
    sizeText = if (isDirectory) null else size.binaryBytes.toString(),
    updatedAtText =
      if (isDirectory) null else (currentTime - lastModified).elapsedIntervalString(context),
  )
}
