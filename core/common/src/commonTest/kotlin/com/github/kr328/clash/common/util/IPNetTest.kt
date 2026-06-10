package com.github.kr328.clash.common.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IPNetTest {
  @Test
  fun parsesIpv4Cidr() {
    assertEquals(IPNet(ip = "172.19.0.1", prefix = 30), IPNet.parse("172.19.0.1/30"))
  }

  @Test
  fun parsesIpv6Cidr() {
    assertEquals(IPNet(ip = "::", prefix = 1), IPNet.parse("::/1"))
  }

  @Test
  fun rejectsAddressWithoutPrefixSeparator() {
    assertFailsWith<IllegalArgumentException> { IPNet.parse("172.19.0.1") }
  }

  @Test
  fun rejectsNonNumericPrefix() {
    assertFailsWith<NumberFormatException> { IPNet.parse("172.19.0.1/not-a-prefix") }
  }
}
