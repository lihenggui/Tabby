package com.github.kr328.clash.home.api

import com.github.kr328.clash.glue.util.TABBY_REPO
import com.github.kr328.clash.network.GitHubReleaseClient

class HelpApi(private val releaseClient: GitHubReleaseClient = GitHubReleaseClient()) {
  suspend fun getLatestRelease(): String? = releaseClient.fetchLatestReleaseTag(TABBY_REPO)
}
