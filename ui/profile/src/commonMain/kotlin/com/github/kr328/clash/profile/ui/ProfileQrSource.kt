package com.github.kr328.clash.profile.ui

internal enum class ProfileQrResultKind {
  Success,
  UserCanceled,
  MissingPermission,
  Error,
}

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
