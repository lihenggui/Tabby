package com.github.kr328.clash.engine.desktop

import com.github.kr328.clash.core.model.Provider
import com.github.kr328.clash.core.model.Traffic
import com.github.kr328.clash.core.model.TunnelState
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
  fun queryStateDelegatesToMihomoConfigsMode() = runTest {
    val api =
      DesktopMihomoApi(
        DesktopMihomoEndpoint("127.0.0.1:9090"),
        createTabbyHttpClient(
          MockEngine {
            respond("""{"mode":"global"}""", HttpStatusCode.OK, headers = JSON_HEADERS)
          }
        ),
      )
    val controller = DesktopEngineController(mihomoApi = api)

    val state = controller.queryState()

    assertEquals(TunnelState(TunnelState.Mode.Global), state)
    assertEquals(state, controller.state.value)
  }

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

  @Test
  fun queryProvidersDelegatesToMihomoApi() = runTest {
    var requestIndex = 0
    val api =
      DesktopMihomoApi(
        DesktopMihomoEndpoint("127.0.0.1:9090"),
        createTabbyHttpClient(
          MockEngine { request ->
            when (requestIndex++) {
              0 ->
                respond(
                  """
                  {
                    "providers": {
                      "ProxyRemote": {
                        "name": "ProxyRemote",
                        "type": "Proxy",
                        "vehicleType": "HTTP",
                        "updatedAt": 1234
                      }
                    }
                  }
                  """,
                  HttpStatusCode.OK,
                  headers = JSON_HEADERS,
                )
              1 -> respond("""{"providers":{}}""", HttpStatusCode.OK, headers = JSON_HEADERS)
              else -> error("Unexpected request: ${request.url}")
            }
          }
        ),
      )
    val controller = DesktopEngineController(mihomoApi = api)

    assertEquals(
      listOf(Provider("ProxyRemote", Provider.Type.Proxy, Provider.VehicleType.HTTP, 1234)),
      controller.queryProviders(),
    )
    assertEquals(2, requestIndex)
  }

  private companion object {
    val JSON_HEADERS = io.ktor.http.headersOf(HttpHeaders.ContentType, "application/json")
  }
}
