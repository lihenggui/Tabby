package com.github.kr328.clash.profile.ui

internal fun <T> selectVisibleProfileFiles(
  files: List<T>,
  inBaseDirectory: Boolean,
  id: (T) -> String,
  size: (T) -> Long,
): List<T> {
  if (!inBaseDirectory) return files

  val configFile = files.firstOrNull { file -> id(file).endsWith(PROFILE_CONFIG_FILE_NAME) }
  return if (configFile == null || size(configFile) > 0) files else listOf(configFile)
}

private const val PROFILE_CONFIG_FILE_NAME = "config.yaml"
