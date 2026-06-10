package com.github.kr328.clash.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect fun createTabbyHttpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient

fun createTabbyHttpClient(
  engine: HttpClientEngine,
  config: HttpClientConfig<*>.() -> Unit = {},
): HttpClient {
  return HttpClient(engine) {
    installTabbyDefaults()
    config()
  }
}

internal fun HttpClientConfig<*>.installTabbyDefaults() {
  expectSuccess = false
  followRedirects = true
  install(HttpTimeout) {
    connectTimeoutMillis = 15_000
    requestTimeoutMillis = 30_000
    socketTimeoutMillis = 30_000
  }
  install(ContentNegotiation) {
    json(
      Json {
        ignoreUnknownKeys = true
        explicitNulls = false
      }
    )
  }
}
