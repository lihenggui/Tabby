package com.github.kr328.clash.common.app

import kotlin.test.Test
import kotlin.test.assertEquals

class ApkNativeLibraryTest {
  @Test
  fun acceptsApkWhenAnySupportedAbiHasClashNativeLibrary() {
    assertEquals(
      true,
      tabbyApkHasSupportedClashNativeLibrary(
        supportedAbis = setOf("arm64-v8a", "x86_64"),
        apkEntryNames =
          sequenceOf(
            "AndroidManifest.xml",
            "lib/armeabi-v7a/libclash.so",
            "lib/arm64-v8a/libclash.so",
          ),
      ),
    )
  }

  @Test
  fun rejectsApkWhenClashNativeLibraryUsesUnsupportedAbi() {
    assertEquals(
      false,
      tabbyApkHasSupportedClashNativeLibrary(
        supportedAbis = setOf("arm64-v8a"),
        apkEntryNames = sequenceOf("lib/x86_64/libclash.so"),
      ),
    )
  }

  @Test
  fun ignoresEntriesThatAreNotClashNativeLibraries() {
    assertEquals(
      false,
      tabbyApkHasSupportedClashNativeLibrary(
        supportedAbis = setOf("arm64-v8a"),
        apkEntryNames =
          sequenceOf(
            "lib/arm64-v8a/libmihomo.so",
            "assets/lib/arm64-v8a/libclash.so",
            "lib/arm64-v8a/nested/libclash.so",
          ),
      ),
    )
  }
}
