package com.github.kr328.clash.engine.desktop

import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.core.model.Proxy
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.core.model.Traffic
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.network.createTabbyHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest

class DesktopMihomoApiTest {
  @Test
  fun endpointAddsDefaultHttpScheme() {
    assertEquals(
      "http://127.0.0.1:9090/configs",
      DesktopMihomoEndpoint("127.0.0.1:9090").url("configs"),
    )
  }

  @Test
  fun endpointEncodesPathSegments() {
    assertEquals(
      "http://127.0.0.1:9090/proxies/Select%2FA",
      DesktopMihomoEndpoint("127.0.0.1:9090").url("proxies", "Select/A"),
    )
  }

  @Test
  fun queryModeReadsConfigsMode() = runTest {
    val api =
      DesktopMihomoApi(
        DesktopMihomoEndpoint("127.0.0.1:9090", secret = "token"),
        createTabbyHttpClient(
          MockEngine { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("http://127.0.0.1:9090/configs", request.url.toString())
            assertEquals("Bearer token", request.headers[HttpHeaders.Authorization])
            respond("""{"mode":"rule"}""", HttpStatusCode.OK, headers = JSON_HEADERS)
          }
        ),
      )

    assertEquals(TunnelState.Mode.Rule, api.queryMode())
  }

  @Test
  fun patchModeWritesConfigsMode() = runTest {
    var requestBody: String? = null
    val api =
      DesktopMihomoApi(
        DesktopMihomoEndpoint("http://127.0.0.1:9090", secret = "token"),
        createTabbyHttpClient(
          MockEngine { request ->
            assertEquals(HttpMethod.Patch, request.method)
            assertEquals("http://127.0.0.1:9090/configs", request.url.toString())
            assertEquals("Bearer token", request.headers[HttpHeaders.Authorization])
            assertEquals(ContentType.Application.Json, request.body.contentType)
            requestBody = request.body.toString()
            respond("", HttpStatusCode.NoContent)
          }
        ),
      )

    api.patchMode(TunnelState.Mode.Global)

    assertNotNull(requestBody)
    assertEquals(true, requestBody.contains("global"))
  }

  @Test
  fun failedStatusThrowsApiException() = runTest {
    val api =
      DesktopMihomoApi(
        DesktopMihomoEndpoint("127.0.0.1:9090"),
        createTabbyHttpClient(MockEngine { respond("failed", HttpStatusCode.InternalServerError) }),
      )

    val error = assertFailsWith<DesktopMihomoApiException> { api.queryVersion() }

    assertEquals(HttpStatusCode.InternalServerError, error.status)
  }

  @Test
  fun observeLogsStreamsLogFrames() = runTest {
    val api =
      DesktopMihomoApi(
        DesktopMihomoEndpoint("127.0.0.1:9090", secret = "token"),
        createTabbyHttpClient(
          MockEngine { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("http://127.0.0.1:9090/logs?level=warning", request.url.toString())
            assertEquals("Bearer token", request.headers[HttpHeaders.Authorization])
            respond(
              """
              {"type":"warning","payload":"first"}
              {"type":"error","payload":"second"}
              """
                .trimIndent(),
              HttpStatusCode.OK,
              headers = JSON_HEADERS,
            )
          }
        ),
        clock = { 1234 },
      )

    assertEquals(
      listOf(
        LogMessage(LogMessage.Level.Warning, "first", 1234),
        LogMessage(LogMessage.Level.Error, "second", 1234),
      ),
      api.observeLogs(LogMessage.Level.Warning).take(2).toList(),
    )
  }

  @Test
  fun queryTrafficReadsFirstTrafficStreamFrame() = runTest {
    val api =
      DesktopMihomoApi(
        DesktopMihomoEndpoint("127.0.0.1:9090", secret = "token"),
        createTabbyHttpClient(
          MockEngine { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("http://127.0.0.1:9090/traffic", request.url.toString())
            assertEquals("Bearer token", request.headers[HttpHeaders.Authorization])
            respond(
              """
              {"up":1,"down":2,"upTotal":120,"downTotal":340}
              {"up":3,"down":4,"upTotal":560,"downTotal":780}
              """
                .trimIndent(),
              HttpStatusCode.OK,
              headers = JSON_HEADERS,
            )
          }
        ),
      )

    assertEquals(Traffic.fromBytes(120, 340), api.queryTraffic())
  }

  @Test
  fun observeTrafficStreamsMultipleFrames() = runTest {
    val api =
      DesktopMihomoApi(
        DesktopMihomoEndpoint("127.0.0.1:9090"),
        createTabbyHttpClient(
          MockEngine {
            respond(
              """
              {"up":1,"down":2,"upTotal":120,"downTotal":340}
              {"up":3,"down":4,"upTotal":560,"downTotal":780}
              """
                .trimIndent(),
              HttpStatusCode.OK,
              headers = JSON_HEADERS,
            )
          }
        ),
      )

    assertEquals(
      listOf(Traffic.fromBytes(120, 340), Traffic.fromBytes(560, 780)),
      api.observeTraffic().take(2).toList(),
    )
  }

  @Test
  fun queryProxyGroupNamesFiltersByModeHiddenAndSelectable() = runTest {
    var requestIndex = 0
    val api =
      DesktopMihomoApi(
        DesktopMihomoEndpoint("127.0.0.1:9090"),
        createTabbyHttpClient(
          MockEngine { request ->
            when (requestIndex++) {
              0 -> {
                assertEquals("http://127.0.0.1:9090/configs", request.url.toString())
                respond("""{"mode":"rule"}""", HttpStatusCode.OK, headers = JSON_HEADERS)
              }
              1 -> {
                assertEquals("http://127.0.0.1:9090/group", request.url.toString())
                respond(
                  """
                  {
                    "proxies": [
                      {"name":"GLOBAL","type":"Selector","all":["Proxy"],"now":"Proxy"},
                      {"name":"Auto","type":"URLTest","all":["Proxy"],"now":"Proxy"},
                      {"name":"Hidden","type":"Selector","all":["Proxy"],"now":"Proxy","hidden":true},
                      {"name":"Select","type":"Selector","all":["Proxy"],"now":"Proxy"}
                    ]
                  }
                  """,
                  HttpStatusCode.OK,
                  headers = JSON_HEADERS,
                )
              }
              else -> error("Unexpected request: ${request.url}")
            }
          }
        ),
      )

    assertEquals(listOf("Select"), api.queryProxyGroupNames(excludeNotSelectable = true))
    assertEquals(2, requestIndex)
  }

  @Test
  fun queryProxyGroupBuildsAndSortsProxyGroup() = runTest {
    var requestIndex = 0
    val api =
      DesktopMihomoApi(
        DesktopMihomoEndpoint("127.0.0.1:9090"),
        createTabbyHttpClient(
          MockEngine { request ->
            when (requestIndex++) {
              0 -> {
                assertEquals(HttpMethod.Get, request.method)
                assertEquals(
                  listOf("proxies", "Auto Group"),
                  request.url.segments,
                )
                respond(
                  """{"name":"Auto Group","type":"URLTest","all":["B","A","C"],"now":"A"}""",
                  HttpStatusCode.OK,
                  headers = JSON_HEADERS,
                )
              }
              1 -> {
                assertEquals("http://127.0.0.1:9090/proxies", request.url.toString())
                respond(
                  """
                  {
                    "proxies": {
                      "A": {"name":"A","type":"Direct","history":[{"delay":30}]},
                      "B": {"name":"B","type":"Reject","history":[{"delay":10}]},
                      "C": {"name":"C","type":"OpenVPN","history":[{"delay":20}]}
                    }
                  }
                  """,
                  HttpStatusCode.OK,
                  headers = JSON_HEADERS,
                )
              }
              else -> error("Unexpected request: ${request.url}")
            }
          }
        ),
      )

    val group = api.queryProxyGroup("Auto Group", ProxySort.Delay)

    assertEquals(Proxy.Type.URLTest, group.type)
    assertEquals("A", group.now)
    assertEquals(listOf("B", "C", "A"), group.proxies.map { it.name })
    assertEquals(listOf(10, 20, 30), group.proxies.map { it.delay })
    assertEquals(Proxy.Type.OpenVPN, group.proxies[1].type)
    assertEquals(2, requestIndex)
  }

  @Test
  fun queryProxyGroupReturnsUnknownForMissingGroup() = runTest {
    val api =
      DesktopMihomoApi(
        DesktopMihomoEndpoint("127.0.0.1:9090"),
        createTabbyHttpClient(MockEngine { respond("missing", HttpStatusCode.NotFound) }),
      )

    val group = api.queryProxyGroup("Missing", ProxySort.Default)

    assertEquals(Proxy.Type.Unknown, group.type)
    assertEquals(emptyList(), group.proxies)
    assertEquals("", group.now)
  }

  @Test
  fun patchSelectorReturnsTrueForNoContentAndSendsPut() = runTest {
    var requestBody: String? = null
    val api =
      DesktopMihomoApi(
        DesktopMihomoEndpoint("127.0.0.1:9090", secret = "token"),
        createTabbyHttpClient(
          MockEngine { request ->
            assertEquals(HttpMethod.Put, request.method)
            assertEquals(
              listOf("proxies", "Select/A"),
              request.url.segments,
            )
            assertEquals("Bearer token", request.headers[HttpHeaders.Authorization])
            assertEquals(ContentType.Application.Json, request.body.contentType)
            requestBody = request.body.toString()
            respond("", HttpStatusCode.NoContent)
          }
        ),
      )

    assertTrue(api.patchSelector("Select/A", "Proxy B"))

    assertNotNull(requestBody)
    assertTrue(requestBody.contains("Proxy B"))
  }

  @Test
  fun patchSelectorReturnsFalseForBadRequest() = runTest {
    val api =
      DesktopMihomoApi(
        DesktopMihomoEndpoint("127.0.0.1:9090"),
        createTabbyHttpClient(
          MockEngine { respond("invalid selector", HttpStatusCode.BadRequest) }
        ),
      )

    assertFalse(api.patchSelector("Auto", "Proxy"))
  }

  @Test
  fun healthCheckUsesGroupDelayEndpoint() = runTest {
    val api =
      DesktopMihomoApi(
        DesktopMihomoEndpoint("127.0.0.1:9090"),
        createTabbyHttpClient(
          MockEngine { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertEquals(
              listOf("group", "Auto Group", "delay"),
              request.url.segments,
            )
            assertEquals("https://www.gstatic.com/generate_204", request.url.parameters["url"])
            assertEquals("5000", request.url.parameters["timeout"])
            assertEquals("204", request.url.parameters["expected"])
            respond("""{"delay":20}""", HttpStatusCode.OK, headers = JSON_HEADERS)
          }
        ),
      )

    api.healthCheck("Auto Group")
  }

  @Test
  fun healthCheckProxyUsesProxyDelayEndpoint() = runTest {
    val api =
      DesktopMihomoApi(
        DesktopMihomoEndpoint("127.0.0.1:9090"),
        createTabbyHttpClient(
          MockEngine { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertEquals(
              listOf("proxies", "Proxy/A", "delay"),
              request.url.segments,
            )
            assertEquals("https://www.gstatic.com/generate_204", request.url.parameters["url"])
            assertEquals("5000", request.url.parameters["timeout"])
            assertEquals("204", request.url.parameters["expected"])
            respond("""{"delay":20}""", HttpStatusCode.OK, headers = JSON_HEADERS)
          }
        ),
      )

    api.healthCheckProxy("Proxy/A")
  }

  private companion object {
    val JSON_HEADERS = io.ktor.http.headersOf(HttpHeaders.ContentType, "application/json")
  }
}
