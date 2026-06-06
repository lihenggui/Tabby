package com.github.kr328.clash.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class DarkMode {
  Auto,
  ForceLight,
  ForceDark,
}
