package com.github.kr328.clash.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class GitHubReleaseClientTest {
  @Test
  fun fetchLatestReleaseTagReturnsTagName() = runTest {
    val engine = MockEngine { request ->
      assertEquals(
        "https://api.github.com/repos/owner/repository/releases/latest",
        request.url.toString(),
      )
      assertEquals(ContentType.Application.Json.toString(), request.headers[HttpHeaders.Accept])
      respond(
        content = """{"tag_name":"1.2.3","ignored":true}""",
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
      )
    }
    val client = GitHubReleaseClient(createTabbyHttpClient(engine))

    assertEquals("1.2.3", client.fetchLatestReleaseTag("owner/repository"))
  }

  @Test
  fun fetchLatestReleaseTagReturnsNullForFailedStatus() = runTest {
    val client =
      GitHubReleaseClient(
        createTabbyHttpClient(MockEngine { respond("Not found", HttpStatusCode.NotFound) })
      )

    assertEquals(null, client.fetchLatestReleaseTag("owner/repository"))
  }
}
