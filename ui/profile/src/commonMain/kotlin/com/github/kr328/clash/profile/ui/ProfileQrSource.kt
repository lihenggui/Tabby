package com.github.kr328.clash.profile.ui

enum class ProfileQrResultKind {
  Success,
  UserCanceled,
  MissingPermission,
  Error,
}

class ProfileQrScanResult(
  val kind: ProfileQrResultKind,
  val rawValue: String? = null,
  val rawBytes: ByteArray? = null,
)

internal sealed interface ProfileQrAction {
  data class CreateUrlProfile(val source: String) : ProfileQrAction

  data object Ignore : ProfileQrAction

  data object ShowMissingPermission : ProfileQrAction

  data object ShowScanError : ProfileQrAction
}

internal fun decodeProfileQrSource(rawValue: String?, rawBytes: ByteArray?): String {
  return rawValue ?: rawBytes?.decodeToString().orEmpty()
}

internal fun profileQrAction(
  kind: ProfileQrResultKind,
  rawValue: String? = null,
  rawBytes: ByteArray? = null,
): ProfileQrAction {
  return when (kind) {
    ProfileQrResultKind.Success ->
      ProfileQrAction.CreateUrlProfile(decodeProfileQrSource(rawValue, rawBytes))
    ProfileQrResultKind.UserCanceled -> ProfileQrAction.Ignore
    ProfileQrResultKind.MissingPermission -> ProfileQrAction.ShowMissingPermission
    ProfileQrResultKind.Error -> ProfileQrAction.ShowScanError
  }
}

internal fun profileQrAction(result: ProfileQrScanResult): ProfileQrAction {
  return profileQrAction(
    kind = result.kind,
    rawValue = result.rawValue,
    rawBytes = result.rawBytes,
  )
}
