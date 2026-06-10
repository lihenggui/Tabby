package com.github.kr328.clash.core.model

enum class ProfileFieldValidationError {
  EmptyName,
  MissingSource,
  UnsupportedSource,
  InvalidInterval,
}

fun profileFieldValidationErrorMessage(
  error: ProfileFieldValidationError,
  source: String,
): String {
  return when (error) {
    ProfileFieldValidationError.EmptyName -> "Empty name"
    ProfileFieldValidationError.MissingSource -> "Invalid url"
    ProfileFieldValidationError.UnsupportedSource -> "Unsupported url $source"
    ProfileFieldValidationError.InvalidInterval -> "Invalid interval"
  }
}

fun profileFieldValidationError(
  type: Profile.Type,
  name: String,
  sourceMissing: Boolean,
  sourceSupported: Boolean,
  interval: Long,
): ProfileFieldValidationError? {
  return when {
    name.isBlank() -> ProfileFieldValidationError.EmptyName
    sourceMissing && type != Profile.Type.File -> ProfileFieldValidationError.MissingSource
    !sourceMissing && !sourceSupported -> ProfileFieldValidationError.UnsupportedSource
    !isValidProfileAutoUpdateIntervalMillis(interval) -> ProfileFieldValidationError.InvalidInterval
    else -> null
  }
}
