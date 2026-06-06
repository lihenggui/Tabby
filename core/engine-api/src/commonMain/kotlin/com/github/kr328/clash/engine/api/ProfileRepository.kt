package com.github.kr328.clash.engine.api

import com.github.kr328.clash.core.model.FetchStatus
import com.github.kr328.clash.core.model.Profile
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
  fun observeProfiles(): Flow<List<Profile>>

  suspend fun queryProfiles(): List<Profile>

  suspend fun queryByUuid(uuid: Uuid): Profile?

  suspend fun queryActive(): Profile?

  suspend fun create(type: Profile.Type, name: String, source: String = ""): Uuid

  suspend fun clone(uuid: Uuid): Uuid

  suspend fun patch(uuid: Uuid, name: String, source: String, interval: Long)

  suspend fun update(uuid: Uuid)

  suspend fun commit(uuid: Uuid, onStatus: ((FetchStatus) -> Unit)? = null)

  suspend fun release(uuid: Uuid)

  suspend fun delete(uuid: Uuid)

  suspend fun setActive(profile: Profile)
}
