package com.github.kr328.clash.proxy.ui

import androidx.compose.ui.graphics.Color
import com.github.kr328.clash.core.model.Proxy
import com.github.kr328.clash.core.model.ProxyGroup
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.core.model.TunnelState

internal data class ProxyUiState(
  val groupNames: List<String> = emptyList(),
  val groups: List<ProxyGroupUiState> = emptyList(),
  val currentPage: Int = 0,
  val proxyLine: Int = 0,
  val excludeNotSelectable: Boolean = false,
  val proxySort: ProxySort = ProxySort.Default,
  val overrideMode: TunnelState.Mode? = null,
  val initialPage: Int = 0,
)

internal fun proxyInitialUiState(): ProxyUiState {
  return ProxyUiState()
}

internal data class ProxyGroupUiState(
  val selectable: Boolean = false,
  val urlTesting: Boolean = false,
  val sources: List<ProxyItemSource> = emptyList(),
  val delayTestingKeys: Set<String> = emptySet(),
  val refreshVersion: Int = 0,
)

internal data class ProxyItemSource(val proxy: Proxy, val linkIndex: Int) {
  fun toUiState(
    parentNow: SelectedProxy?,
    linkNow: SelectedProxy?,
    proxyLine: Int,
    selectedControl: Color,
    selectedBackground: Color,
    unselectedControl: Color,
    unselectedBackground: Color,
    delayTesting: Boolean,
  ): ProxyItemUiState {
    val selected = proxy.name == parentNow?.name
    val background =
      if (selected) {
        selectedBackground
      } else if (proxyLine == 1) {
        Color.Transparent
      } else {
        unselectedBackground
      }
    val controls = if (selected) selectedControl else unselectedControl
    val title = if (proxy.type.group) proxy.name else proxy.title
    val subtitle =
      if (proxy.type.group) {
        if (linkNow == null) {
          proxy.type.name
        } else {
          "${proxy.type.name}(${linkNow.name.ifEmpty { "*" }})"
        }
      } else {
        proxy.subtitle
      }
    val delayText =
      when {
        delayTesting -> "..."
        proxy.delay in 0..Short.MAX_VALUE -> proxy.delay.toString()
        else -> "--"
      }
    return ProxyItemUiState(
      key = proxy.name,
      title = title,
      subtitle = subtitle,
      delayText = delayText,
      delayTesting = delayTesting,
      selected = selected,
      background = background,
      controls = controls,
    )
  }
}

internal data class ProxyItemUiState(
  val key: String,
  val title: String,
  val subtitle: String,
  val delayText: String,
  val delayTesting: Boolean,
  val selected: Boolean,
  val background: Color,
  val controls: Color,
)

internal data class SelectedProxy(val name: String)

internal fun proxyInitialSelectedProxies(): List<SelectedProxy> {
  return emptyList()
}

internal data class ProxyInitialStateAction(
  val state: ProxyUiState,
  val selectedProxies: List<SelectedProxy>,
)

internal data class ProxyReloadResultAction(
  val state: ProxyUiState,
  val selectedProxies: List<SelectedProxy>,
)

internal data class ProxySelectedPatchAction(
  val state: ProxyUiState,
  val selectedProxies: List<SelectedProxy>,
)

internal sealed interface ProxyGroupSelectionAction {
  data class SelectGroup(val name: String) : ProxyGroupSelectionAction

  data object Ignore : ProxyGroupSelectionAction
}

internal enum class ProxyBroadcastEventKind {
  ProfileLoaded,
  Other,
}

internal sealed interface ProxyBroadcastAction {
  data object QueryGroupNames : ProxyBroadcastAction

  data object Ignore : ProxyBroadcastAction
}

internal sealed interface ProxyGroupNamesChangeAction {
  data object ReLaunch : ProxyGroupNamesChangeAction

  data object Ignore : ProxyGroupNamesChangeAction
}

internal sealed interface ProxyReloadAction {
  data class QueryGroup(
    val index: Int,
    val groupName: String,
    val groupNames: List<String>,
    val sort: ProxySort,
  ) : ProxyReloadAction

  data object Ignore : ProxyReloadAction
}

internal data class ProxyPageChangedAction(
  val state: ProxyUiState,
  val effect: ProxyPageChangedEffect,
)

internal sealed interface ProxyPageChangedEffect {
  data class SaveLastGroup(val groupName: String) : ProxyPageChangedEffect

  data object Ignore : ProxyPageChangedEffect
}

internal data class ProxyPreferenceChangeAction(
  val state: ProxyUiState,
  val effect: ProxyPreferenceChangeEffect,
)

internal enum class ProxyPreferenceChangeEffect {
  ReLaunch,
  ReloadAll,
}

internal data class ProxyOverrideModeAction(
  val state: ProxyUiState,
  val effect: ProxyOverrideModeEffect,
)

internal sealed interface ProxyOverrideModeEffect {
  data class ShowTipsAndPatchMode(val mode: TunnelState.Mode?) : ProxyOverrideModeEffect
}

internal data class ProxyUrlTestAction(
  val state: ProxyUiState,
  val effect: ProxyUrlTestEffect,
)

internal sealed interface ProxyUrlTestEffect {
  data class StartUrlTest(val index: Int, val groupName: String) : ProxyUrlTestEffect

  data object Ignore : ProxyUrlTestEffect
}

internal data class ProxyDelayTestAction(
  val state: ProxyUiState,
  val effect: ProxyDelayTestEffect,
)

internal sealed interface ProxyDelayTestEffect {
  data class StartDelayTest(
    val index: Int,
    val groupName: String,
    val proxyName: String,
  ) : ProxyDelayTestEffect

  data object Ignore : ProxyDelayTestEffect
}

internal sealed interface ProxySelectedAction {
  data class PatchSelector(
    val index: Int,
    val groupName: String,
    val proxyName: String,
  ) : ProxySelectedAction

  data object Ignore : ProxySelectedAction
}

internal fun ProxyUiState.withProxyPreferences(
  proxyLine: Int,
  excludeNotSelectable: Boolean,
  proxySort: ProxySort,
): ProxyUiState {
  return copy(
    proxyLine = proxyLine,
    excludeNotSelectable = excludeNotSelectable,
    proxySort = proxySort,
  )
}

internal fun ProxyUiState.withInitialProxyGroups(
  overrideMode: TunnelState.Mode?,
  groupNames: List<String>,
  lastGroupName: String,
): ProxyUiState {
  val preservedGroups = groups.takeIf {
    this.groupNames == groupNames && groups.size == groupNames.size
  }
  val initialPage = groupNames.indexOf(lastGroupName).coerceAtLeast(0)
  val currentPage = initialPage.coerceAtMost((groupNames.size - 1).coerceAtLeast(0))

  return copy(
    overrideMode = overrideMode,
    groupNames = groupNames,
    groups = preservedGroups ?: List(groupNames.size) { ProxyGroupUiState() },
    initialPage = initialPage,
    currentPage = currentPage,
  )
}

internal fun ProxyUiState.withCurrentPage(index: Int): ProxyUiState {
  return copy(currentPage = index)
}

internal fun proxyGroupSelectionAction(
  groupNames: List<String>,
  index: Int,
): ProxyGroupSelectionAction {
  val name = groupNames.getOrNull(index) ?: return ProxyGroupSelectionAction.Ignore

  return ProxyGroupSelectionAction.SelectGroup(name)
}

internal fun proxyInitialStateAction(
  state: ProxyUiState,
  overrideMode: TunnelState.Mode?,
  groupNames: List<String>,
  lastGroupName: String,
): ProxyInitialStateAction {
  return ProxyInitialStateAction(
    state =
      state.withInitialProxyGroups(
        overrideMode = overrideMode,
        groupNames = groupNames,
        lastGroupName = lastGroupName,
      ),
    selectedProxies = initialSelectedProxies(groupNames.size),
  )
}

internal fun proxyBroadcastAction(
  kind: ProxyBroadcastEventKind,
  initialized: Boolean,
): ProxyBroadcastAction {
  return if (kind == ProxyBroadcastEventKind.ProfileLoaded && initialized) {
    ProxyBroadcastAction.QueryGroupNames
  } else {
    ProxyBroadcastAction.Ignore
  }
}

internal fun proxyGroupNamesChangeAction(
  currentGroupNames: List<String>,
  newGroupNames: List<String>,
): ProxyGroupNamesChangeAction {
  return if (newGroupNames != currentGroupNames) {
    ProxyGroupNamesChangeAction.ReLaunch
  } else {
    ProxyGroupNamesChangeAction.Ignore
  }
}

internal fun proxyGroupReloadIndexes(groupNames: List<String>): IntRange {
  return groupNames.indices
}

internal fun proxyReloadAction(
  state: ProxyUiState,
  index: Int,
): ProxyReloadAction {
  return when (val selection = proxyGroupSelectionAction(state.groupNames, index)) {
    is ProxyGroupSelectionAction.SelectGroup ->
      ProxyReloadAction.QueryGroup(
        index = index,
        groupName = selection.name,
        groupNames = state.groupNames,
        sort = state.proxySort,
      )
    ProxyGroupSelectionAction.Ignore -> ProxyReloadAction.Ignore
  }
}

internal fun proxyReloadSelectedProxies(
  selectedProxies: List<SelectedProxy>,
  index: Int,
  group: ProxyGroup,
): List<SelectedProxy> {
  return selectedProxies.withSelectedProxy(index, group.now)
}

internal fun proxyReloadUiState(
  state: ProxyUiState,
  index: Int,
  group: ProxyGroup,
  sources: List<ProxyItemSource>,
): ProxyUiState {
  return state.withProxyGroupState(index) { it.withProxyGroup(group, sources) }
}

internal fun proxyReloadResultAction(
  state: ProxyUiState,
  selectedProxies: List<SelectedProxy>,
  index: Int,
  group: ProxyGroup,
  sources: List<ProxyItemSource>,
): ProxyReloadResultAction {
  return ProxyReloadResultAction(
    state = proxyReloadUiState(state, index, group, sources),
    selectedProxies = proxyReloadSelectedProxies(selectedProxies, index, group),
  )
}

internal fun proxyPageChangedAction(
  state: ProxyUiState,
  index: Int,
): ProxyPageChangedAction {
  val effect =
    when (val selection = proxyGroupSelectionAction(state.groupNames, index)) {
      is ProxyGroupSelectionAction.SelectGroup ->
        ProxyPageChangedEffect.SaveLastGroup(selection.name)
      ProxyGroupSelectionAction.Ignore -> ProxyPageChangedEffect.Ignore
    }

  return ProxyPageChangedAction(
    state = state.withCurrentPage(index),
    effect = effect,
  )
}

internal fun proxyExcludeNotSelectableChangeAction(
  state: ProxyUiState,
  enabled: Boolean,
): ProxyPreferenceChangeAction {
  return ProxyPreferenceChangeAction(
    state = state.withExcludeNotSelectable(enabled),
    effect = ProxyPreferenceChangeEffect.ReLaunch,
  )
}

internal fun proxyLineChangeAction(
  state: ProxyUiState,
  line: Int,
): ProxyPreferenceChangeAction {
  return ProxyPreferenceChangeAction(
    state = state.withProxyLine(line),
    effect = ProxyPreferenceChangeEffect.ReloadAll,
  )
}

internal fun proxySortChangeAction(
  state: ProxyUiState,
  sort: ProxySort,
): ProxyPreferenceChangeAction {
  return ProxyPreferenceChangeAction(
    state = state.withProxySort(sort),
    effect = ProxyPreferenceChangeEffect.ReloadAll,
  )
}

internal fun proxyOverrideModeAction(
  state: ProxyUiState,
  mode: TunnelState.Mode?,
): ProxyOverrideModeAction {
  return ProxyOverrideModeAction(
    state = state.withOverrideMode(mode),
    effect = ProxyOverrideModeEffect.ShowTipsAndPatchMode(mode),
  )
}

internal fun proxyUrlTestAction(
  state: ProxyUiState,
  index: Int,
): ProxyUrlTestAction {
  return when (val selection = proxyGroupSelectionAction(state.groupNames, index)) {
    is ProxyGroupSelectionAction.SelectGroup ->
      ProxyUrlTestAction(
        state = state.withProxyGroupState(index) { it.withUrlTestStarted() },
        effect =
          ProxyUrlTestEffect.StartUrlTest(
            index = index,
            groupName = selection.name,
          ),
      )
    ProxyGroupSelectionAction.Ignore ->
      ProxyUrlTestAction(
        state = state,
        effect = ProxyUrlTestEffect.Ignore,
      )
  }
}

internal fun proxyDelayTestAction(
  state: ProxyUiState,
  index: Int,
  name: String,
): ProxyDelayTestAction {
  return when (val selection = proxyGroupSelectionAction(state.groupNames, index)) {
    is ProxyGroupSelectionAction.SelectGroup ->
      ProxyDelayTestAction(
        state = state.withProxyGroupState(index) { it.withDelayTestStarted(name) },
        effect =
          ProxyDelayTestEffect.StartDelayTest(
            index = index,
            groupName = selection.name,
            proxyName = name,
          ),
      )
    ProxyGroupSelectionAction.Ignore ->
      ProxyDelayTestAction(
        state = state,
        effect = ProxyDelayTestEffect.Ignore,
      )
  }
}

internal fun proxySelectedAction(
  state: ProxyUiState,
  index: Int,
  name: String,
): ProxySelectedAction {
  return when (val selection = proxyGroupSelectionAction(state.groupNames, index)) {
    is ProxyGroupSelectionAction.SelectGroup ->
      ProxySelectedAction.PatchSelector(
        index = index,
        groupName = selection.name,
        proxyName = name,
      )
    ProxyGroupSelectionAction.Ignore -> ProxySelectedAction.Ignore
  }
}

internal fun proxySelectedProxies(
  selectedProxies: List<SelectedProxy>,
  index: Int,
  name: String,
): List<SelectedProxy> {
  return selectedProxies.withSelectedProxy(index, name)
}

internal fun proxySelectedUiState(
  state: ProxyUiState,
  index: Int,
): ProxyUiState {
  return state.withProxyGroupState(index) { it.withProxySelectionRefreshed() }
}

internal fun proxySelectedPatchAction(
  state: ProxyUiState,
  selectedProxies: List<SelectedProxy>,
  index: Int,
  name: String,
): ProxySelectedPatchAction {
  return ProxySelectedPatchAction(
    state = proxySelectedUiState(state, index),
    selectedProxies = proxySelectedProxies(selectedProxies, index, name),
  )
}

internal fun ProxyUiState.withExcludeNotSelectable(enabled: Boolean): ProxyUiState {
  return copy(excludeNotSelectable = enabled)
}

internal fun ProxyUiState.withProxyLine(line: Int): ProxyUiState {
  return copy(
    proxyLine = line,
    groups = groups.map { it.copy(refreshVersion = it.refreshVersion + 1) },
  )
}

internal fun ProxyUiState.withProxySort(sort: ProxySort): ProxyUiState {
  return copy(proxySort = sort)
}

internal fun ProxyUiState.withOverrideMode(mode: TunnelState.Mode?): ProxyUiState {
  return copy(overrideMode = mode)
}

internal fun ProxyUiState.withProxyGroupState(
  index: Int,
  transform: (ProxyGroupUiState) -> ProxyGroupUiState,
): ProxyUiState {
  if (index !in groups.indices) return this

  val newGroups = groups.toMutableList()
  newGroups[index] = transform(newGroups[index])
  return copy(groups = newGroups)
}

internal fun ProxyGroupUiState.withUrlTestStarted(): ProxyGroupUiState {
  return copy(urlTesting = true)
}

internal fun ProxyGroupUiState.withProxySelectionRefreshed(): ProxyGroupUiState {
  return copy(refreshVersion = refreshVersion + 1)
}

internal fun ProxyGroupUiState.withDelayTestStarted(name: String): ProxyGroupUiState {
  return copy(
    delayTestingKeys = delayTestingKeys + name,
    refreshVersion = refreshVersion + 1,
  )
}

internal fun ProxyGroupUiState.withDelayTestFinished(name: String): ProxyGroupUiState {
  return copy(
    delayTestingKeys = delayTestingKeys - name,
    refreshVersion = refreshVersion + 1,
  )
}

internal fun initialSelectedProxies(size: Int): List<SelectedProxy> {
  return List(size) { SelectedProxy("?") }
}

internal fun List<SelectedProxy>.withSelectedProxy(
  index: Int,
  name: String,
): List<SelectedProxy> {
  if (index !in indices) return this

  return toMutableList().apply { set(index, SelectedProxy(name)) }
}

internal fun ProxyGroup.toProxyItemSources(groupNames: List<String>): List<ProxyItemSource> {
  val nameIndexMap = groupNames.withIndex().associate { (index, name) -> name to index }

  return proxies.map { proxy ->
    ProxyItemSource(
      proxy = proxy,
      linkIndex = if (proxy.type.group) nameIndexMap[proxy.name] ?: -1 else -1,
    )
  }
}

internal fun ProxyGroupUiState.withProxyGroup(
  group: ProxyGroup,
  sources: List<ProxyItemSource>,
): ProxyGroupUiState =
  copy(
    selectable = group.type == Proxy.Type.Selector,
    urlTesting = false,
    sources = sources,
    delayTestingKeys =
      delayTestingKeys.intersect(sources.mapTo(mutableSetOf()) { source -> source.proxy.name }),
    refreshVersion = refreshVersion + 1,
  )

internal sealed interface ProxyEventState {
  data object Idle : ProxyEventState

  data object ReLaunch : ProxyEventState

  data object ShowModeSwitchTips : ProxyEventState
}

internal fun proxyInitialEventState(): ProxyEventState {
  return ProxyEventState.Idle
}

internal fun proxyGroupNamesChangeEventState(
  action: ProxyGroupNamesChangeAction
): ProxyEventState? {
  return when (action) {
    ProxyGroupNamesChangeAction.ReLaunch -> ProxyEventState.ReLaunch
    ProxyGroupNamesChangeAction.Ignore -> null
  }
}

internal fun proxyPreferenceChangeEventState(
  effect: ProxyPreferenceChangeEffect
): ProxyEventState? {
  return when (effect) {
    ProxyPreferenceChangeEffect.ReLaunch -> ProxyEventState.ReLaunch
    ProxyPreferenceChangeEffect.ReloadAll -> null
  }
}

internal fun proxyOverrideModeEventState(effect: ProxyOverrideModeEffect): ProxyEventState {
  return when (effect) {
    is ProxyOverrideModeEffect.ShowTipsAndPatchMode -> ProxyEventState.ShowModeSwitchTips
  }
}

internal fun proxyConsumedEventState(): ProxyEventState {
  return ProxyEventState.Idle
}
