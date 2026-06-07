package com.github.kr328.clash.settings.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.engine.android.AndroidEngineController
import com.github.kr328.clash.engine.api.EngineController
import com.github.kr328.clash.settings.ui.OverridePersistAction
import com.github.kr328.clash.settings.ui.overridePersistAction
import com.github.kr328.clash.settings.ui.overrideSettingsInitialConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class OverrideSettingsViewModel(app: Application) :
  AndroidViewModel(app), DefaultLifecycleObserver {
  private val engineController: EngineController = AndroidEngineController(app)
  @Volatile private var skipPersist = false

  val configuration: StateFlow<ConfigurationOverride>
    field = MutableStateFlow(overrideSettingsInitialConfiguration())

  init {
    viewModelScope.launch {
      configuration.value = engineController.queryPersistOverride()
    }
  }

  override fun onStop(owner: LifecycleOwner) {
    // Intended to use non-viewModel scope as we need the action to be called on disposed.
    Global.launch {
      when (val action = overridePersistAction(skipPersist, configuration.value)) {
        OverridePersistAction.Clear -> engineController.clearPersistOverride()
        is OverridePersistAction.Patch ->
          engineController.patchPersistOverride(action.configuration)
      }
    }
  }

  fun resetOverride() {
    skipPersist = true
  }

  fun setConfiguration(value: ConfigurationOverride) {
    skipPersist = false
    configuration.value = value
  }
}
