package com.github.kr328.clash.settings.ui

import com.github.kr328.clash.core.model.AccessControlSort
import kotlin.test.Test
import kotlin.test.assertEquals

class AccessControlSelectionTest {
  @Test
  fun mapsPlatformPayloadToAccessControlPackage() {
    assertEquals(
      AccessControlPackage(
        packageName = "com.example.alpha",
        label = "Alpha",
        installTime = 10,
        updateDate = 20,
      ),
      accessControlPackageFromPlatformPayload(
        packageName = "com.example.alpha",
        label = "Alpha",
        installTime = 10,
        updateDate = 20,
      ),
    )
  }

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
  fun accessControlExportPayloadUsesStableLabel() {
    assertEquals(
      AccessControlExportPayload(label = "packages", text = "com.example.alpha"),
      accessControlExportPayload("com.example.alpha"),
    )
  }

  @Test
  fun accessControlExportPayloadUsesSelectedPackagesText() {
    val state =
      AccessControlUiState(
        apps = emptyList<TestAccessControlApp>(),
        settings =
          accessControlSettingsState(selected = setOf("com.example.beta", "com.example.alpha")),
      )

    assertEquals(
      AccessControlExportPayload(
        label = "packages",
        text = "com.example.alpha\ncom.example.beta",
      ),
      accessControlExportPayload(state),
    )
  }

  @Test
  fun clipboardImportPayloadKeepsTextOnlyWhenPrimaryClipItemExists() {
    assertEquals(
      AccessControlClipboardImportPayload(
        hasPrimaryClipItem = true,
        clipboardText = "com.example.alpha",
      ),
      accessControlClipboardImportPayloadFromPlatformPayload(
        hasPrimaryClipItem = true,
        clipboardText = "com.example.alpha",
      ),
    )
    assertEquals(
      AccessControlClipboardImportPayload(
        hasPrimaryClipItem = false,
        clipboardText = null,
      ),
      accessControlClipboardImportPayloadFromPlatformPayload(
        hasPrimaryClipItem = false,
        clipboardText = "ignored",
      ),
    )
  }

  @Test
  fun clipboardImportActionImportsOnlyWhenPrimaryClipItemExists() {
    assertEquals(
      AccessControlClipboardImportAction.Import("com.example.alpha"),
      accessControlClipboardImportAction(
        AccessControlClipboardImportPayload(
          hasPrimaryClipItem = true,
          clipboardText = "com.example.alpha",
        )
      ),
    )
    assertEquals(
      AccessControlClipboardImportAction.Ignore,
      accessControlClipboardImportAction(
        AccessControlClipboardImportPayload(
          hasPrimaryClipItem = false,
          clipboardText = null,
        )
      ),
    )
  }

  @Test
  fun mapsSystemAppFromPlatformFlags() {
    val systemAppFlag = 0b0100

    assertEquals(
      true,
      accessControlSystemAppFromPlatformFlags(
        flags = 0b0101,
        systemAppFlag = systemAppFlag,
      ),
    )
    assertEquals(
      false,
      accessControlSystemAppFromPlatformFlags(
        flags = 0b0011,
        systemAppFlag = systemAppFlag,
      ),
    )
    assertEquals(
      false,
      accessControlSystemAppFromPlatformFlags(
        flags = null,
        systemAppFlag = systemAppFlag,
      ),
    )
  }

  @Test
  fun createsAccessControlReloadRequestFromPlatformSnapshots() {
    assertEquals(
      AccessControlReloadRequest(
        selected = setOf("com.example.alpha"),
        sort = AccessControlSort.InstallTime,
        reverse = true,
        showSystemApps = true,
      ),
      accessControlReloadRequest(
        selected = setOf("com.example.alpha"),
        sort = AccessControlSort.InstallTime,
        reverse = true,
        showSystemApps = true,
      ),
    )
  }

  @Test
  fun plansNoAccessControlPersistWorkWhenSelectionIsUnchanged() {
    assertEquals(
      AccessControlPersistPlan(
        shouldPersistSelection = false,
        shouldRestartService = false,
      ),
      planAccessControlPersist(
        selected = setOf("com.example.alpha"),
        persistedSelection = setOf("com.example.alpha"),
        clashRunning = true,
      ),
    )
  }

  @Test
  fun plansSelectionPersistWithoutRestartWhenClashIsStopped() {
    assertEquals(
      AccessControlPersistPlan(
        shouldPersistSelection = true,
        shouldRestartService = false,
      ),
      planAccessControlPersist(
        selected = setOf("com.example.alpha"),
        persistedSelection = emptySet(),
        clashRunning = false,
      ),
    )
  }

  @Test
  fun plansSelectionPersistAndRestartWhenRunningSelectionChanged() {
    assertEquals(
      AccessControlPersistPlan(
        shouldPersistSelection = true,
        shouldRestartService = true,
      ),
      planAccessControlPersist(
        selected = setOf("com.example.alpha"),
        persistedSelection = emptySet(),
        clashRunning = true,
      ),
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
  fun filtersPackageCandidatesForAccessControl() {
    val packages =
      listOf(
        accessControlPackage(packageName = "io.github.goooler.tabby", hasInternetPermission = true),
        accessControlPackage(packageName = "com.example.without-info", hasAppMetadata = false),
        accessControlPackage(packageName = "com.example.without-network"),
        accessControlPackage(
          packageName = "com.example.internet",
          hasInternetPermission = true,
        ),
        accessControlPackage(
          packageName = "com.example.system-uid",
          hasSystemUid = true,
        ),
        accessControlPackage(
          packageName = "com.example.system-app",
          hasInternetPermission = true,
          isSystemApp = true,
        ),
      )

    val visible =
      filterAccessControlPackageCandidates(
        packages = packages,
        currentPackageName = "io.github.goooler.tabby",
        showSystemApps = false,
        packageName = TestAccessControlPackage::packageName,
        hasAppMetadata = TestAccessControlPackage::hasAppMetadata,
        hasInternetPermission = TestAccessControlPackage::hasInternetPermission,
        hasSystemUid = TestAccessControlPackage::hasSystemUid,
        isSystemApp = TestAccessControlPackage::isSystemApp,
      )

    assertEquals(
      listOf("com.example.internet", "com.example.system-uid"),
      visible.map(TestAccessControlPackage::packageName),
    )
  }

  @Test
  fun keepsSystemPackageCandidatesWhenShowSystemAppsEnabled() {
    val packages =
      listOf(
        accessControlPackage(
          packageName = "com.example.system-app",
          hasInternetPermission = true,
          isSystemApp = true,
        )
      )

    val visible =
      filterAccessControlPackageCandidates(
        packages = packages,
        currentPackageName = "io.github.goooler.tabby",
        showSystemApps = true,
        packageName = TestAccessControlPackage::packageName,
        hasAppMetadata = TestAccessControlPackage::hasAppMetadata,
        hasInternetPermission = TestAccessControlPackage::hasInternetPermission,
        hasSystemUid = TestAccessControlPackage::hasSystemUid,
        isSystemApp = TestAccessControlPackage::isSystemApp,
      )

    assertEquals(
      listOf("com.example.system-app"),
      visible.map(TestAccessControlPackage::packageName),
    )
  }

  @Test
  fun loadsAccessControlAppsByFilteringMappingAndSorting() {
    val packages =
      listOf(
        accessControlPackage(packageName = "io.github.goooler.tabby", hasInternetPermission = true),
        accessControlPackage(
          packageName = "com.example.without-info",
          hasAppMetadata = false,
          hasInternetPermission = true,
        ),
        accessControlPackage(packageName = "com.example.without-network"),
        accessControlPackage(
          packageName = "com.example.hidden-system-app",
          hasInternetPermission = true,
          isSystemApp = true,
        ),
        accessControlPackage(packageName = "com.example.beta", hasInternetPermission = true),
        accessControlPackage(packageName = "com.example.alpha", hasInternetPermission = true),
        accessControlPackage(packageName = "com.example.selected", hasInternetPermission = true),
      )
    val mappedPackageNames = mutableListOf<String>()

    val apps =
      loadAccessControlApps(
        packages = packages,
        selectedPackageNames = setOf("com.example.selected"),
        sort = AccessControlSort.Label,
        reverse = false,
        currentPackageName = "io.github.goooler.tabby",
        showSystemApps = false,
        packageName = TestAccessControlPackage::packageName,
        hasAppMetadata = TestAccessControlPackage::hasAppMetadata,
        hasInternetPermission = TestAccessControlPackage::hasInternetPermission,
        hasSystemUid = TestAccessControlPackage::hasSystemUid,
        isSystemApp = TestAccessControlPackage::isSystemApp,
        toApp = {
          mappedPackageNames += it.packageName
          accessControlApp(
            packageName = it.packageName,
            label = it.packageName.substringAfterLast('.'),
          )
        },
        appPackageName = TestAccessControlApp::packageName,
        appLabel = TestAccessControlApp::label,
        appInstallTime = TestAccessControlApp::installTime,
        appUpdateTime = TestAccessControlApp::updateTime,
      )

    assertEquals(
      listOf("com.example.beta", "com.example.alpha", "com.example.selected"),
      mappedPackageNames,
    )
    assertEquals(
      listOf("com.example.selected", "com.example.alpha", "com.example.beta"),
      apps.map(TestAccessControlApp::packageName),
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

  @Test
  fun accessControlUiStateUpdatesAppsAndPreservesSettings() {
    val settings = accessControlSettingsState(selected = setOf("com.example.alpha"))
    val apps = listOf(accessControlApp(packageName = "com.example.alpha"))
    val updated =
      AccessControlUiState<TestAccessControlApp>(apps = emptyList(), settings = settings)
        .withAccessControlApps(apps)

    assertEquals(apps, updated.apps)
    assertEquals(settings, updated.settings)
  }

  @Test
  fun createsInitialAccessControlUiState() {
    val apps = listOf(accessControlApp(packageName = "com.example.alpha"))
    val state =
      accessControlInitialUiState(
        selected = setOf("com.example.alpha"),
        sort = AccessControlSort.UpdateTime,
        reverse = true,
        showSystemApps = true,
        apps = apps,
      )

    assertEquals(apps, state.apps)
    assertEquals(setOf("com.example.alpha"), state.settings.selected)
    assertEquals(AccessControlSort.UpdateTime, state.settings.sort)
    assertEquals(true, state.settings.reverse)
    assertEquals(true, state.settings.showSystemApps)
  }

  @Test
  fun accessControlUiStateReducersUpdateSettingsAndPreserveApps() {
    val apps =
      listOf(
        accessControlApp(packageName = "com.example.alpha"),
        accessControlApp(packageName = "com.example.beta"),
      )
    val state =
      AccessControlUiState(
        apps = apps,
        settings = accessControlSettingsState(selected = setOf("com.example.alpha")),
      )

    val selectedUpdated =
      state.withAccessControlSelectedPackages(setOf("com.example.beta", "com.example.missing"))
    val toggled = state.withToggledAccessControlPackage("com.example.beta")
    val selectedAll =
      state.withAllAccessControlPackages(apps.map(TestAccessControlApp::packageName))
    val visibleSelectedAll =
      state.withAllVisibleAccessControlPackages(TestAccessControlApp::packageName)
    val selectedNone = state.withNoAccessControlPackages()
    val inverted =
      state.withInvertedAccessControlPackages(apps.map(TestAccessControlApp::packageName))
    val visibleInverted =
      state.withInvertedVisibleAccessControlPackages(TestAccessControlApp::packageName)
    val imported =
      state.withImportedAccessControlPackages(
        clipboardText = "com.example.beta\ncom.example.missing",
        installedPackageNames = apps.map(TestAccessControlApp::packageName),
      )
    val visibleImported =
      state.withImportedVisibleAccessControlPackages(
        clipboardText = "com.example.beta\ncom.example.missing",
        packageName = TestAccessControlApp::packageName,
      )
    val clipboardImported =
      accessControlImportClipboardState(
        state = state,
        clipboardText = "com.example.beta\ncom.example.missing",
        packageName = TestAccessControlApp::packageName,
      )
    val clipboardText = accessControlExportClipboardText(state)
    val sortUpdated = state.withAccessControlSort(AccessControlSort.UpdateTime)
    val reverseUpdated = state.withAccessControlReverse(true)
    val showSystemAppsUpdated = state.withAccessControlShowSystemApps(true)

    assertEquals(apps, selectedUpdated.apps)
    assertEquals(
      setOf("com.example.beta", "com.example.missing"),
      selectedUpdated.settings.selected,
    )
    assertEquals(setOf("com.example.alpha", "com.example.beta"), toggled.settings.selected)
    assertEquals(setOf("com.example.alpha", "com.example.beta"), selectedAll.settings.selected)
    assertEquals(
      setOf("com.example.alpha", "com.example.beta"),
      visibleSelectedAll.settings.selected,
    )
    assertEquals(emptySet(), selectedNone.settings.selected)
    assertEquals(setOf("com.example.beta"), inverted.settings.selected)
    assertEquals(setOf("com.example.beta"), visibleInverted.settings.selected)
    assertEquals(setOf("com.example.beta"), imported.settings.selected)
    assertEquals(setOf("com.example.beta"), visibleImported.settings.selected)
    assertEquals(setOf("com.example.beta"), clipboardImported.settings.selected)
    assertEquals("com.example.alpha", clipboardText)
    assertEquals(AccessControlSort.UpdateTime, sortUpdated.settings.sort)
    assertEquals(true, reverseUpdated.settings.reverse)
    assertEquals(true, showSystemAppsUpdated.settings.showSystemApps)

    listOf(
        toggled,
        selectedAll,
        visibleSelectedAll,
        selectedNone,
        inverted,
        visibleInverted,
        imported,
        visibleImported,
        clipboardImported,
        sortUpdated,
        reverseUpdated,
        showSystemAppsUpdated,
      )
      .forEach { assertEquals(apps, it.apps) }
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

  private fun accessControlPackage(
    packageName: String,
    hasAppMetadata: Boolean = true,
    hasInternetPermission: Boolean = false,
    hasSystemUid: Boolean = false,
    isSystemApp: Boolean = false,
  ): TestAccessControlPackage {
    return TestAccessControlPackage(
      packageName = packageName,
      hasAppMetadata = hasAppMetadata,
      hasInternetPermission = hasInternetPermission,
      hasSystemUid = hasSystemUid,
      isSystemApp = isSystemApp,
    )
  }

  private data class TestAccessControlPackage(
    val packageName: String,
    val hasAppMetadata: Boolean,
    val hasInternetPermission: Boolean,
    val hasSystemUid: Boolean,
    val isSystemApp: Boolean,
  )
}
