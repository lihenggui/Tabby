package com.github.kr328.clash.app

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
import com.github.kr328.clash.profile.ui.ProviderRouteItem
import com.github.kr328.clash.profile.ui.ProvidersRouteContent
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun EngineControllerProvidersRouteContent(
  engineController: EngineController,
  modifier: Modifier = Modifier,
  onActionError: (Throwable) -> Unit = {},
) {
  val scope = rememberCoroutineScope()
  var providers by remember(engineController) { mutableStateOf(emptyList<ProviderRouteItem>()) }
  var currentTimeMillis by remember(engineController) { mutableStateOf(tabbyCurrentTimeMillis()) }

  suspend fun fetchProviders() {
    val fetchedProviders = engineController.queryProviders()

    providers = tabbyProviderRouteItemsAfterFetch(providers, fetchedProviders)
  }

  fun launchProviderUpdate(provider: Provider) {
    providers = tabbyProviderRouteItemsAfterUpdateStarted(providers, provider)

    scope.launch {
      runCatching {
          engineController.updateProvider(provider.type, provider.name)
          providers = tabbyProviderRouteItemsAfterUpdateFinished(providers, provider)
          fetchProviders()
        }
        .onFailure { cause ->
          providers = tabbyProviderRouteItemsAfterUpdateFinished(providers, provider)
          onActionError(cause)
        }
    }
  }

  LaunchedEffect(engineController) { runCatching { fetchProviders() }.onFailure(onActionError) }
  LaunchedEffect(engineController) {
    while (true) {
      delay(1.minutes)
      currentTimeMillis = tabbyCurrentTimeMillis()
    }
  }

  ProvidersRouteContent(
    modifier = modifier,
    providers = providers,
    currentTimeMillis = currentTimeMillis,
    onUpdateAll = {
      tabbyProviderRouteItemsPendingUpdateProviders(providers).forEach { provider ->
        launchProviderUpdate(provider)
      }
    },
    onUpdate = ::launchProviderUpdate,
  )
}

private fun tabbyCurrentTimeMillis(): Long {
  return Clock.System.now().toEpochMilliseconds()
}
