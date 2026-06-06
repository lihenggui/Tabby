package com.github.kr328.clash.settings.ui

import com.github.kr328.clash.core.model.AccessControlSort
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

  @Test
  fun filtersAppsByLabelOrPackageName() {
    val apps =
      listOf(
        accessControlApp(packageName = "com.example.alpha", label = "Alpha Tool"),
        accessControlApp(packageName = "com.example.beta", label = "Beta Tool"),
        accessControlApp(packageName = "io.sample.gamma", label = "Gamma Tool"),
      )

    val byLabel =
      filterAccessControlApps(
        apps = apps,
        keyword = "alpha",
        label = TestAccessControlApp::label,
        packageName = TestAccessControlApp::packageName,
      )
    val byPackageName =
      filterAccessControlApps(
        apps = apps,
        keyword = "SAMPLE",
        label = TestAccessControlApp::label,
        packageName = TestAccessControlApp::packageName,
      )

    assertEquals(listOf("com.example.alpha"), byLabel.map(TestAccessControlApp::packageName))
    assertEquals(listOf("io.sample.gamma"), byPackageName.map(TestAccessControlApp::packageName))
  }

  @Test
  fun returnsNoAppsForBlankSearchKeyword() {
    val apps = listOf(accessControlApp(packageName = "com.example.alpha", label = "Alpha Tool"))

    assertEquals(
      emptyList(),
      filterAccessControlApps(
        apps = apps,
        keyword = "  ",
        label = TestAccessControlApp::label,
        packageName = TestAccessControlApp::packageName,
      ),
    )
  }

  @Test
  fun keepsWhitespaceSensitiveSearchKeyword() {
    val apps = listOf(accessControlApp(packageName = "com.example.alpha", label = "Alpha Tool"))

    assertEquals(
      emptyList(),
      filterAccessControlApps(
        apps = apps,
        keyword = " alpha ",
        label = TestAccessControlApp::label,
        packageName = TestAccessControlApp::packageName,
      ),
    )
  }

  @Test
  fun updatesAccessControlSettingsSelectionState() {
    val state =
      accessControlSettingsState(selected = setOf("com.example.alpha", "com.example.missing"))

    val toggled = toggleAccessControlSelectedPackage(state, "com.example.beta")
    val selectedAll =
      selectAllAccessControlPackages(
        state = state,
        packageNames = listOf("com.example.alpha", "com.example.beta", "com.example.alpha"),
      )
    val selectedNone = selectNoAccessControlPackages(state)
    val inverted =
      invertAccessControlPackages(
        state = state,
        packageNames = listOf("com.example.alpha", "com.example.beta"),
      )
    val imported =
      importAccessControlPackages(
        state = state,
        clipboardText = "com.example.beta\ncom.example.missing",
        installedPackageNames = listOf("com.example.alpha", "com.example.beta"),
      )

    assertEquals(
      setOf("com.example.alpha", "com.example.missing", "com.example.beta"),
      toggled.selected,
    )
    assertEquals(state.sort, toggled.sort)
    assertEquals(state.reverse, toggled.reverse)
    assertEquals(state.showSystemApps, toggled.showSystemApps)

    assertEquals(setOf("com.example.alpha", "com.example.beta"), selectedAll.selected)
    assertEquals(state.sort, selectedAll.sort)
    assertEquals(state.reverse, selectedAll.reverse)
    assertEquals(state.showSystemApps, selectedAll.showSystemApps)

    assertEquals(emptySet(), selectedNone.selected)
    assertEquals(state.sort, selectedNone.sort)
    assertEquals(state.reverse, selectedNone.reverse)
    assertEquals(state.showSystemApps, selectedNone.showSystemApps)

    assertEquals(setOf("com.example.beta"), inverted.selected)
    assertEquals(state.sort, inverted.sort)
    assertEquals(state.reverse, inverted.reverse)
    assertEquals(state.showSystemApps, inverted.showSystemApps)

    assertEquals(setOf("com.example.beta"), imported.selected)
    assertEquals(state.sort, imported.sort)
    assertEquals(state.reverse, imported.reverse)
    assertEquals(state.showSystemApps, imported.showSystemApps)
  }

  @Test
  fun updatesAccessControlSettingsSortAndFilterState() {
    val state = accessControlSettingsState()

    val sortUpdated = updateAccessControlSort(state, AccessControlSort.UpdateTime)
    val reverseUpdated = updateAccessControlReverse(state, true)
    val systemAppsUpdated = updateAccessControlShowSystemApps(state, true)
    val selectedUpdated =
      updateAccessControlSelectedPackages(state, setOf("com.example.alpha", "com.example.beta"))

    assertEquals(AccessControlSort.UpdateTime, sortUpdated.sort)
    assertEquals(state.selected, sortUpdated.selected)
    assertEquals(state.reverse, sortUpdated.reverse)
    assertEquals(state.showSystemApps, sortUpdated.showSystemApps)

    assertEquals(true, reverseUpdated.reverse)
    assertEquals(state.selected, reverseUpdated.selected)
    assertEquals(state.sort, reverseUpdated.sort)
    assertEquals(state.showSystemApps, reverseUpdated.showSystemApps)

    assertEquals(true, systemAppsUpdated.showSystemApps)
    assertEquals(state.selected, systemAppsUpdated.selected)
    assertEquals(state.sort, systemAppsUpdated.sort)
    assertEquals(state.reverse, systemAppsUpdated.reverse)

    assertEquals(setOf("com.example.alpha", "com.example.beta"), selectedUpdated.selected)
    assertEquals(state.sort, selectedUpdated.sort)
    assertEquals(state.reverse, selectedUpdated.reverse)
    assertEquals(state.showSystemApps, selectedUpdated.showSystemApps)
  }

  @Test
  fun sortsAppsWithSelectedPackagesFirstThenLabel() {
    val apps =
      listOf(
        accessControlApp(packageName = "com.example.delta", label = "Delta"),
        accessControlApp(packageName = "com.example.beta", label = "Beta"),
        accessControlApp(packageName = "com.example.alpha", label = "Alpha"),
      )

    val sorted =
      sortAccessControlApps(
        apps = apps,
        selectedPackageNames = setOf("com.example.delta", "com.example.alpha"),
        sort = AccessControlSort.Label,
        reverse = false,
        packageName = TestAccessControlApp::packageName,
        label = TestAccessControlApp::label,
        installTime = TestAccessControlApp::installTime,
        updateTime = TestAccessControlApp::updateTime,
      )

    assertEquals(
      listOf("com.example.alpha", "com.example.delta", "com.example.beta"),
      sorted.map(TestAccessControlApp::packageName),
    )
  }

  @Test
  fun sortsAppsWithReverseAppliedInsideSelectionGroups() {
    val apps =
      listOf(
        accessControlApp(
          packageName = "com.example.old-selected",
          installTime = 1,
          updateTime = 10,
        ),
        accessControlApp(
          packageName = "com.example.new-selected",
          installTime = 2,
          updateTime = 30,
        ),
        accessControlApp(packageName = "com.example.new", installTime = 3, updateTime = 40),
        accessControlApp(packageName = "com.example.old", installTime = 4, updateTime = 20),
      )

    val sorted =
      sortAccessControlApps(
        apps = apps,
        selectedPackageNames = setOf("com.example.old-selected", "com.example.new-selected"),
        sort = AccessControlSort.UpdateTime,
        reverse = true,
        packageName = TestAccessControlApp::packageName,
        label = TestAccessControlApp::label,
        installTime = TestAccessControlApp::installTime,
        updateTime = TestAccessControlApp::updateTime,
      )

    assertEquals(
      listOf(
        "com.example.new-selected",
        "com.example.old-selected",
        "com.example.new",
        "com.example.old",
      ),
      sorted.map(TestAccessControlApp::packageName),
    )
  }

  @Test
  fun sortsAppsByPackageNameOrInstallTime() {
    val apps =
      listOf(
        accessControlApp(packageName = "com.example.delta", installTime = 30),
        accessControlApp(packageName = "com.example.alpha", installTime = 20),
        accessControlApp(packageName = "com.example.beta", installTime = 10),
      )

    val byPackageName =
      sortAccessControlApps(
        apps = apps,
        selectedPackageNames = emptySet(),
        sort = AccessControlSort.PackageName,
        reverse = false,
        packageName = TestAccessControlApp::packageName,
        label = TestAccessControlApp::label,
        installTime = TestAccessControlApp::installTime,
        updateTime = TestAccessControlApp::updateTime,
      )
    val byInstallTime =
      sortAccessControlApps(
        apps = apps,
        selectedPackageNames = emptySet(),
        sort = AccessControlSort.InstallTime,
        reverse = false,
        packageName = TestAccessControlApp::packageName,
        label = TestAccessControlApp::label,
        installTime = TestAccessControlApp::installTime,
        updateTime = TestAccessControlApp::updateTime,
      )

    assertEquals(
      listOf("com.example.alpha", "com.example.beta", "com.example.delta"),
      byPackageName.map(TestAccessControlApp::packageName),
    )
    assertEquals(
      listOf("com.example.beta", "com.example.alpha", "com.example.delta"),
      byInstallTime.map(TestAccessControlApp::packageName),
    )
  }

  private fun accessControlSettingsState(
    selected: Set<String> = setOf("com.example.alpha")
  ): AccessControlSettingsState {
    return AccessControlSettingsState(
      selected = selected,
      sort = AccessControlSort.Label,
      reverse = false,
      showSystemApps = false,
    )
  }

  private fun accessControlApp(
    packageName: String,
    label: String = packageName.substringAfterLast('.'),
    installTime: Long = 0,
    updateTime: Long = 0,
  ): TestAccessControlApp {
    return TestAccessControlApp(
      packageName = packageName,
      label = label,
      installTime = installTime,
      updateTime = updateTime,
    )
  }

  private data class TestAccessControlApp(
    val packageName: String,
    val label: String,
    val installTime: Long,
    val updateTime: Long,
  )
}
