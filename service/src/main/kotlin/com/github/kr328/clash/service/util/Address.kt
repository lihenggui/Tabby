package com.github.kr328.clash.service.util

import com.github.kr328.clash.common.util.tabbyIpv4SocketAddressText
import com.github.kr328.clash.common.util.tabbyIpv6SocketAddressText
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

fun InetAddress.asSocketAddressText(port: Int): String {
  return when (this) {
    is Inet6Address -> tabbyIpv6SocketAddressText(address, scopeId, port)
    is Inet4Address ->
      tabbyIpv4SocketAddressText(hostAddress ?: error("Inet4Address.hostAddress is null"), port)
    else -> throw IllegalArgumentException("Unsupported Inet type ${this.javaClass}")
  }
}
