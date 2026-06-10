package com.github.kr328.clash.service.util

import android.content.Context
import com.github.kr328.clash.network.ProfileFetchResult
import com.github.kr328.clash.network.ProfileNetworkClient
import com.github.kr328.clash.network.SubscriptionUserInfo
import com.github.kr328.clash.network.tabbyProfileUserAgent

private val profileNetworkClient = ProfileNetworkClient()

internal suspend fun Context.fetchProfile(source: String): ProfileFetchResult {
  return profileNetworkClient.fetchProfile(source, tabbyUserAgent())
}

internal suspend fun Context.fetchSubscriptionUserInfo(source: String): SubscriptionUserInfo? {
  return profileNetworkClient.fetchSubscriptionUserInfo(source, tabbyUserAgent())
}

private fun Context.tabbyUserAgent(): String {
  val versionName = packageManager.getPackageInfo(packageName, 0).versionName
  return tabbyProfileUserAgent(versionName)
}
