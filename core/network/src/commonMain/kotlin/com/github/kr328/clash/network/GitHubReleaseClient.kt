package com.github.kr328.clash.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class GitHubReleaseClient(private val httpClient: HttpClient = createTabbyHttpClient()) {
  suspend fun fetchLatestReleaseTag(repository: String): String? {
    val response =
      httpClient.get("https://api.github.com/repos/$repository/releases/latest") {
        header(HttpHeaders.Accept, ContentType.Application.Json.toString())
      }

    if (response.status.value !in 200..299) return null

    return response.body<GitHubRelease>().tagName
  }
}

@Serializable private data class GitHubRelease(@SerialName("tag_name") val tagName: String)
