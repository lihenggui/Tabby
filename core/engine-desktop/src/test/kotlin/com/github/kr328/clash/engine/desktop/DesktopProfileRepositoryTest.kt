package com.github.kr328.clash.engine.desktop

import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.network.ProfileNetworkClient
import com.github.kr328.clash.network.SUBSCRIPTION_USER_INFO_HEADER
import com.github.kr328.clash.network.createTabbyHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class DesktopProfileRepositoryTest {
  @Test
  fun fileProfileCommitAndSetActiveWritesRuntimeConfig() = runTest {
    withTempHome { home ->
      val validator = RecordingValidator()
      val repository = DesktopProfileRepository(homeDir = home, configValidator = validator)
      val uuid = repository.create(Profile.Type.File, "Local")
      val pending = home.resolve("profiles/pending/$uuid")
      val proxyUrl = "https://example.com/proxies.yaml"
      val ruleUrl = "https://example.com/rules.yaml"
      val proxyCache = "proxies/${md5Hex(proxyUrl)}"
      val ruleCache = "rules/${md5Hex(ruleUrl)}"

      pending
        .resolve("config.yaml")
        .writeText(
          """
          proxy-providers:
            remote:
              type: http
              url: $proxyUrl
          rule-providers:
            remote-rules:
              type: http
              url: $ruleUrl
              behavior: classical
          """
            .trimIndent()
        )
      pending.resolve("providers/$proxyCache").parent.createDirectories()
      pending.resolve("providers/$proxyCache").writeText("proxies: []")
      pending.resolve("providers/$ruleCache").parent.createDirectories()
      pending.resolve("providers/$ruleCache").writeText("payload: []")

      repository.commit(uuid)
      val importedProfile = repository.observeProfiles().first().single()

      assertEquals(uuid, importedProfile.uuid)
      assertEquals("Local", importedProfile.name)
      assertTrue(importedProfile.imported)
      assertEquals(false, importedProfile.pending)
      assertEquals(1, validator.configFiles.size)
      assertTrue(home.resolve("profiles/imported/$uuid/config.yaml").isRegularFile())
      assertEquals(null, repository.queryActive())

      repository.setActive(importedProfile)
      val activeProfile = repository.observeProfiles().first().single()

      assertTrue(activeProfile.active)
      assertEquals(uuid, repository.queryActive()?.uuid)
      assertEquals(uuid.toString(), home.resolve("active-profile").readText())
      assertTrue(home.resolve("config.yaml").isRegularFile())
      assertEquals("proxies: []", home.resolve(proxyCache).readText())
      assertEquals("payload: []", home.resolve(ruleCache).readText())
    }
  }

  @Test
  fun explicitProviderPathIsSyncedUnderMihomoHomeDir() = runTest {
    withTempHome { home ->
      val repository =
        DesktopProfileRepository(homeDir = home, configValidator = RecordingValidator())
      val uuid = repository.create(Profile.Type.File, "Local")
      val pending = home.resolve("profiles/pending/$uuid")

      pending
        .resolve("config.yaml")
        .writeText(
          """
          proxy-providers:
            local:
              type: file
              path: ./provider-files/../provider-files/local.yaml
          """
            .trimIndent()
        )
      pending.resolve("providers/provider-files").createDirectories()
      pending.resolve("providers/provider-files/local.yaml").writeText("proxies: []")

      repository.commit(uuid)
      repository.setActive(repository.observeProfiles().first().single())

      assertEquals("proxies: []", home.resolve("provider-files/local.yaml").readText())
    }
  }

  @Test
  fun validationUsesOfficialProviderCacheLayout() = runTest {
    withTempHome { home ->
      val providerUrl = "https://example.com/proxies.yaml"
      val providerCache = "proxies/${md5Hex(providerUrl)}"
      val validator = RecordingValidator { validationHome ->
        assertEquals("proxies: []", validationHome.resolve(providerCache).readText())
      }
      val repository = DesktopProfileRepository(homeDir = home, configValidator = validator)
      val uuid = repository.create(Profile.Type.File, "Local")
      val pending = home.resolve("profiles/pending/$uuid")

      pending
        .resolve("config.yaml")
        .writeText(
          """
          proxy-providers:
            remote:
              type: http
              url: $providerUrl
          """
            .trimIndent()
        )
      pending.resolve("providers/$providerCache").parent.createDirectories()
      pending.resolve("providers/$providerCache").writeText("proxies: []")

      repository.commit(uuid)

      assertEquals(1, validator.configFiles.size)
    }
  }

  @Test
  fun urlProfileCommitFetchesConfigAndSubscriptionMetadata() = runTest {
    withTempHome { home ->
      val repository =
        DesktopProfileRepository(
          homeDir = home,
          networkClient =
            ProfileNetworkClient(
              createTabbyHttpClient(
                MockEngine { request ->
                  assertEquals("https://example.com/config.yaml", request.url.toString())
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
              )
            ),
          configValidator = RecordingValidator(),
        )
      val uuid = repository.create(Profile.Type.Url, "Remote", "https://example.com/config.yaml")

      repository.commit(uuid)
      val profile = repository.observeProfiles().first().single()

      assertEquals("Remote", profile.name)
      assertEquals(Profile.Type.Url, profile.type)
      assertEquals(1, profile.upload)
      assertEquals(2, profile.download)
      assertEquals(3, profile.total)
      assertEquals(4_000, profile.expire)
      assertEquals(
        "mixed-port: 7890",
        home.resolve("profiles/imported/$uuid/config.yaml").readText(),
      )
    }
  }

  @Test
  fun updateActiveUrlProfileRefreshesRuntimeConfig() = runTest {
    withTempHome { home ->
      var requestIndex = 0
      val repository =
        DesktopProfileRepository(
          homeDir = home,
          networkClient =
            ProfileNetworkClient(
              createTabbyHttpClient(
                MockEngine {
                  val content = if (requestIndex++ == 0) "mixed-port: 7890" else "mixed-port: 7891"
                  respond(content, HttpStatusCode.OK)
                }
              )
            ),
          configValidator = RecordingValidator(),
        )
      val uuid = repository.create(Profile.Type.Url, "Remote", "https://example.com/config.yaml")

      repository.commit(uuid)
      repository.setActive(repository.observeProfiles().first().single())
      repository.update(uuid)

      assertEquals("mixed-port: 7891", home.resolve("config.yaml").readText())
      assertEquals(
        "mixed-port: 7891",
        home.resolve("profiles/imported/$uuid/config.yaml").readText(),
      )
      assertEquals(2, requestIndex)
    }
  }

  @Test
  fun patchAndReleaseKeepImportedProfile() = runTest {
    withTempHome { home ->
      val repository =
        DesktopProfileRepository(homeDir = home, configValidator = RecordingValidator())
      val uuid = repository.create(Profile.Type.File, "Original")
      home.resolve("profiles/pending/$uuid/config.yaml").writeText("proxies: []")
      repository.commit(uuid)

      repository.patch(uuid, name = "Edited", source = "", interval = 0)
      val pending = repository.observeProfiles().first().single()
      assertEquals("Edited", pending.name)
      assertTrue(pending.pending)

      repository.release(uuid)
      val imported = repository.observeProfiles().first().single()
      assertEquals("Original", imported.name)
      assertEquals(false, imported.pending)
    }
  }

  @Test
  fun deleteActiveProfileClearsRuntimeFiles() = runTest {
    withTempHome { home ->
      val repository =
        DesktopProfileRepository(homeDir = home, configValidator = RecordingValidator())
      val uuid = repository.create(Profile.Type.File, "Local")
      home.resolve("profiles/pending/$uuid/config.yaml").writeText("proxies: []")
      repository.commit(uuid)
      repository.setActive(repository.observeProfiles().first().single())

      repository.delete(uuid)

      assertEquals(emptyList(), repository.observeProfiles().first())
      assertEquals(null, repository.queryActive())
      assertEquals(false, home.resolve("active-profile").isRegularFile())
      assertEquals(false, home.resolve("config.yaml").isRegularFile())
    }
  }

  private suspend fun withTempHome(block: suspend (Path) -> Unit) {
    val home = createTempDirectory("tabby-desktop-profile")
    try {
      block(home)
    } finally {
      home.toFile().deleteRecursively()
    }
  }

  private class RecordingValidator(private val onValidate: (Path) -> Unit = {}) :
    DesktopMihomoConfigValidator {
    val configFiles = mutableListOf<Path>()

    override suspend fun validate(homeDir: Path, configFile: Path) {
      assertTrue(configFile.isRegularFile())
      onValidate(homeDir)
      configFiles.add(configFile)
    }
  }
}

private fun md5Hex(value: String): String {
  val digest = MessageDigest.getInstance("MD5").digest(value.toByteArray(StandardCharsets.UTF_8))
  val chars = CharArray(digest.size * 2)

  digest.forEachIndexed { index, byte ->
    val unsigned = byte.toInt() and 0xff
    chars[index * 2] = HEX_CHARS[unsigned ushr 4]
    chars[index * 2 + 1] = HEX_CHARS[unsigned and 0x0f]
  }

  return String(chars)
}

private val HEX_CHARS = "0123456789abcdef".toCharArray()
