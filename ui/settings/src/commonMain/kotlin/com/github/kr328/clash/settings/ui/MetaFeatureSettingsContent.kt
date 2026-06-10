package com.github.kr328.clash.settings.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.AnnotatedString
import com.github.kr328.clash.core.model.ConfigurationOverride
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
import tabby.ui.settings.generated.resources.always
import tabby.ui.settings.generated.resources.disabled
import tabby.ui.settings.generated.resources.dont_modify
import tabby.ui.settings.generated.resources.enabled
import tabby.ui.settings.generated.resources.find_process_mode
import tabby.ui.settings.generated.resources.force_dns_mapping
import tabby.ui.settings.generated.resources.force_domain
import tabby.ui.settings.generated.resources.general
import tabby.ui.settings.generated.resources.geodata_mode
import tabby.ui.settings.generated.resources.geox_files
import tabby.ui.settings.generated.resources.import_asn_file
import tabby.ui.settings.generated.resources.import_country_file
import tabby.ui.settings.generated.resources.import_geoip_file
import tabby.ui.settings.generated.resources.import_geosite_file
import tabby.ui.settings.generated.resources.meta_features
import tabby.ui.settings.generated.resources.off
import tabby.ui.settings.generated.resources.override_destination
import tabby.ui.settings.generated.resources.parse_pure_ip
import tabby.ui.settings.generated.resources.press_to_import
import tabby.ui.settings.generated.resources.reset
import tabby.ui.settings.generated.resources.skip_domain
import tabby.ui.settings.generated.resources.skip_dst_address
import tabby.ui.settings.generated.resources.skip_src_address
import tabby.ui.settings.generated.resources.sniff_http_override_destination
import tabby.ui.settings.generated.resources.sniff_http_ports
import tabby.ui.settings.generated.resources.sniff_quic_override_destination
import tabby.ui.settings.generated.resources.sniff_quic_ports
import tabby.ui.settings.generated.resources.sniff_tls_override_destination
import tabby.ui.settings.generated.resources.sniff_tls_ports
import tabby.ui.settings.generated.resources.sniffer_setting
import tabby.ui.settings.generated.resources.strategy
import tabby.ui.settings.generated.resources.strict
import tabby.ui.settings.generated.resources.tcp_concurrent
import tabby.ui.settings.generated.resources.unified_delay

@Composable
internal fun MetaFeatureSettingsContent(
  configuration: ConfigurationOverride,
  actions: MetaFeatureSettingsActions,
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier,
  showResetConfirmDialog: Boolean,
  onShowResetConfirmDialogChange: (Boolean) -> Unit,
  onResetConfirmed: () -> Unit,
  onImportGeoIp: () -> Unit,
  onImportGeoSite: () -> Unit,
  onImportCountry: () -> Unit,
  onImportASN: () -> Unit,
  onOpenEditableTextList: (EditableTextTitle, List<String>?, (List<String>?) -> Unit) -> Unit,
) {
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  TabbyScaffold(
    title = stringResource(Res.string.meta_features),
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    snackbarHostState = snackbarHostState,
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
        metaBasicPreferenceItems(configuration, actions)
        metaSnifferPreferenceItems(configuration, actions, onOpenEditableTextList)
        metaGeoFileItems(
          onImportGeoIp = onImportGeoIp,
          onImportGeoSite = onImportGeoSite,
          onImportCountry = onImportCountry,
          onImportASN = onImportASN,
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

private fun LazyListScope.metaBasicPreferenceItems(
  configuration: ConfigurationOverride,
  actions: MetaFeatureSettingsActions,
) {
  preferenceCategory(key = "cat_general", title = { Text(stringResource(Res.string.general)) })
  listPreference(
    key = "unifiedDelay",
    value = configuration.unifiedDelay,
    onValueChange = actions::updateUnifiedDelay,
    values = booleanOptions,
    title = { Text(stringResource(Res.string.unified_delay)) },
    summary = { Text(stringResource(configuration.unifiedDelay.textResource)) },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  listPreference(
    key = "geodataMode",
    value = configuration.geodataMode,
    onValueChange = actions::updateGeodataMode,
    values = booleanOptions,
    title = { Text(stringResource(Res.string.geodata_mode)) },
    summary = { Text(stringResource(configuration.geodataMode.textResource)) },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  listPreference(
    key = "tcpConcurrent",
    value = configuration.tcpConcurrent,
    onValueChange = actions::updateTcpConcurrent,
    values = booleanOptions,
    title = { Text(stringResource(Res.string.tcp_concurrent)) },
    summary = { Text(stringResource(configuration.tcpConcurrent.textResource)) },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  listPreference(
    key = "findProcessMode",
    value = configuration.findProcessMode,
    onValueChange = actions::updateFindProcessMode,
    values = ConfigurationOverride.FindProcessMode.entries,
    title = { Text(stringResource(Res.string.find_process_mode)) },
    summary = { Text(stringResource(configuration.findProcessMode.textResource)) },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
}

private fun LazyListScope.metaSnifferPreferenceItems(
  configuration: ConfigurationOverride,
  actions: MetaFeatureSettingsActions,
  onOpenEditableTextList: (EditableTextTitle, List<String>?, (List<String>?) -> Unit) -> Unit,
) {
  val enabled = configuration.sniffer.enable != false
  preferenceCategory(
    key = "cat_sniffer",
    title = { Text(stringResource(Res.string.sniffer_setting)) },
  )
  listPreference(
    key = "snifferEnable",
    value = configuration.sniffer.enable,
    onValueChange = actions::updateSnifferEnable,
    values = booleanOptions,
    title = { Text(stringResource(Res.string.strategy)) },
    summary = { Text(stringResource(configuration.sniffer.enable.textResource)) },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  preference(
    key = "sniffHttpPorts",
    title = { Text(stringResource(Res.string.sniff_http_ports)) },
    summary = {
      Text(
        configuration.sniffer.sniff.http.ports.listSummary(stringResource(Res.string.dont_modify))
      )
    },
    enabled = enabled,
    onClick = {
      onOpenEditableTextList(
        EditableTextTitle.SniffHttpPorts,
        configuration.sniffer.sniff.http.ports,
        actions::updateSniffHttpPorts,
      )
    },
  )
  listPreference(
    key = "sniffHttpOverrideDestination",
    value = configuration.sniffer.sniff.http.overrideDestination,
    onValueChange = actions::updateSniffHttpOverrideDestination,
    values = booleanOptions,
    enabled = enabled,
    title = { Text(stringResource(Res.string.sniff_http_override_destination)) },
    summary = {
      Text(stringResource(configuration.sniffer.sniff.http.overrideDestination.textResource))
    },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  preference(
    key = "sniffTlsPorts",
    title = { Text(stringResource(Res.string.sniff_tls_ports)) },
    summary = {
      Text(
        configuration.sniffer.sniff.tls.ports.listSummary(stringResource(Res.string.dont_modify))
      )
    },
    enabled = enabled,
    onClick = {
      onOpenEditableTextList(
        EditableTextTitle.SniffTlsPorts,
        configuration.sniffer.sniff.tls.ports,
        actions::updateSniffTlsPorts,
      )
    },
  )
  listPreference(
    key = "sniffTlsOverrideDestination",
    value = configuration.sniffer.sniff.tls.overrideDestination,
    onValueChange = actions::updateSniffTlsOverrideDestination,
    values = booleanOptions,
    enabled = enabled,
    title = { Text(stringResource(Res.string.sniff_tls_override_destination)) },
    summary = {
      Text(stringResource(configuration.sniffer.sniff.tls.overrideDestination.textResource))
    },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  preference(
    key = "sniffQuicPorts",
    title = { Text(stringResource(Res.string.sniff_quic_ports)) },
    summary = {
      Text(
        configuration.sniffer.sniff.quic.ports.listSummary(stringResource(Res.string.dont_modify))
      )
    },
    enabled = enabled,
    onClick = {
      onOpenEditableTextList(
        EditableTextTitle.SniffQuicPorts,
        configuration.sniffer.sniff.quic.ports,
        actions::updateSniffQuicPorts,
      )
    },
  )
  listPreference(
    key = "sniffQuicOverrideDestination",
    value = configuration.sniffer.sniff.quic.overrideDestination,
    onValueChange = actions::updateSniffQuicOverrideDestination,
    values = booleanOptions,
    enabled = enabled,
    title = { Text(stringResource(Res.string.sniff_quic_override_destination)) },
    summary = {
      Text(stringResource(configuration.sniffer.sniff.quic.overrideDestination.textResource))
    },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  listPreference(
    key = "forceDnsMapping",
    value = configuration.sniffer.forceDnsMapping,
    onValueChange = actions::updateForceDnsMapping,
    values = booleanOptions,
    enabled = enabled,
    title = { Text(stringResource(Res.string.force_dns_mapping)) },
    summary = { Text(stringResource(configuration.sniffer.forceDnsMapping.textResource)) },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  listPreference(
    key = "parsePureIp",
    value = configuration.sniffer.parsePureIp,
    onValueChange = actions::updateParsePureIp,
    values = booleanOptions,
    enabled = enabled,
    title = { Text(stringResource(Res.string.parse_pure_ip)) },
    summary = { Text(stringResource(configuration.sniffer.parsePureIp.textResource)) },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  listPreference(
    key = "overrideDestination",
    value = configuration.sniffer.overrideDestination,
    onValueChange = actions::updateOverrideDestination,
    values = booleanOptions,
    enabled = enabled,
    title = { Text(stringResource(Res.string.override_destination)) },
    summary = { Text(stringResource(configuration.sniffer.overrideDestination.textResource)) },
    valueToText = { AnnotatedString(stringResource(it.textResource)) },
  )
  preference(
    key = "forceDomain",
    title = { Text(stringResource(Res.string.force_domain)) },
    summary = {
      Text(configuration.sniffer.forceDomain.listSummary(stringResource(Res.string.dont_modify)))
    },
    enabled = enabled,
    onClick = {
      onOpenEditableTextList(
        EditableTextTitle.ForceDomain,
        configuration.sniffer.forceDomain,
        actions::updateForceDomain,
      )
    },
  )
  preference(
    key = "skipDomain",
    title = { Text(stringResource(Res.string.skip_domain)) },
    summary = {
      Text(configuration.sniffer.skipDomain.listSummary(stringResource(Res.string.dont_modify)))
    },
    enabled = enabled,
    onClick = {
      onOpenEditableTextList(
        EditableTextTitle.SkipDomain,
        configuration.sniffer.skipDomain,
        actions::updateSkipDomain,
      )
    },
  )
  preference(
    key = "skipSrcAddress",
    title = { Text(stringResource(Res.string.skip_src_address)) },
    summary = {
      Text(configuration.sniffer.skipSrcAddress.listSummary(stringResource(Res.string.dont_modify)))
    },
    enabled = enabled,
    onClick = {
      onOpenEditableTextList(
        EditableTextTitle.SkipSrcAddress,
        configuration.sniffer.skipSrcAddress,
        actions::updateSkipSrcAddress,
      )
    },
  )
  preference(
    key = "skipDstAddress",
    title = { Text(stringResource(Res.string.skip_dst_address)) },
    summary = {
      Text(configuration.sniffer.skipDstAddress.listSummary(stringResource(Res.string.dont_modify)))
    },
    enabled = enabled,
    onClick = {
      onOpenEditableTextList(
        EditableTextTitle.SkipDstAddress,
        configuration.sniffer.skipDstAddress,
        actions::updateSkipDstAddress,
      )
    },
  )
}

private fun LazyListScope.metaGeoFileItems(
  onImportGeoIp: () -> Unit,
  onImportGeoSite: () -> Unit,
  onImportCountry: () -> Unit,
  onImportASN: () -> Unit,
) {
  preferenceCategory(key = "cat_geox", title = { Text(stringResource(Res.string.geox_files)) })
  preference(
    key = "importGeoIp",
    title = { Text(stringResource(Res.string.import_geoip_file)) },
    summary = { Text(stringResource(Res.string.press_to_import)) },
    onClick = onImportGeoIp,
  )
  preference(
    key = "importGeoSite",
    title = { Text(stringResource(Res.string.import_geosite_file)) },
    summary = { Text(stringResource(Res.string.press_to_import)) },
    onClick = onImportGeoSite,
  )
  preference(
    key = "importCountry",
    title = { Text(stringResource(Res.string.import_country_file)) },
    summary = { Text(stringResource(Res.string.press_to_import)) },
    onClick = onImportCountry,
  )
  preference(
    key = "importASN",
    title = { Text(stringResource(Res.string.import_asn_file)) },
    summary = { Text(stringResource(Res.string.press_to_import)) },
    onClick = onImportASN,
  )
}

private val Boolean?.textResource: StringResource
  get() =
    when (this) {
      true -> Res.string.enabled
      false -> Res.string.disabled
      null -> Res.string.dont_modify
    }

private val ConfigurationOverride.FindProcessMode?.textResource: StringResource
  get() =
    when (this) {
      ConfigurationOverride.FindProcessMode.Off -> Res.string.off
      ConfigurationOverride.FindProcessMode.Strict -> Res.string.strict
      ConfigurationOverride.FindProcessMode.Always -> Res.string.always
      null -> Res.string.dont_modify
    }
