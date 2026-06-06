package com.github.kr328.clash.settings.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessControlSelectionTest {
  @Test
  fun togglesPackageSelection() {
    assertEquals(
      setOf("com.example.alpha", "com.example.beta"),
      toggleAccessControlPackage(
        selected = setOf("com.example.alpha"),
        packageName = "com.example.beta",
      ),
    )
    assertEquals(
      setOf("com.example.alpha"),
      toggleAccessControlPackage(
        selected = setOf("com.example.alpha", "com.example.beta"),
        packageName = "com.example.beta",
      ),
    )
  }

  @Test
  fun selectsAllInstalledPackages() {
    assertEquals(
      setOf("com.example.alpha", "com.example.beta"),
      selectAllAccessControlPackages(
        listOf("com.example.alpha", "com.example.beta", "com.example.alpha")
      ),
    )
  }

  @Test
  fun invertsSelectionAgainstInstalledPackages() {
    assertEquals(
      setOf("com.example.beta"),
      invertAccessControlPackages(
        selected = setOf("com.example.alpha", "com.example.missing"),
        packageNames = listOf("com.example.alpha", "com.example.beta"),
      ),
    )
  }

  @Test
  fun importsOnlyInstalledPackagesFromClipboardText() {
    assertEquals(
      setOf("com.example.alpha", "com.example.beta"),
      importAccessControlPackages(
        clipboardText =
          """
          com.example.alpha
            com.example.beta
          com.example.missing

          com.example.alpha
          """
            .trimIndent(),
        installedPackageNames = listOf("com.example.alpha", "com.example.beta"),
      ),
    )
  }

  @Test
  fun exportsSelectedPackagesInSortedOrder() {
    assertEquals(
      "com.example.alpha\ncom.example.beta",
      exportAccessControlPackages(setOf("com.example.beta", "com.example.alpha")),
    )
  }
}
