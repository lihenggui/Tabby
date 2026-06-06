package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile

internal data class ProfileFilesUiState<T>(
  val configFiles: List<T> = emptyList(),
  val currentInBaseDir: Boolean = true,
  val configurationEditable: Boolean = false,
)

internal fun isProfileConfigurationEditable(profile: Profile): Boolean {
  return profile.type == Profile.Type.Url
}

internal fun <T> ProfileFilesUiState<T>.withConfigurationEditable(
  configurationEditable: Boolean
): ProfileFilesUiState<T> {
  return copy(configurationEditable = configurationEditable)
}

internal fun <T> ProfileFilesUiState<T>.withConfigFiles(
  configFiles: List<T>,
  currentInBaseDir: Boolean,
): ProfileFilesUiState<T> {
  return copy(configFiles = configFiles, currentInBaseDir = currentInBaseDir)
}
