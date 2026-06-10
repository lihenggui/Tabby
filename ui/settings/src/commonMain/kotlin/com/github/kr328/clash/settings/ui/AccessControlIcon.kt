package com.github.kr328.clash.settings.ui

internal fun accessControlShouldUseAdaptiveIconForeground(
  isAdaptiveIcon: Boolean,
  hasBackground: Boolean,
): Boolean {
  return isAdaptiveIcon && !hasBackground
}
