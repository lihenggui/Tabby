package com.github.kr328.clash.settings.ui

import org.jetbrains.compose.resources.StringResource
import tabby.ui.settings.generated.resources.Res
import tabby.ui.settings.generated.resources.allow_origins
import tabby.ui.settings.generated.resources.authentication
import tabby.ui.settings.generated.resources.default_name_server
import tabby.ui.settings.generated.resources.domain_fallback
import tabby.ui.settings.generated.resources.fakeip_filter
import tabby.ui.settings.generated.resources.fallback
import tabby.ui.settings.generated.resources.force_domain
import tabby.ui.settings.generated.resources.hosts
import tabby.ui.settings.generated.resources.ipcidr_fallback
import tabby.ui.settings.generated.resources.name_server
import tabby.ui.settings.generated.resources.name_server_policy
import tabby.ui.settings.generated.resources.skip_domain
import tabby.ui.settings.generated.resources.skip_dst_address
import tabby.ui.settings.generated.resources.skip_src_address
import tabby.ui.settings.generated.resources.sniff_http_ports
import tabby.ui.settings.generated.resources.sniff_quic_ports
import tabby.ui.settings.generated.resources.sniff_tls_ports

internal enum class EditableTextTitle {
  AllowOrigins,
  Authentication,
  DefaultNameServer,
  DomainFallback,
  FakeIpFilter,
  Fallback,
  ForceDomain,
  Hosts,
  IpcidrFallback,
  NameServer,
  NameServerPolicy,
  SkipDomain,
  SkipDstAddress,
  SkipSrcAddress,
  SniffHttpPorts,
  SniffQuicPorts,
  SniffTlsPorts,
}

internal val EditableTextTitle.textResource: StringResource
  get() =
    when (this) {
      EditableTextTitle.AllowOrigins -> Res.string.allow_origins
      EditableTextTitle.Authentication -> Res.string.authentication
      EditableTextTitle.DefaultNameServer -> Res.string.default_name_server
      EditableTextTitle.DomainFallback -> Res.string.domain_fallback
      EditableTextTitle.FakeIpFilter -> Res.string.fakeip_filter
      EditableTextTitle.Fallback -> Res.string.fallback
      EditableTextTitle.ForceDomain -> Res.string.force_domain
      EditableTextTitle.Hosts -> Res.string.hosts
      EditableTextTitle.IpcidrFallback -> Res.string.ipcidr_fallback
      EditableTextTitle.NameServer -> Res.string.name_server
      EditableTextTitle.NameServerPolicy -> Res.string.name_server_policy
      EditableTextTitle.SkipDomain -> Res.string.skip_domain
      EditableTextTitle.SkipDstAddress -> Res.string.skip_dst_address
      EditableTextTitle.SkipSrcAddress -> Res.string.skip_src_address
      EditableTextTitle.SniffHttpPorts -> Res.string.sniff_http_ports
      EditableTextTitle.SniffQuicPorts -> Res.string.sniff_quic_ports
      EditableTextTitle.SniffTlsPorts -> Res.string.sniff_tls_ports
    }
