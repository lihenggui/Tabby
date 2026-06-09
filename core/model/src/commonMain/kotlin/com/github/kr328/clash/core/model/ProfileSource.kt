package com.github.kr328.clash.core.model

fun isHttpProfileSource(source: String): Boolean {
  return source.startsWith("https://", ignoreCase = true) ||
    source.startsWith("http://", ignoreCase = true)
}

fun isHttpsProfileSource(source: String): Boolean {
  return source.startsWith("https://", ignoreCase = true)
}
