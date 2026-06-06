package com.github.kr328.clash.network

const val SUBSCRIPTION_USER_INFO_HEADER = "subscription-userinfo"

data class SubscriptionUserInfo(
  val upload: Long,
  val download: Long,
  val total: Long,
  val expire: Long,
)

fun parseSubscriptionUserInfo(userInfo: String): SubscriptionUserInfo {
  var upload = 0L
  var download = 0L
  var total = 0L
  var expire = 0L

  userInfo.split(';').forEach { field ->
    val parts = field.split('=', limit = 2)
    if (parts.size != 2) return@forEach

    val key = parts[0].trim()
    val value = parts[1].trim()
    if (value.isEmpty()) return@forEach

    when {
      key.contains("upload", ignoreCase = true) -> upload = value.toWholeLong()
      key.contains("download", ignoreCase = true) -> download = value.toWholeLong()
      key.contains("total", ignoreCase = true) -> total = value.toWholeLong()
      key.contains("expire", ignoreCase = true) ->
        expire = ((value.toDoubleOrNull() ?: 0.0) * 1000L).toLong()
    }
  }

  return SubscriptionUserInfo(upload = upload, download = download, total = total, expire = expire)
}

private fun String.toWholeLong(): Long {
  return substringBefore('.').toLongOrNull() ?: 0
}
