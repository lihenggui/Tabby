package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Provider

internal fun Provider.toProviderListItem(
  currentTime: Long,
  updatedAt: Long,
  updating: Boolean,
  formatType: (Provider) -> String,
  formatElapsedMillis: (Long) -> String,
): ProviderListItem {
  return ProviderListItem(
    provider = this,
    typeText = formatType(this),
    updatedAtText = formatElapsedMillis(currentTime - updatedAt),
    updating = updating,
  )
}
