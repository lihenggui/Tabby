package com.github.kr328.clash.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin

actual fun createTabbyHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient {
  return HttpClient(Darwin) {
    installTabbyDefaults()
    config()
  }
}
