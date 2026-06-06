package com.github.kr328.clash.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.android.Android

actual fun createTabbyHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient {
  return HttpClient(Android) {
    installTabbyDefaults()
    config()
  }
}
