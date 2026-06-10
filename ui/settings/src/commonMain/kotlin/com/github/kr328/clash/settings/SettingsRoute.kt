package com.github.kr328.clash.settings

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface SettingsRoute : NavKey {
  @Serializable data object Root : SettingsRoute

  @Serializable data object AppSettings : SettingsRoute

  @Serializable data object NetworkSettings : SettingsRoute

  @Serializable data object OverrideSettings : SettingsRoute

  @Serializable data object MetaFeatureSettings : SettingsRoute

  @Serializable data object AccessControl : SettingsRoute
}
