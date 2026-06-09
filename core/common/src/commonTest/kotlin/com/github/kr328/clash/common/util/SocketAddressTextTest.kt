package com.github.kr328.clash.common.util

import kotlin.test.Test
import kotlin.test.assertEquals

class SocketAddressTextTest {
  @Test
  fun formatsIpv4SocketAddressText() {
    assertEquals("198.18.0.1:53", tabbyIpv4SocketAddressText("198.18.0.1", 53))
  }

  @Test
  fun formatsIpv6SocketAddressText() {
    val address =
      byteArrayOf(
        0x20,
        0x01,
        0x0d,
        0xb8.toByte(),
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        1,
      )

    assertEquals("[2001:db8:0:0:0:0:0:1]:5353", tabbyIpv6SocketAddressText(address, 0, 5353))
  }

  @Test
  fun formatsIpv6SocketAddressTextWithScopeId() {
    val address =
      byteArrayOf(
        0xfe.toByte(),
        0x80.toByte(),
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        1,
      )

    assertEquals("[fe80:0:0:0:0:0:0:1%4]:53", tabbyIpv6SocketAddressText(address, 4, 53))
  }
}
