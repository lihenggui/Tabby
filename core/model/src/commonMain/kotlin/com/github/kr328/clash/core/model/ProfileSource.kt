package com.github.kr328.clash.core.model

fun isHttpProfileSource(source: String): Boolean {
  return source.startsWith("https://", ignoreCase = true) ||
    source.startsWith("http://", ignoreCase = true)
}

fun isHttpsProfileSource(source: String): Boolean {
  return source.startsWith("https://", ignoreCase = true)
}

fun profileShouldFetchSubscriptionUserInfo(type: Profile.Type, source: String): Boolean {
  return type == Profile.Type.Url && isHttpsProfileSource(source)
}

fun profileShouldFetchConfiguration(
  source: String,
  force: Boolean,
  cachedConfigurationExists: Boolean,
): Boolean {
  return isHttpProfileSource(source) && (force || !cachedConfigurationExists)
}

fun profileShouldForceFetchConfiguration(type: Profile.Type): Boolean {
  return type != Profile.Type.File
}

fun isSupportedProfileSourceScheme(scheme: String?): Boolean {
  return scheme.equals("https", ignoreCase = true) ||
    scheme.equals("http", ignoreCase = true) ||
    scheme.equals("content", ignoreCase = true)
}
