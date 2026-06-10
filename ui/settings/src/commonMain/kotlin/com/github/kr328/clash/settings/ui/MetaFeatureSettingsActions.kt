package com.github.kr328.clash.settings.ui

import com.github.kr328.clash.core.model.ConfigurationOverride

internal interface MetaFeatureSettingsActions {
  fun updateUnifiedDelay(value: Boolean?) = Unit

  fun updateGeodataMode(value: Boolean?) = Unit

  fun updateTcpConcurrent(value: Boolean?) = Unit

  fun updateFindProcessMode(value: ConfigurationOverride.FindProcessMode?) = Unit

  fun updateSnifferEnable(value: Boolean?) = Unit

  fun updateSniffHttpPorts(value: List<String>?) = Unit

  fun updateSniffHttpOverrideDestination(value: Boolean?) = Unit

  fun updateSniffTlsPorts(value: List<String>?) = Unit

  fun updateSniffTlsOverrideDestination(value: Boolean?) = Unit

  fun updateSniffQuicPorts(value: List<String>?) = Unit

  fun updateSniffQuicOverrideDestination(value: Boolean?) = Unit

  fun updateForceDnsMapping(value: Boolean?) = Unit

  fun updateParsePureIp(value: Boolean?) = Unit

  fun updateOverrideDestination(value: Boolean?) = Unit

  fun updateForceDomain(value: List<String>?) = Unit

  fun updateSkipDomain(value: List<String>?) = Unit

  fun updateSkipSrcAddress(value: List<String>?) = Unit

  fun updateSkipDstAddress(value: List<String>?) = Unit
}
