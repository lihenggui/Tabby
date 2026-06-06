package com.github.kr328.clash.engine.desktop

import com.github.kr328.clash.core.model.Traffic
import com.github.kr328.clash.network.createTabbyHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class DesktopEngineControllerTest {
  @Test
  fun queryTrafficDelegatesToMihomoTrafficStream() = runTest {
    val api =
      DesktopMihomoApi(
        DesktopMihomoEndpoint("127.0.0.1:9090"),
        createTabbyHttpClient(
          MockEngine {
            respond(
              """{"up":1,"down":2,"upTotal":120,"downTotal":340}""",
              HttpStatusCode.OK,
              headers = JSON_HEADERS,
            )
          }
        ),
      )
    val controller = DesktopEngineController(mihomoApi = api)

    assertEquals(Traffic.fromBytes(120, 340), controller.queryTraffic())
  }

  @Test
  fun queryProxyGroupNamesDelegatesToMihomoApi() = runTest {
    var requestIndex = 0
    val api =
      DesktopMihomoApi(
        DesktopMihomoEndpoint("127.0.0.1:9090"),
        createTabbyHttpClient(
          MockEngine { request ->
            when (requestIndex++) {
              0 -> respond("""{"mode":"rule"}""", HttpStatusCode.OK, headers = JSON_HEADERS)
              1 ->
                respond(
                  """{"proxies":[{"name":"Select","type":"Selector","all":[],"now":""}]}""",
                  HttpStatusCode.OK,
                  headers = JSON_HEADERS,
                )
              else -> error("Unexpected request: ${request.url}")
            }
          }
        ),
      )
    val controller = DesktopEngineController(mihomoApi = api)

    assertEquals(listOf("Select"), controller.queryProxyGroupNames(excludeNotSelectable = true))
    assertEquals(2, requestIndex)
  }

  private companion object {
    val JSON_HEADERS = io.ktor.http.headersOf(HttpHeaders.ContentType, "application/json")
  }
}
