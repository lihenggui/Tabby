package com.github.kr328.clash.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

class ProfileNetworkClient(private val httpClient: HttpClient = createTabbyHttpClient()) {
  suspend fun fetchProfile(source: String, userAgent: String? = null): ProfileFetchResult {
    requireHttpSource(source)

    val response = httpClient.get(source) { userAgent?.let { header(HttpHeaders.UserAgent, it) } }

    if (response.status.value !in 200..299) {
      throw ProfileFetchException(source, response.status)
    }

    return ProfileFetchResult(
      content = response.body(),
      subscriptionUserInfo =
        response.headers[SUBSCRIPTION_USER_INFO_HEADER]?.let(::parseSubscriptionUserInfo),
    )
  }

  suspend fun fetchSubscriptionUserInfo(
    source: String,
    userAgent: String? = null,
  ): SubscriptionUserInfo? {
    requireHttpSource(source)

    val response = httpClient.get(source) { userAgent?.let { header(HttpHeaders.UserAgent, it) } }

    if (response.status.value !in 200..299) return null

    return response.headers[SUBSCRIPTION_USER_INFO_HEADER]?.let(::parseSubscriptionUserInfo)
  }
}

data class ProfileFetchResult(
  val content: String,
  val subscriptionUserInfo: SubscriptionUserInfo?,
)

class ProfileFetchException(
  val source: String,
  val status: HttpStatusCode,
) : IllegalStateException("Profile fetch failed for $source: HTTP ${status.value}")

private fun requireHttpSource(source: String) {
  require(
    source.startsWith("https://", ignoreCase = true) ||
      source.startsWith("http://", ignoreCase = true)
  ) {
    "Only HTTP(S) profile sources are supported"
  }
}
