package com.github.kr328.clash.network

fun tabbyProfileUserAgent(versionName: String?): String {
  return "Tabby/${versionName ?: "unknown"}"
}
