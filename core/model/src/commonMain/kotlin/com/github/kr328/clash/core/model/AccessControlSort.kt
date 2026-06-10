package com.github.kr328.clash.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class AccessControlSort {
  Label,
  PackageName,
  InstallTime,
  UpdateTime,
}
