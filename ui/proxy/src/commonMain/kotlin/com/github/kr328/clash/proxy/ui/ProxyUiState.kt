package com.github.kr328.clash.proxy.ui

import androidx.compose.ui.graphics.Color
import com.github.kr328.clash.core.model.Proxy
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

internal sealed interface ProxyEventState {
  data object Idle : ProxyEventState

  data object ReLaunch : ProxyEventState

  data object ShowModeSwitchTips : ProxyEventState
}
