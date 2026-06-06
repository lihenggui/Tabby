package com.github.kr328.clash.proxy.ui

import com.github.kr328.clash.core.model.Proxy
import com.github.kr328.clash.core.model.ProxyGroup
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
  fun proxyExcludeNotSelectableChangeActionUpdatesStateAndRelaunches() {
    val action =
      proxyExcludeNotSelectableChangeAction(
        state = ProxyUiState(excludeNotSelectable = false),
        enabled = true,
      )

    assertEquals(true, action.state.excludeNotSelectable)
    assertEquals(ProxyPreferenceChangeEffect.ReLaunch, action.effect)
  }

  @Test
  fun proxyLineChangeActionUpdatesStateAndReloadsAll() {
    val action =
      proxyLineChangeAction(
        state =
          ProxyUiState(
            proxyLine = 1,
            groups = listOf(ProxyGroupUiState(refreshVersion = 2), ProxyGroupUiState()),
          ),
        line = 2,
      )

    assertEquals(2, action.state.proxyLine)
    assertEquals(listOf(3, 1), action.state.groups.map(ProxyGroupUiState::refreshVersion))
    assertEquals(ProxyPreferenceChangeEffect.ReloadAll, action.effect)
  }

  @Test
  fun proxySortChangeActionUpdatesStateAndReloadsAll() {
    val action =
      proxySortChangeAction(
        state = ProxyUiState(proxySort = ProxySort.Default),
        sort = ProxySort.Delay,
      )

    assertEquals(ProxySort.Delay, action.state.proxySort)
    assertEquals(ProxyPreferenceChangeEffect.ReloadAll, action.effect)
  }

  @Test
  fun proxyPageChangedActionUpdatesPageAndSavesGroup() {
    val action =
      proxyPageChangedAction(
        state = ProxyUiState(currentPage = 0, groupNames = listOf("Proxy", "Auto")),
        index = 1,
      )

    assertEquals(1, action.state.currentPage)
    assertEquals(ProxyPageChangedEffect.SaveLastGroup("Auto"), action.effect)
  }

  @Test
  fun proxyPageChangedActionUpdatesPageAndIgnoresMissingGroupIndex() {
    val action =
      proxyPageChangedAction(
        state = ProxyUiState(currentPage = 0, groupNames = listOf("Proxy")),
        index = 2,
      )

    assertEquals(2, action.state.currentPage)
    assertEquals(ProxyPageChangedEffect.Ignore, action.effect)
  }

  @Test
  fun proxyOverrideModeActionUpdatesStateShowsTipsAndPatchesMode() {
    val action =
      proxyOverrideModeAction(
        state = ProxyUiState(overrideMode = null),
        mode = TunnelState.Mode.Rule,
      )

    assertEquals(TunnelState.Mode.Rule, action.state.overrideMode)
    assertEquals(
      ProxyOverrideModeEffect.ShowTipsAndPatchMode(TunnelState.Mode.Rule),
      action.effect,
    )
  }

  @Test
  fun proxyOverrideModeActionClearsModeAndPatchesNullMode() {
    val action =
      proxyOverrideModeAction(
        state = ProxyUiState(overrideMode = TunnelState.Mode.Global),
        mode = null,
      )

    assertEquals(null, action.state.overrideMode)
    assertEquals(
      ProxyOverrideModeEffect.ShowTipsAndPatchMode(null),
      action.effect,
    )
  }

  @Test
  fun proxyUrlTestActionStartsUrlTestForGroupIndex() {
    val action =
      proxyUrlTestAction(
        state =
          ProxyUiState(
            groupNames = listOf("Proxy", "Auto"),
            groups = listOf(ProxyGroupUiState(), ProxyGroupUiState()),
          ),
        index = 1,
      )

    assertEquals(false, action.state.groups[0].urlTesting)
    assertEquals(true, action.state.groups[1].urlTesting)
    assertEquals(
      ProxyUrlTestEffect.StartUrlTest(index = 1, groupName = "Auto"),
      action.effect,
    )
  }

  @Test
  fun proxyUrlTestActionIgnoresMissingGroupIndex() {
    val state =
      ProxyUiState(
        groupNames = listOf("Proxy"),
        groups = listOf(ProxyGroupUiState()),
      )
    val action = proxyUrlTestAction(state, index = 1)

    assertSame(state, action.state)
    assertEquals(ProxyUrlTestEffect.Ignore, action.effect)
  }

  @Test
  fun proxyUrlTestActionStartsEffectWhenGroupStateIsMissing() {
    val state =
      ProxyUiState(
        groupNames = listOf("Proxy"),
        groups = emptyList(),
      )
    val action = proxyUrlTestAction(state, index = 0)

    assertSame(state, action.state)
    assertEquals(
      ProxyUrlTestEffect.StartUrlTest(index = 0, groupName = "Proxy"),
      action.effect,
    )
  }

  @Test
  fun proxyDelayTestActionStartsDelayTestForGroupIndex() {
    val action =
      proxyDelayTestAction(
        state =
          ProxyUiState(
            groupNames = listOf("Proxy", "Auto"),
            groups = listOf(ProxyGroupUiState(), ProxyGroupUiState()),
          ),
        index = 1,
        name = "Direct",
      )

    assertEquals(emptySet(), action.state.groups[0].delayTestingKeys)
    assertEquals(setOf("Direct"), action.state.groups[1].delayTestingKeys)
    assertEquals(1, action.state.groups[1].refreshVersion)
    assertEquals(
      ProxyDelayTestEffect.StartDelayTest(
        index = 1,
        groupName = "Auto",
        proxyName = "Direct",
      ),
      action.effect,
    )
  }

  @Test
  fun proxyDelayTestActionIgnoresMissingGroupIndex() {
    val state =
      ProxyUiState(
        groupNames = listOf("Proxy"),
        groups = listOf(ProxyGroupUiState()),
      )
    val action = proxyDelayTestAction(state, index = 1, name = "Direct")

    assertSame(state, action.state)
    assertEquals(ProxyDelayTestEffect.Ignore, action.effect)
  }

  @Test
  fun proxyDelayTestActionStartsEffectWhenGroupStateIsMissing() {
    val state =
      ProxyUiState(
        groupNames = listOf("Proxy"),
        groups = emptyList(),
      )
    val action = proxyDelayTestAction(state, index = 0, name = "Direct")

    assertSame(state, action.state)
    assertEquals(
      ProxyDelayTestEffect.StartDelayTest(
        index = 0,
        groupName = "Proxy",
        proxyName = "Direct",
      ),
      action.effect,
    )
  }

  @Test
  fun proxySelectedActionPatchesSelectorForGroupIndex() {
    assertEquals(
      ProxySelectedAction.PatchSelector(
        index = 1,
        groupName = "Auto",
        proxyName = "Direct",
      ),
      proxySelectedAction(
        state = ProxyUiState(groupNames = listOf("Proxy", "Auto")),
        index = 1,
        name = "Direct",
      ),
    )
  }

  @Test
  fun proxySelectedActionIgnoresMissingGroupIndex() {
    assertEquals(
      ProxySelectedAction.Ignore,
      proxySelectedAction(
        state = ProxyUiState(groupNames = listOf("Proxy")),
        index = 1,
        name = "Direct",
      ),
    )
  }

  @Test
  fun proxySelectedProxiesUpdatesSelectedProxyByIndex() {
    val selected = listOf(SelectedProxy("?"), SelectedProxy("Proxy"))
    val updated = proxySelectedProxies(selected, index = 1, name = "Direct")
    val unchanged = proxySelectedProxies(selected, index = 3, name = "Direct")

    assertEquals(listOf(SelectedProxy("?"), SelectedProxy("Direct")), updated)
    assertSame(selected, unchanged)
  }

  @Test
  fun proxySelectedUiStateRefreshesGroupByIndex() {
    val state =
      ProxyUiState(
        groups =
          listOf(
            ProxyGroupUiState(refreshVersion = 2),
            ProxyGroupUiState(refreshVersion = 4),
          )
      )
    val updated = proxySelectedUiState(state, index = 1)
    val unchanged = proxySelectedUiState(state, index = 2)

    assertEquals(listOf(2, 5), updated.groups.map(ProxyGroupUiState::refreshVersion))
    assertSame(state, unchanged)
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

  @Test
  fun proxyReloadActionQueriesGroupByIndexWithStateSortAndGroupNames() {
    assertEquals(
      ProxyReloadAction.QueryGroup(
        index = 1,
        groupName = "Auto",
        groupNames = listOf("Proxy", "Auto"),
        sort = ProxySort.Delay,
      ),
      proxyReloadAction(
        state = ProxyUiState(groupNames = listOf("Proxy", "Auto"), proxySort = ProxySort.Delay),
        index = 1,
      ),
    )
  }

  @Test
  fun proxyReloadActionIgnoresMissingGroupIndex() {
    assertEquals(
      ProxyReloadAction.Ignore,
      proxyReloadAction(
        state = ProxyUiState(groupNames = listOf("Proxy")),
        index = 1,
      ),
    )
  }

  @Test
  fun proxyReloadSelectedProxiesUpdatesSelectedProxyFromGroupNow() {
    val selected = listOf(SelectedProxy("?"), SelectedProxy("Proxy"))
    val group = proxyGroup(now = "Direct")
    val updated = proxyReloadSelectedProxies(selected, index = 1, group = group)
    val unchanged = proxyReloadSelectedProxies(selected, index = 3, group = group)

    assertEquals(listOf(SelectedProxy("?"), SelectedProxy("Direct")), updated)
    assertSame(selected, unchanged)
  }

  @Test
  fun proxyReloadUiStateRefreshesGroupStateByIndex() {
    val group =
      proxyGroup(
        type = Proxy.Type.Selector,
        now = "Proxy",
        proxies = listOf(proxy("Proxy"), proxy("Direct")),
      )
    val sources = listOf(ProxyItemSource(proxy("Proxy"), linkIndex = -1))
    val state =
      ProxyUiState(
        groups =
          listOf(
            ProxyGroupUiState(refreshVersion = 2),
            ProxyGroupUiState(
              urlTesting = true,
              delayTestingKeys = setOf("Proxy", "Removed"),
              refreshVersion = 4,
            ),
          )
      )

    val updated = proxyReloadUiState(state, index = 1, group = group, sources = sources)
    val unchanged = proxyReloadUiState(state, index = 3, group = group, sources = sources)

    assertEquals(false, updated.groups[1].urlTesting)
    assertEquals(setOf("Proxy"), updated.groups[1].delayTestingKeys)
    assertEquals(5, updated.groups[1].refreshVersion)
    assertEquals(sources, updated.groups[1].sources)
    assertSame(state, unchanged)
  }
}

private fun proxyGroup(
  type: Proxy.Type = Proxy.Type.Selector,
  now: String = "Proxy",
  proxies: List<Proxy> = listOf(proxy(now)),
): ProxyGroup = ProxyGroup(type = type, now = now, proxies = proxies)

private fun proxy(name: String): Proxy =
  Proxy(
    name = name,
    title = "$name title",
    subtitle = "$name subtitle",
    type = Proxy.Type.Direct,
    delay = 12,
  )
