package com.github.kr328.clash.app

import com.github.kr328.clash.core.model.FetchStatus
import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.engine.api.ProfileRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest

class TabbyInstallProfileRequestTest {
  @Test
  fun tabbyInstallProfileRequestReturnsNullWhenSourceIsMissing() {
    assertNull(
      tabbyInstallProfileRequest(
        source = null,
        type = "url",
        name = "Profile",
        defaultName = "Default",
      )
    )
  }

  @Test
  fun tabbyInstallProfileRequestKeepsUrlTypeAsDefault() {
    assertEquals(
      TabbyInstallProfileRequest(
        type = Profile.Type.Url,
        name = "Default",
        source = "https://example.com/config.yaml",
      ),
      tabbyInstallProfileRequest(
        source = "https://example.com/config.yaml",
        type = null,
        name = null,
        defaultName = "Default",
      ),
    )

    assertEquals(
      Profile.Type.Url,
      tabbyInstallProfileRequest(
          source = "https://example.com/config.yaml",
          type = "unsupported",
          name = "Profile",
          defaultName = "Default",
        )
        ?.type,
    )
  }

  @Test
  fun tabbyInstallProfileRequestParsesFileTypeCaseInsensitively() {
    assertEquals(
      TabbyInstallProfileRequest(
        type = Profile.Type.File,
        name = "Profile",
        source = "content://profiles/config.yaml",
      ),
      tabbyInstallProfileRequest(
        source = "content://profiles/config.yaml",
        type = "FiLe",
        name = "Profile",
        defaultName = "Default",
      ),
    )
  }

  @Test
  fun tabbyInstallProfileRequestFromPlatformPayloadReadsCommonQueryKeys() {
    assertEquals(
      TabbyInstallProfileRequest(
        type = Profile.Type.File,
        name = "Imported",
        source = "content://profiles/config.yaml",
      ),
      tabbyInstallProfileRequestFromPlatformPayload(
        payload =
          TestInstallProfilePayload(
            parameters =
              mapOf(
                "url" to "content://profiles/config.yaml",
                "type" to "file",
                "name" to "Imported",
              )
          ),
        queryParameter = TestInstallProfilePayload::parameter,
        defaultName = "Default",
      ),
    )
  }

  @Test
  fun tabbyInstallProfileRequestFromPlatformPayloadFallsBackToDefaultNameAndUrlType() {
    assertEquals(
      TabbyInstallProfileRequest(
        type = Profile.Type.Url,
        name = "Default",
        source = "https://example.com/config.yaml",
      ),
      tabbyInstallProfileRequestFromPlatformPayload(
        payload =
          TestInstallProfilePayload(parameters = mapOf("url" to "https://example.com/config.yaml")),
        queryParameter = TestInstallProfilePayload::parameter,
        defaultName = "Default",
      ),
    )
  }

  @Test
  fun tabbyInstallProfileCreatesAndPatchesRequestedProfile() = runTest {
    val uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")
    val repository = RecordingProfileRepository(uuid)
    val request =
      TabbyInstallProfileRequest(
        type = Profile.Type.Url,
        name = "Imported",
        source = "https://example.com/config.yaml",
      )

    assertEquals(uuid, tabbyInstallProfile(repository, request))
    assertEquals(
      listOf(RecordingProfileRepository.CreateCall(Profile.Type.Url, "Imported", "")),
      repository.createCalls,
    )
    assertEquals(
      listOf(
        RecordingProfileRepository.PatchCall(
          uuid = uuid,
          name = "Imported",
          source = "https://example.com/config.yaml",
          interval = 0,
        )
      ),
      repository.patchCalls,
    )
  }

  @Test
  fun tabbyInstallProfileResultActionOpensInstalledProfileProperties() {
    val uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")

    assertEquals(
      TabbyInstallProfileResultAction.OpenRoute(
        TabbyExternalRouteAction.OpenProfileProperties(uuid)
      ),
      tabbyInstallProfileResultAction(uuid),
    )
  }

  private data class TestInstallProfilePayload(val parameters: Map<String, String>) {
    fun parameter(name: String): String? {
      return parameters[name]
    }
  }

  private class RecordingProfileRepository(private val createdUuid: Uuid) : ProfileRepository {
    val createCalls = mutableListOf<CreateCall>()
    val patchCalls = mutableListOf<PatchCall>()

    override fun observeProfiles(): Flow<List<Profile>> = emptyFlow()

    override suspend fun queryProfiles(): List<Profile> = unexpected()

    override suspend fun queryByUuid(uuid: Uuid): Profile? = unexpected()

    override suspend fun queryActive(): Profile? = unexpected()

    override suspend fun create(type: Profile.Type, name: String, source: String): Uuid {
      createCalls += CreateCall(type, name, source)
      return createdUuid
    }

    override suspend fun clone(uuid: Uuid): Uuid = unexpected()

    override suspend fun patch(uuid: Uuid, name: String, source: String, interval: Long) {
      patchCalls += PatchCall(uuid, name, source, interval)
    }

    override suspend fun update(uuid: Uuid) = unexpected()

    override suspend fun commit(uuid: Uuid, onStatus: ((FetchStatus) -> Unit)?) = unexpected()

    override suspend fun release(uuid: Uuid) = unexpected()

    override suspend fun delete(uuid: Uuid) = unexpected()

    override suspend fun setActive(profile: Profile) = unexpected()

    private fun unexpected(): Nothing = error("Unexpected repository call")

    data class CreateCall(val type: Profile.Type, val name: String, val source: String)

    data class PatchCall(
      val uuid: Uuid,
      val name: String,
      val source: String,
      val interval: Long,
    )
  }
}
