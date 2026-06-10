package com.github.kr328.clash.common.app

import kotlin.test.Test
import kotlin.test.assertEquals

class InstalledAppCacheTest {
  @Test
  fun usesPackageNameForAppsWithoutSharedUserId() {
    assertEquals(
      listOf(1001 to "org.example.single"),
      tabbyInstalledAppCacheEntries(
        listOf(TabbyInstalledAppInfo(uid = 1001, packageName = "org.example.single"))
      ),
    )
  }

  @Test
  fun usesPackageNameForSingleAppSharedUserIdGroup() {
    assertEquals(
      listOf(1002 to "org.mozilla.firefox"),
      tabbyInstalledAppCacheEntries(
        listOf(
          TabbyInstalledAppInfo(
            uid = 1002,
            packageName = "org.mozilla.firefox",
            sharedUserId = "org.mozilla.firefox.shared",
          )
        )
      ),
    )
  }

  @Test
  fun usesSharedUserIdForMultipleAppsInSharedUserIdGroup() {
    assertEquals(
      listOf(1003 to "com.example.shared"),
      tabbyInstalledAppCacheEntries(
        listOf(
          TabbyInstalledAppInfo(
            uid = 1003,
            packageName = "com.example.one",
            sharedUserId = "com.example.shared",
          ),
          TabbyInstalledAppInfo(
            uid = 1003,
            packageName = "com.example.two",
            sharedUserId = "com.example.shared",
          ),
        )
      ),
    )
  }

  @Test
  fun treatsBlankSharedUserIdAsMissing() {
    assertEquals(
      listOf(1004 to "com.example.blank"),
      tabbyInstalledAppCacheEntries(
        listOf(
          TabbyInstalledAppInfo(uid = 1004, packageName = "com.example.blank", sharedUserId = " ")
        )
      ),
    )
  }

  @Test
  fun preservesFirstSeenGroupOrder() {
    assertEquals(
      listOf(1005 to "com.example.beta", 1006 to "com.example.alpha.shared"),
      tabbyInstalledAppCacheEntries(
        listOf(
          TabbyInstalledAppInfo(uid = 1005, packageName = "com.example.beta"),
          TabbyInstalledAppInfo(
            uid = 1006,
            packageName = "com.example.alpha.one",
            sharedUserId = "com.example.alpha.shared",
          ),
          TabbyInstalledAppInfo(
            uid = 1006,
            packageName = "com.example.alpha.two",
            sharedUserId = "com.example.alpha.shared",
          ),
        )
      ),
    )
  }
}
