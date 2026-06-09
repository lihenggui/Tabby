package com.github.kr328.clash.common.util

fun tabbyIpv4SocketAddressText(hostAddress: String, port: Int): String {
  return "$hostAddress:$port"
}

fun tabbyIpv6SocketAddressText(address: ByteArray, scopeId: Int, port: Int): String {
  return "[${tabbyIpv6NumericAddressText(address, scopeId)}]:$port"
}

private const val INT16SZ = 2
private const val INADDRSZ = 16

private fun tabbyIpv6NumericAddressText(address: ByteArray, scopeId: Int): String {
  require(address.size == INADDRSZ) { "IPv6 address must be $INADDRSZ bytes" }

  val sb = StringBuilder(39)
  for (i in 0 until INADDRSZ / INT16SZ) {
    sb.append(
      (address[i shl 1].toInt() shl 8 and 0xff00 or (address[(i shl 1) + 1].toInt() and 0xff))
        .toString(16)
    )
    if (i < INADDRSZ / INT16SZ - 1) {
      sb.append(":")
    }
  }

  // Preserve the numeric scope ID so link-local IPv6 addresses remain connectable for Go.
  if (scopeId > 0) {
    sb.append("%")
    sb.append(scopeId)
  }

  return sb.toString()
}
