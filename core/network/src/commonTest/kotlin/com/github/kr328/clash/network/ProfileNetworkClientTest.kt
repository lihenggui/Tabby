package com.github.kr328.clash.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout

class ProfileNetworkClientTest {
  @Test
  fun fetchProfileReturnsBodyAndSubscriptionUserInfo() = runTest {
    val engine = MockEngine { request ->
      assertEquals("Tabby/Test", request.headers[HttpHeaders.UserAgent])
      respond(
        content = "mixed-port: 7890",
        status = HttpStatusCode.OK,
        headers =
          headersOf(
            SUBSCRIPTION_USER_INFO_HEADER,
            "upload=1; download=2; total=3; expire=4",
          ),
      )
    }
    val client = ProfileNetworkClient(createTabbyHttpClient(engine))

    val result = client.fetchProfile("https://example.com/config.yaml", "Tabby/Test")

    assertEquals("mixed-port: 7890", result.content)
    assertEquals(
      SubscriptionUserInfo(upload = 1, download = 2, total = 3, expire = 4000),
      result.subscriptionUserInfo,
    )
  }

  @Test
  fun invalidSourceIsRejectedBeforeRequest() = runTest {
    val client =
      ProfileNetworkClient(createTabbyHttpClient(MockEngine { error("No request should be made") }))

    assertFailsWith<IllegalArgumentException> { client.fetchProfile("file:///profile.yaml") }
  }

  @Test
  fun failedHttpStatusThrows() = runTest {
    val client =
      ProfileNetworkClient(
        createTabbyHttpClient(MockEngine { respond("Not found", HttpStatusCode.NotFound) })
      )

    val error =
      assertFailsWith<ProfileFetchException> {
        client.fetchProfile("https://example.com/missing.yaml")
      }

    assertEquals(HttpStatusCode.NotFound, error.status)
  }

  @Test
  fun fetchSubscriptionUserInfoReturnsHeaderForSuccessfulStatus() = runTest {
    val client =
      ProfileNetworkClient(
        createTabbyHttpClient(
          MockEngine {
            respond(
              content = "",
              status = HttpStatusCode.OK,
              headers =
                headersOf(
                  SUBSCRIPTION_USER_INFO_HEADER,
                  "upload=11; download=22; total=33; expire=44",
                ),
            )
          }
        )
      )

    assertEquals(
      SubscriptionUserInfo(upload = 11, download = 22, total = 33, expire = 44_000),
      client.fetchSubscriptionUserInfo("https://example.com/config.yaml"),
    )
  }

  @Test
  fun fetchSubscriptionUserInfoReturnsNullForFailedStatus() = runTest {
    val client =
      ProfileNetworkClient(
        createTabbyHttpClient(MockEngine { respond("Forbidden", HttpStatusCode.Forbidden) })
      )

    assertEquals(null, client.fetchSubscriptionUserInfo("https://example.com/config.yaml"))
  }

  @Test
  fun subscriptionUserInfoParserIgnoresMalformedFields() {
    assertEquals(
      SubscriptionUserInfo(upload = 10, download = 20, total = 30, expire = 4500),
      parseSubscriptionUserInfo("upload=10.5;ignored; download=20; total=30; expire=4.5"),
    )
  }

  @Test
  fun fetchProfileCanBeCancelled() = runTest {
    val client =
      ProfileNetworkClient(
        createTabbyHttpClient(
          MockEngine {
            delay(60_000)
            respond("late", HttpStatusCode.OK)
          }
        )
      )

    assertFailsWith<TimeoutCancellationException> {
      withTimeout(10) { client.fetchProfile("https://example.com/slow.yaml") }
    }
  }
}
