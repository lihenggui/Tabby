package com.github.kr328.clash.engine.ios

import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.engine.api.ProfileRepository
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class IosProfileRepository : ProfileRepository {
  override fun observeProfiles(): Flow<List<Profile>> {
    return flowOf(emptyList())
  }

  override suspend fun create(type: Profile.Type, name: String, source: String): Uuid {
    unsupported()
  }

  override suspend fun clone(uuid: Uuid): Uuid {
    unsupported()
  }

  override suspend fun patch(uuid: Uuid, name: String, source: String, interval: Long) {
    unsupported()
  }

  override suspend fun update(uuid: Uuid) {
    unsupported()
  }

  override suspend fun commit(uuid: Uuid) {
    unsupported()
  }

  override suspend fun release(uuid: Uuid) {
    unsupported()
  }

  override suspend fun delete(uuid: Uuid) {
    unsupported()
  }

  override suspend fun setActive(profile: Profile) {
    unsupported()
  }
}
