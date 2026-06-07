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
    when (val action = providersEventPlatformAction(eventState)) {
      ProvidersEventPlatformAction.Ignore -> Unit
      is ProvidersEventPlatformAction.ShowMessage -> {
        snackbarHostState.showSnackbar(message = action.message)
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
  return context.getString(
    providerTypeTextPlatformToken(
      token = this,
      proxy = CommonR.string.proxy,
      rule = CommonR.string.rule,
    )
  )
}

private fun ProviderVehicleTextToken.androidString(context: Context): String {
  return context.getString(
    providerVehicleTextPlatformToken(
      token = this,
      http = CommonR.string.http,
      file = CommonR.string.file,
      inline = CommonR.string.inline,
      compatible = CommonR.string.compatible,
    )
  )
}
