package com.github.kr328.clash.engine.desktop

import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.network.createTabbyHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest

class DesktopLogRepositoryTest {
  @Test
  fun observeLogsReturnsEmptyFlowWhenApiIsMissing() = runTest {
    assertEquals(emptyList(), DesktopLogRepository().observeLogs().toList())
  }

  @Test
  fun observeLogsDelegatesToMihomoApi() = runTest {
    val api =
      DesktopMihomoApi(
        DesktopMihomoEndpoint("127.0.0.1:9090"),
        createTabbyHttpClient(
          MockEngine {
            respond(
              """{"type":"info","payload":"ready"}""",
              HttpStatusCode.OK,
              headers = JSON_HEADERS,
            )
          }
        ),
        clock = { 5678 },
      )

    assertEquals(
      LogMessage(LogMessage.Level.Info, "ready", 5678),
      DesktopLogRepository(api).observeLogs().first(),
    )
  }

  private companion object {
    val JSON_HEADERS = headersOf(HttpHeaders.ContentType, "application/json")
  }
}
