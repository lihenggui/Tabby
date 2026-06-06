package com.github.kr328.clash.profile.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.glue.util.elapsedIntervalString
import com.github.kr328.clash.glue.util.type
import com.github.kr328.clash.profile.vm.ProvidersViewModel
import com.github.kr328.clash.ui.lifecycle.viewModelWithLifecycle

@Composable
internal fun ProvidersScreen(
  modifier: Modifier = Modifier,
  viewModel: ProvidersViewModel = viewModelWithLifecycle(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val eventState by viewModel.eventState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val context = LocalContext.current

  LaunchedEffect(eventState) {
    when (val event = eventState) {
      Idle -> Unit
      is ShowMessage -> {
        snackbarHostState.showSnackbar(message = event.message)
      }
    }
    viewModel.consumeEvent()
  }

  ProvidersContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    providers =
      uiState.providers.map { state ->
        state.provider.toProviderListItem(
          currentTime = uiState.currentTime,
          updatedAt = state.updatedAt,
          updating = state.updating,
          formatType = { provider -> provider.type(context) },
          formatElapsedMillis = { elapsed -> elapsed.elapsedIntervalString(context) },
        )
      },
    onUpdateAll = viewModel::onUpdateAll,
    onUpdate = { _, provider -> viewModel.onUpdate(provider) },
  )
}
