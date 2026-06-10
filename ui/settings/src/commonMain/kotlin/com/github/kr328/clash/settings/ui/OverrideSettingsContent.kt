package com.github.kr328.clash.settings.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.AnnotatedString
import com.github.kr328.clash.core.model.ConfigurationOverride
import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.ui.component.TabbyScaffold
import com.github.kr328.clash.ui.icon.BaselineReplay
import com.github.kr328.clash.ui.icon.TabbyIcons
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tabby.ui.settings.generated.resources.Res
import tabby.ui.settings.generated.resources.allow_lan
import tabby.ui.settings.generated.resources.allow_origins
import tabby.ui.settings.generated.resources.allow_private_network
import tabby.ui.settings.generated.resources.append_system_dns
import tabby.ui.settings.generated.resources.authentication
import tabby.ui.settings.generated.resources.bind_address
import tabby.ui.settings.generated.resources.blacklist
import tabby.ui.settings.generated.resources.debug
import tabby.ui.settings.generated.resources.default_
import tabby.ui.settings.generated.resources.default_name_server
import tabby.ui.settings.generated.resources.direct_mode
import tabby.ui.settings.generated.resources.disabled
import tabby.ui.settings.generated.resources.dns
import tabby.ui.settings.generated.resources.domain_fallback
import tabby.ui.settings.generated.resources.dont_modify
import tabby.ui.settings.generated.resources.enabled
import tabby.ui.settings.generated.resources.enhanced_mode
import tabby.ui.settings.generated.resources.error
import tabby.ui.settings.generated.resources.external_controller
import tabby.ui.settings.generated.resources.external_controller_tls
import tabby.ui.settings.generated.resources.fakeip
import tabby.ui.settings.generated.resources.fakeip_filter
import tabby.ui.settings.generated.resources.fakeip_filter_mode
import tabby.ui.settings.generated.resources.fallback
import tabby.ui.settings.generated.resources.force_enable
import tabby.ui.settings.generated.resources.general
import tabby.ui.settings.generated.resources.geoip_fallback
import tabby.ui.settings.generated.resources.geoip_fallback_code
import tabby.ui.settings.generated.resources.global_mode
import tabby.ui.settings.generated.resources.hosts
import tabby.ui.settings.generated.resources.http_port
import tabby.ui.settings.generated.resources.info
import tabby.ui.settings.generated.resources.ipcidr_fallback
import tabby.ui.settings.generated.resources.ipv6
import tabby.ui.settings.generated.resources.listen
import tabby.ui.settings.generated.resources.log_level
import tabby.ui.settings.generated.resources.mapping
import tabby.ui.settings.generated.resources.mixed_port
import tabby.ui.settings.generated.resources.mode
import tabby.ui.settings.generated.resources.name_server
import tabby.ui.settings.generated.resources.name_server_policy
import tabby.ui.settings.generated.resources.override
import tabby.ui.settings.generated.resources.prefer_h3
import tabby.ui.settings.generated.resources.raw_cn
import tabby.ui.settings.generated.resources.redirect_port
import tabby.ui.settings.generated.resources.reset
import tabby.ui.settings.generated.resources.rule_mode
import tabby.ui.settings.generated.resources.secret
import tabby.ui.settings.generated.resources.silent
import tabby.ui.settings.generated.resources.socks_port
import tabby.ui.settings.generated.resources.strategy
import tabby.ui.settings.generated.resources.tproxy_port
import tabby.ui.settings.generated.resources.unknown
import tabby.ui.settings.generated.resources.use_built_in
import tabby.ui.settings.generated.resources.use_hosts
import tabby.ui.settings.generated.resources.warning
import tabby.ui.settings.generated.resources.whitelist

@Composable
internal fun OverrideSettingsContent(
  configuration: ConfigurationOverride,
  actions: OverrideSettingsActions,
  modifier: Modifier = Modifier,
  showResetConfirmDialog: Boolean,
  onShowResetConfirmDialogChange: (Boolean) -> Unit,
  onResetConfirmed: () -> Unit,
  onOpenEditableTextMap:
    (EditableTextTitle, Map<String, String>?, (Map<String, String>?) -> Unit) -> Unit,
  onOpenEditableTextList: (EditableTextTitle, List<String>?, (List<String>?) -> Unit) -> Unit,
) {
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  TabbyScaffold(
    title = stringResource(Res.string.override),
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    scrollBehavior = scrollBehavior,
    actions = {
      IconButton(onClick = { onShowResetConfirmDialogChange(true) }) {
        Icon(
          imageVector = TabbyIcons.BaselineReplay,
          contentDescription = stringResource(Res.string.reset),
        )
      }
    },
  ) { innerPadding ->
    ProvidePreferenceLocals {
      LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = innerPadding) {
        generalPreferenceItems(
          configuration,
          actions,
          onOpenEditableTextMap,
          onOpenEditableTextList,
        )
        dnsPreferenceItems(
          configuration = configuration,
          actions = actions,
          onOpenEditableTextMap = onOpenEditableTextMap,
          onOpenEditableTextList = onOpenEditableTextList,
        )
      }
    }

    if (showResetConfirmDialog) {
      ResetOverrideSettingsDialog(
        onConfirm = {
          onShowResetConfirmDialogChange(false)
          onResetConfirmed()
        },
        onDismiss = { onShowResetConfirmDialogChange(false) },
      )
    }
  }
}

private fun LazyListScope.generalPreferenceItems(
  configuration: ConfigurationOverride,
  actions: OverrideSettingsActions,
  onOpenEditableTextMap:
    (EditableTextTitle, Map<String, String>?, (Map<String, String>?) -> Unit) -> Unit,
  onOpenEditableTextList: (EditableTextTitle, List<String>?, (List<String>?) -> Unit) -> Unit,
) {
  preferenceCategory(key = "cat_general", title = { Text(stringResource(Res.string.general)) })
  overrideEditTextPreferenceItem(
    key = "httpPort",
    title = { stringResource(Res.string.http_port) },
    placeholder = { stringResource(Res.string.dont_modify) },
    emptyLabel = { stringResource(Res.string.disabled) },
    value = portText(configuration.httpPort),
    onValueChange = { actions.updateHttpPort(parsePort(it)) },
    numericOnly = true,
  )
  overrideEditTextPreferenceItem(
    key = "socksPort",
    title = { stringResource(Res.string.socks_port) },
    placeholder = { stringResource(Res.string.dont_modify) },
    emptyLabel = { stringResource(Res.string.disabled) },
    value = portText(configuration.socksPort),
    onValueChange = { actions.updateSocksPort(parsePort(it)) },
    numericOnly = true,
  )
  overrideEditTextPreferenceItem(
    key = "redirectPort",
    title = { stringResource(Res.string.redirect_port) },
    placeholder = { stringResource(Res.string.dont_modify) },
    emptyLabel = { stringResource(Res.string.disabled) },
    value = portText(configuration.redirectPort),
    onValueChange = { actions.updateRedirectPort(parsePort(it)) },
    numericOnly = true,
  )
  overrideEditTextPreferenceItem(
    key = "tproxyPort",
    title = { stringResource(Res.string.tproxy_port) },
    placeholder = { stringResource(Res.string.dont_modify) },
    emptyLabel = { stringResource(Res.string.disabled) },
    value = portText(configuration.tproxyPort),
    onValueChange = { actions.updateTproxyPort(parsePort(it)) },
    numericOnly = true,
  )
  overrideEditTextPreferenceItem(
    key = "mixedPort",
    title = { stringResource(Res.string.mixed_port) },
    placeholder = { stringResource(Res.string.dont_modify) },
    emptyLabel = { stringResource(Res.string.disabled) },
    value = portText(configuration.mixedPort),
    onValueChange = { actions.updateMixedPort(parsePort(it)) },
    numericOnly = true,
  )
  preference(
    key = "authentication",
    title = { Text(stringResource(Res.string.authentication)) },
    summary = {
      Text(configuration.authentication.listSummary(stringResource(Res.string.dont_modify)))
    },
    onClick = {
      onOpenEditableTextList(
        EditableTextTitle.Authentication,
        configuration.authentication,
        actions::updateAuthentication,
      )
    },
  )
  listPreference(
    key = "allowLan",
    value = configuration.allowLan,
    onValueChange = actions::updateAllowLan,
    values = booleanOptions,
    title = { Text(stringResource(Res.string.allow_lan)) },
    summary = { Text(stringResource(configuration.allowLan.textResource)) },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  listPreference(
    key = "ipv6",
    value = configuration.ipv6,
    onValueChange = actions::updateIpv6,
    values = booleanOptions,
    title = { Text(stringResource(Res.string.ipv6)) },
    summary = { Text(stringResource(configuration.ipv6.textResource)) },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  overrideEditTextPreferenceItem(
    key = "bindAddress",
    title = { stringResource(Res.string.bind_address) },
    placeholder = { stringResource(Res.string.dont_modify) },
    emptyLabel = { stringResource(Res.string.default_) },
    value = configuration.bindAddress,
    onValueChange = actions::updateBindAddress,
  )
  overrideEditTextPreferenceItem(
    key = "externalController",
    title = { stringResource(Res.string.external_controller) },
    placeholder = { stringResource(Res.string.dont_modify) },
    emptyLabel = { stringResource(Res.string.default_) },
    value = configuration.externalController,
    onValueChange = actions::updateExternalController,
  )
  overrideEditTextPreferenceItem(
    key = "externalControllerTls",
    title = { stringResource(Res.string.external_controller_tls) },
    placeholder = { stringResource(Res.string.dont_modify) },
    emptyLabel = { stringResource(Res.string.default_) },
    value = configuration.externalControllerTLS,
    onValueChange = actions::updateExternalControllerTls,
  )
  preference(
    key = "allowOrigins",
    title = { Text(stringResource(Res.string.allow_origins)) },
    summary = {
      Text(
        configuration.externalControllerCors.allowOrigins.listSummary(
          stringResource(Res.string.dont_modify)
        )
      )
    },
    onClick = {
      onOpenEditableTextList(
        EditableTextTitle.AllowOrigins,
        configuration.externalControllerCors.allowOrigins,
        actions::updateAllowOrigins,
      )
    },
  )
  listPreference(
    key = "allowPrivateNetwork",
    value = configuration.externalControllerCors.allowPrivateNetwork,
    onValueChange = actions::updateAllowPrivateNetwork,
    values = booleanOptions,
    title = { Text(stringResource(Res.string.allow_private_network)) },
    summary = {
      Text(stringResource(configuration.externalControllerCors.allowPrivateNetwork.textResource))
    },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  overrideEditTextPreferenceItem(
    key = "secret",
    title = { stringResource(Res.string.secret) },
    placeholder = { stringResource(Res.string.dont_modify) },
    emptyLabel = { stringResource(Res.string.default_) },
    value = configuration.secret,
    onValueChange = actions::updateSecret,
  )
  listPreference(
    key = "mode",
    value = configuration.mode,
    onValueChange = actions::updateMode,
    values = TunnelState.Mode.entries,
    title = { Text(stringResource(Res.string.mode)) },
    summary = { Text(stringResource(configuration.mode.textResource)) },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  listPreference(
    key = "logLevel",
    value = configuration.logLevel,
    onValueChange = actions::updateLogLevel,
    values = LogMessage.Level.entries,
    title = { Text(stringResource(Res.string.log_level)) },
    summary = { Text(stringResource(configuration.logLevel.textResource)) },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  preference(
    key = "hosts",
    title = { Text(stringResource(Res.string.hosts)) },
    summary = { Text(configuration.hosts.mapSummary(stringResource(Res.string.dont_modify))) },
    onClick = {
      onOpenEditableTextMap(EditableTextTitle.Hosts, configuration.hosts, actions::updateHosts)
    },
  )
}

private fun LazyListScope.dnsPreferenceItems(
  configuration: ConfigurationOverride,
  actions: OverrideSettingsActions,
  onOpenEditableTextMap:
    (EditableTextTitle, Map<String, String>?, (Map<String, String>?) -> Unit) -> Unit,
  onOpenEditableTextList: (EditableTextTitle, List<String>?, (List<String>?) -> Unit) -> Unit,
) {
  val enabled = configuration.dns.enable != false
  preferenceCategory(key = "cat_dns", title = { Text(stringResource(Res.string.dns)) })
  listPreference(
    key = "dnsStrategy",
    value = enabled,
    onValueChange = actions::updateDnsEnable,
    values = booleanOptions,
    title = { Text(stringResource(Res.string.strategy)) },
    summary = { Text(stringResource(configuration.dns.enable.dnsStrategyTextResource)) },
    valueToText = {
      AnnotatedString(stringResource(configuration.dns.enable.dnsStrategyTextResource))
    },
  )
  listPreference(
    key = "dnsPreferH3",
    value = configuration.dns.preferH3,
    onValueChange = actions::updateDnsPreferH3,
    values = booleanOptions,
    enabled = enabled,
    title = { Text(stringResource(Res.string.prefer_h3)) },
    summary = { Text(stringResource(configuration.dns.preferH3.textResource)) },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  overrideEditTextPreferenceItem(
    key = "dnsListen",
    title = { stringResource(Res.string.listen) },
    placeholder = { stringResource(Res.string.dont_modify) },
    emptyLabel = { stringResource(Res.string.disabled) },
    value = configuration.dns.listen,
    onValueChange = actions::updateDnsListen,
    enabled = enabled,
  )
  listPreference(
    key = "appendSystemDns",
    value = configuration.app.appendSystemDns,
    onValueChange = actions::updateAppendSystemDns,
    values = booleanOptions,
    enabled = enabled,
    title = { Text(stringResource(Res.string.append_system_dns)) },
    summary = { Text(stringResource(configuration.app.appendSystemDns.textResource)) },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  listPreference(
    key = "dnsIpv6",
    value = configuration.dns.ipv6,
    onValueChange = actions::updateDnsIpv6,
    values = booleanOptions,
    enabled = enabled,
    title = { Text(stringResource(Res.string.ipv6)) },
    summary = { Text(stringResource(configuration.dns.ipv6.textResource)) },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  listPreference(
    key = "dnsUseHosts",
    value = configuration.dns.useHosts,
    onValueChange = actions::updateDnsUseHosts,
    values = booleanOptions,
    enabled = enabled,
    title = { Text(stringResource(Res.string.use_hosts)) },
    summary = { Text(stringResource(configuration.dns.useHosts.textResource)) },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  listPreference(
    key = "dnsEnhancedMode",
    value = configuration.dns.enhancedMode,
    onValueChange = actions::updateDnsEnhancedMode,
    values = ConfigurationOverride.DnsEnhancedMode.entries,
    enabled = enabled,
    title = { Text(stringResource(Res.string.enhanced_mode)) },
    summary = { Text(stringResource(configuration.dns.enhancedMode.textResource)) },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  preference(
    key = "dnsNameServer",
    title = { Text(stringResource(Res.string.name_server)) },
    summary = {
      Text(configuration.dns.nameServer.listSummary(stringResource(Res.string.dont_modify)))
    },
    enabled = enabled,
    onClick = {
      onOpenEditableTextList(
        EditableTextTitle.NameServer,
        configuration.dns.nameServer,
        actions::updateDnsNameServer,
      )
    },
  )
  preference(
    key = "dnsFallback",
    title = { Text(stringResource(Res.string.fallback)) },
    summary = {
      Text(configuration.dns.fallback.listSummary(stringResource(Res.string.dont_modify)))
    },
    enabled = enabled,
    onClick = {
      onOpenEditableTextList(
        EditableTextTitle.Fallback,
        configuration.dns.fallback,
        actions::updateDnsFallback,
      )
    },
  )
  preference(
    key = "dnsDefaultServer",
    title = { Text(stringResource(Res.string.default_name_server)) },
    summary = {
      Text(configuration.dns.defaultServer.listSummary(stringResource(Res.string.dont_modify)))
    },
    enabled = enabled,
    onClick = {
      onOpenEditableTextList(
        EditableTextTitle.DefaultNameServer,
        configuration.dns.defaultServer,
        actions::updateDnsDefaultServer,
      )
    },
  )
  preference(
    key = "dnsFakeIpFilter",
    title = { Text(stringResource(Res.string.fakeip_filter)) },
    summary = {
      Text(configuration.dns.fakeIpFilter.listSummary(stringResource(Res.string.dont_modify)))
    },
    enabled = enabled,
    onClick = {
      onOpenEditableTextList(
        EditableTextTitle.FakeIpFilter,
        configuration.dns.fakeIpFilter,
        actions::updateDnsFakeIpFilter,
      )
    },
  )
  listPreference(
    key = "dnsFakeIpFilterMode",
    value = configuration.dns.fakeIPFilterMode,
    onValueChange = actions::updateDnsFakeIpFilterMode,
    values = ConfigurationOverride.FilterMode.entries,
    enabled = enabled,
    title = { Text(stringResource(Res.string.fakeip_filter_mode)) },
    summary = { Text(stringResource(configuration.dns.fakeIPFilterMode.textResource)) },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  listPreference(
    key = "dnsGeoIpFallback",
    value = configuration.dns.fallbackFilter.geoIp,
    onValueChange = actions::updateDnsGeoIpFallback,
    values = booleanOptions,
    enabled = enabled,
    title = { Text(stringResource(Res.string.geoip_fallback)) },
    summary = { Text(stringResource(configuration.dns.fallbackFilter.geoIp.textResource)) },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  overrideEditTextPreferenceItem(
    key = "dnsGeoIpCode",
    title = { stringResource(Res.string.geoip_fallback_code) },
    placeholder = { stringResource(Res.string.dont_modify) },
    emptyLabel = { stringResource(Res.string.raw_cn) },
    value = configuration.dns.fallbackFilter.geoIpCode,
    onValueChange = actions::updateDnsGeoIpCode,
    enabled = enabled,
  )
  preference(
    key = "dnsDomainFallback",
    title = { Text(stringResource(Res.string.domain_fallback)) },
    summary = {
      Text(
        configuration.dns.fallbackFilter.domain.listSummary(stringResource(Res.string.dont_modify))
      )
    },
    enabled = enabled,
    onClick = {
      onOpenEditableTextList(
        EditableTextTitle.DomainFallback,
        configuration.dns.fallbackFilter.domain,
        actions::updateDnsDomainFallback,
      )
    },
  )
  preference(
    key = "dnsIpcidrFallback",
    title = { Text(stringResource(Res.string.ipcidr_fallback)) },
    summary = {
      Text(
        configuration.dns.fallbackFilter.ipcidr.listSummary(stringResource(Res.string.dont_modify))
      )
    },
    enabled = enabled,
    onClick = {
      onOpenEditableTextList(
        EditableTextTitle.IpcidrFallback,
        configuration.dns.fallbackFilter.ipcidr,
        actions::updateDnsIpcidrFallback,
      )
    },
  )
  preference(
    key = "dnsNameserverPolicy",
    title = { Text(stringResource(Res.string.name_server_policy)) },
    summary = {
      Text(configuration.dns.nameserverPolicy.mapSummary(stringResource(Res.string.dont_modify)))
    },
    enabled = enabled,
    onClick = {
      onOpenEditableTextMap(
        EditableTextTitle.NameServerPolicy,
        configuration.dns.nameserverPolicy,
        actions::updateDnsNameserverPolicy,
      )
    },
  )
}

private val Boolean?.textResource: StringResource
  get() =
    when (this) {
      true -> Res.string.enabled
      false -> Res.string.disabled
      null -> Res.string.dont_modify
    }

private val Boolean?.dnsStrategyTextResource: StringResource
  get() =
    when (this) {
      true -> Res.string.force_enable
      false -> Res.string.use_built_in
      null -> Res.string.dont_modify
    }

private val TunnelState.Mode?.textResource: StringResource
  get() =
    when (this) {
      Direct -> Res.string.direct_mode
      Global -> Res.string.global_mode
      Rule -> Res.string.rule_mode
      null -> Res.string.dont_modify
    }

private val LogMessage.Level?.textResource: StringResource
  get() =
    when (this) {
      Info -> Res.string.info
      Warning -> Res.string.warning
      LogMessage.Level.Error -> Res.string.error
      Debug -> Res.string.debug
      Silent -> Res.string.silent
      Unknown -> Res.string.unknown
      null -> Res.string.dont_modify
    }

private val ConfigurationOverride.DnsEnhancedMode?.textResource: StringResource
  get() =
    when (this) {
      None -> Res.string.disabled
      FakeIp -> Res.string.fakeip
      Mapping -> Res.string.mapping
      null -> Res.string.dont_modify
    }

private val ConfigurationOverride.FilterMode?.textResource: StringResource
  get() =
    when (this) {
      BlackList -> Res.string.blacklist
      WhiteList -> Res.string.whitelist
      null -> Res.string.dont_modify
    }
