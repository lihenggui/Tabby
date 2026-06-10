package com.github.kr328.clash.profile.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.kr328.clash.core.model.Provider
import com.github.kr328.clash.engine.api.EngineController
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tabby.ui.profile.generated.resources.Res as ProfileRes
import tabby.ui.profile.generated.resources.format_update_provider_failure

@Composable
fun EngineControllerProvidersRouteContent(
  engineController: EngineController,
  modifier: Modifier = Modifier,
  broadcastEvents: Flow<ProvidersBroadcastEvent> = emptyFlow(),
  onActionError: (Throwable) -> Unit = {},
) {
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  var providers by remember(engineController) { mutableStateOf(emptyList<ProviderRouteItem>()) }
  var currentTimeMillis by remember(engineController) { mutableStateOf(tabbyCurrentTimeMillis()) }
  var updateFailureEvent by
    remember(engineController) { mutableStateOf<ProviderUpdateFailureEvent?>(null) }

  val updateFailureMessage = updateFailureEvent?.let { event ->
    stringResource(
      ProfileRes.string.format_update_provider_failure,
      event.providerName,
      event.errorMessage,
    )
  }

  suspend fun fetchProviders() {
    val fetchedProviders = engineController.queryProviders()

    providers = providerRouteItemsAfterFetch(providers, fetchedProviders)
  }

  fun launchProviderUpdate(provider: Provider) {
    providers = providerRouteItemsAfterUpdateStarted(providers, provider)

    scope.launch {
      runCatching {
          engineController.updateProvider(provider.type, provider.name)
          providers = providerRouteItemsAfterUpdateFinished(providers, provider)
          fetchProviders()
        }
        .onFailure { cause ->
          providers = providerRouteItemsAfterUpdateFinished(providers, provider)
          updateFailureEvent = providerUpdateFailureEvent(provider, cause)
          onActionError(cause)
        }
    }
  }

  LaunchedEffect(updateFailureMessage) {
    if (updateFailureMessage != null) {
      snackbarHostState.showSnackbar(updateFailureMessage)
      updateFailureEvent = null
    }
  }
  LaunchedEffect(engineController) { runCatching { fetchProviders() }.onFailure(onActionError) }
  LaunchedEffect(engineController, broadcastEvents) {
    broadcastEvents.collect { event ->
      when (providersBroadcastAction(event)) {
        ProvidersBroadcastAction.FetchProviders ->
          runCatching { fetchProviders() }.onFailure(onActionError)
        ProvidersBroadcastAction.Ignore -> Unit
      }
    }
  }
  LaunchedEffect(engineController) {
    while (true) {
      delay(1.minutes)
      currentTimeMillis = tabbyCurrentTimeMillis()
    }
  }

  ProvidersRouteContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    providers = providers,
    currentTimeMillis = currentTimeMillis,
    onUpdateAll = {
      providerRouteItemsPendingUpdateProviders(providers).forEach { provider ->
        launchProviderUpdate(provider)
      }
    },
    onUpdate = ::launchProviderUpdate,
  )
}

internal fun tabbyCurrentTimeMillis(): Long {
  return Clock.System.now().toEpochMilliseconds()
}

internal data class ProviderUpdateFailureEvent(
  val providerName: String,
  val errorMessage: String,
)

internal fun providerUpdateFailureEvent(
  provider: Provider,
  cause: Throwable,
): ProviderUpdateFailureEvent {
  return ProviderUpdateFailureEvent(
    providerName = provider.name,
    errorMessage =
      providerUpdateFailureErrorMessage(
        localizedMessage = null,
        message = cause.message,
        fallbackMessage = cause.toString(),
      ),
  )
}
