package com.github.kr328.clash.common.app

data class TabbyInstalledAppInfo(
  val uid: Int,
  val packageName: String,
  val sharedUserId: String? = null,
)

fun tabbyInstalledAppCacheEntries(apps: List<TabbyInstalledAppInfo>): List<Pair<Int, String>> {
  return apps
    .groupBy { it.uniqueUidName }
    .map { (_, group) ->
      val app = group.first()

      if (group.size == 1) {
        app.uid to app.packageName
      } else {
        app.uid to app.uniqueUidName
      }
    }
}

private val TabbyInstalledAppInfo.uniqueUidName: String
  get() = if (sharedUserId?.isNotBlank() == true) sharedUserId else packageName
