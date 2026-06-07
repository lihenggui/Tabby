package com.github.kr328.clash.profile.ui

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.kr328.clash.common.R as CommonR
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
      ProvidersEventState.Idle -> Unit
      is ProvidersEventState.ShowMessage -> {
        snackbarHostState.showSnackbar(message = event.message)
      }
    }
    viewModel.consumeEvent()
  }

  ProvidersContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    providers =
      uiState.toProviderListItems(
        formatTypeText = { typeText -> typeText.androidString(context) },
        formatElapsedMillis = { elapsed -> elapsedTimeTextString(context, elapsed) },
      ),
    onUpdateAll = viewModel::onUpdateAll,
    onUpdate = { _, provider -> viewModel.onUpdate(provider) },
  )
}

private fun ProviderTypeText.androidString(context: Context): String {
  return context.getString(
    CommonR.string.format_provider_type,
    typeToken.androidString(context),
    vehicleToken.androidString(context),
  )
}

private fun ProviderTypeTextToken.androidString(context: Context): String {
  return when (this) {
    ProviderTypeTextToken.Proxy -> context.getString(CommonR.string.proxy)
    ProviderTypeTextToken.Rule -> context.getString(CommonR.string.rule)
  }
}

private fun ProviderVehicleTextToken.androidString(context: Context): String {
  return when (this) {
    ProviderVehicleTextToken.Http -> context.getString(CommonR.string.http)
    ProviderVehicleTextToken.File -> context.getString(CommonR.string.file)
    ProviderVehicleTextToken.Inline -> context.getString(CommonR.string.inline)
    ProviderVehicleTextToken.Compatible -> context.getString(CommonR.string.compatible)
  }
}
