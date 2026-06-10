package com.github.kr328.clash.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class AccessControlMode {
  AcceptAll,
  AcceptSelected,
  DenySelected,
}

data class AccessControlPackagePlan(
  val allowedPackages: Set<String> = emptySet(),
  val disallowedPackages: Set<String> = emptySet(),
)

fun accessControlPackagePlan(
  mode: AccessControlMode,
  selectedPackages: Set<String>,
  ownPackageName: String,
): AccessControlPackagePlan {
  return when (mode) {
    AccessControlMode.AcceptAll -> AccessControlPackagePlan()
    AccessControlMode.AcceptSelected ->
      AccessControlPackagePlan(allowedPackages = selectedPackages + ownPackageName)
    AccessControlMode.DenySelected ->
      AccessControlPackagePlan(disallowedPackages = selectedPackages - ownPackageName)
  }
}
