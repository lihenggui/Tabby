package com.github.kr328.clash.proxy.ui

import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.core.model.TunnelState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ProxyUiStateCommonTest {
  @Test
  fun appliesProxyPreferences() {
    val state =
      ProxyUiState()
        .withProxyPreferences(
          proxyLine = 2,
          excludeNotSelectable = true,
          proxySort = ProxySort.Delay,
        )

    assertEquals(2, state.proxyLine)
    assertEquals(true, state.excludeNotSelectable)
    assertEquals(ProxySort.Delay, state.proxySort)
  }

  @Test
  fun initializesProxyGroupsAndPagesFromLastGroupName() {
    val state =
      ProxyUiState()
        .withInitialProxyGroups(
          overrideMode = TunnelState.Mode.Rule,
          groupNames = listOf("Proxy", "Auto", "Fallback"),
          lastGroupName = "Auto",
        )

    assertEquals(TunnelState.Mode.Rule, state.overrideMode)
    assertEquals(listOf("Proxy", "Auto", "Fallback"), state.groupNames)
    assertEquals(3, state.groups.size)
    assertEquals(1, state.initialPage)
    assertEquals(1, state.currentPage)
  }

  @Test
  fun initializesProxyGroupsWithFirstPageWhenLastGroupIsMissingOrGroupsAreEmpty() {
    val missing =
      ProxyUiState()
        .withInitialProxyGroups(
          overrideMode = null,
          groupNames = listOf("Proxy"),
          lastGroupName = "Missing",
        )
    val empty =
      ProxyUiState()
        .withInitialProxyGroups(
          overrideMode = null,
          groupNames = emptyList(),
          lastGroupName = "Missing",
        )

    assertEquals(0, missing.initialPage)
    assertEquals(0, missing.currentPage)
    assertEquals(0, empty.initialPage)
    assertEquals(0, empty.currentPage)
    assertEquals(emptyList(), empty.groups)
  }

  @Test
  fun preservesProxyGroupsWhenNamesStillMatch() {
    val groups =
      listOf(
        ProxyGroupUiState(refreshVersion = 1),
        ProxyGroupUiState(refreshVersion = 2),
      )
    val state =
      ProxyUiState(groupNames = listOf("Proxy", "Auto"), groups = groups)
        .withInitialProxyGroups(
          overrideMode = TunnelState.Mode.Global,
          groupNames = listOf("Proxy", "Auto"),
          lastGroupName = "Proxy",
        )

    assertEquals(groups, state.groups)
  }

  @Test
  fun resetsProxyGroupsWhenNamesChange() {
    val state =
      ProxyUiState(
          groupNames = listOf("Proxy", "Auto"),
          groups = listOf(ProxyGroupUiState(refreshVersion = 7), ProxyGroupUiState()),
        )
        .withInitialProxyGroups(
          overrideMode = null,
          groupNames = listOf("Proxy"),
          lastGroupName = "Proxy",
        )

    assertEquals(listOf(ProxyGroupUiState()), state.groups)
  }

  @Test
  fun updatesProxyLineAndRefreshVersions() {
    val state =
      ProxyUiState(
          proxyLine = 1,
          groups = listOf(ProxyGroupUiState(refreshVersion = 2), ProxyGroupUiState()),
        )
        .withProxyLine(2)

    assertEquals(2, state.proxyLine)
    assertEquals(listOf(3, 1), state.groups.map(ProxyGroupUiState::refreshVersion))
  }

  @Test
  fun updatesProxyGroupStateByIndex() {
    val original =
      ProxyUiState(groups = listOf(ProxyGroupUiState(), ProxyGroupUiState(refreshVersion = 3)))

    val updated =
      original.withProxyGroupState(index = 1) {
        it.copy(urlTesting = true, refreshVersion = it.refreshVersion + 1)
      }
    val unchanged = original.withProxyGroupState(index = 9) { it.copy(urlTesting = true) }

    assertEquals(false, updated.groups[0].urlTesting)
    assertEquals(true, updated.groups[1].urlTesting)
    assertEquals(4, updated.groups[1].refreshVersion)
    assertSame(original, unchanged)
  }

  @Test
  fun startsUrlTestingWithoutRefreshingGroupVersion() {
    val state = ProxyGroupUiState(urlTesting = false, refreshVersion = 4).withUrlTestStarted()

    assertEquals(true, state.urlTesting)
    assertEquals(4, state.refreshVersion)
  }

  @Test
  fun refreshesProxySelectionGroupState() {
    val state =
      ProxyGroupUiState(
          delayTestingKeys = setOf("Direct"),
          refreshVersion = 4,
        )
        .withProxySelectionRefreshed()

    assertEquals(setOf("Direct"), state.delayTestingKeys)
    assertEquals(5, state.refreshVersion)
  }

  @Test
  fun startsAndFinishesDelayTesting() {
    val started =
      ProxyGroupUiState(delayTestingKeys = setOf("Proxy"), refreshVersion = 4)
        .withDelayTestStarted("Direct")
    val finished = started.withDelayTestFinished("Proxy")
    val finishedMissing = finished.withDelayTestFinished("Missing")

    assertEquals(setOf("Proxy", "Direct"), started.delayTestingKeys)
    assertEquals(5, started.refreshVersion)
    assertEquals(setOf("Direct"), finished.delayTestingKeys)
    assertEquals(6, finished.refreshVersion)
    assertEquals(setOf("Direct"), finishedMissing.delayTestingKeys)
    assertEquals(7, finishedMissing.refreshVersion)
  }

  @Test
  fun createsAndUpdatesSelectedProxies() {
    val selected = initialSelectedProxies(size = 2)
    val updated = selected.withSelectedProxy(index = 1, name = "Direct")
    val unchanged = selected.withSelectedProxy(index = 3, name = "Missing")

    assertEquals(listOf(SelectedProxy("?"), SelectedProxy("?")), selected)
    assertEquals(listOf(SelectedProxy("?"), SelectedProxy("Direct")), updated)
    assertSame(selected, unchanged)
  }

  @Test
  fun updatesSimpleProxyUiStateFields() {
    val state =
      ProxyUiState()
        .withCurrentPage(2)
        .withExcludeNotSelectable(true)
        .withProxySort(ProxySort.Title)
        .withOverrideMode(TunnelState.Mode.Direct)

    assertEquals(2, state.currentPage)
    assertEquals(true, state.excludeNotSelectable)
    assertEquals(ProxySort.Title, state.proxySort)
    assertEquals(TunnelState.Mode.Direct, state.overrideMode)
  }

  @Test
  fun proxyGroupSelectionActionSelectsGroupNameByIndex() {
    assertEquals(
      ProxyGroupSelectionAction.SelectGroup("Auto"),
      proxyGroupSelectionAction(listOf("Proxy", "Auto"), index = 1),
    )
  }

  @Test
  fun proxyGroupSelectionActionIgnoresMissingIndex() {
    assertEquals(
      ProxyGroupSelectionAction.Ignore,
      proxyGroupSelectionAction(listOf("Proxy"), index = 1),
    )
  }

  @Test
  fun proxyGroupSelectionActionIgnoresEmptyGroupNames() {
    assertEquals(
      ProxyGroupSelectionAction.Ignore,
      proxyGroupSelectionAction(emptyList(), index = 0),
    )
  }

  @Test
  fun proxyProfileLoadedActionQueriesGroupNamesWhenInitialized() {
    assertEquals(
      ProxyProfileLoadedAction.QueryGroupNames,
      proxyProfileLoadedAction(initialized = true),
    )
  }

  @Test
  fun proxyProfileLoadedActionIgnoresProfileLoadedBeforeInitialization() {
    assertEquals(
      ProxyProfileLoadedAction.Ignore,
      proxyProfileLoadedAction(initialized = false),
    )
  }

  @Test
  fun proxyGroupNamesChangeActionRelaunchesWhenNamesChange() {
    assertEquals(
      ProxyGroupNamesChangeAction.ReLaunch,
      proxyGroupNamesChangeAction(
        currentGroupNames = listOf("Proxy", "Auto"),
        newGroupNames = listOf("Proxy", "Fallback"),
      ),
    )
  }

  @Test
  fun proxyGroupNamesChangeActionIgnoresUnchangedNames() {
    assertEquals(
      ProxyGroupNamesChangeAction.Ignore,
      proxyGroupNamesChangeAction(
        currentGroupNames = listOf("Proxy", "Auto"),
        newGroupNames = listOf("Proxy", "Auto"),
      ),
    )
  }

  @Test
  fun proxyGroupReloadIndexesReturnsIndexesForGroupNames() {
    assertEquals(
      listOf(0, 1, 2),
      proxyGroupReloadIndexes(listOf("Proxy", "Auto", "Fallback")).toList(),
    )
  }

  @Test
  fun proxyGroupReloadIndexesReturnsEmptyIndexesForEmptyGroupNames() {
    assertEquals(
      emptyList(),
      proxyGroupReloadIndexes(emptyList()).toList(),
    )
  }
}
