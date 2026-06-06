package com.github.kr328.clash.engine.android

import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.engine.api.ProfileRepository
import com.github.kr328.clash.glue.remote.Broadcasts
import com.github.kr328.clash.glue.remote.Remote
import com.github.kr328.clash.glue.util.withProfile
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AndroidProfileRepository : ProfileRepository {
  override fun observeProfiles(): Flow<List<Profile>> =
    flow {
        emit(withProfile { queryAll() })

        Remote.broadcasts.register()
        Remote.broadcasts.event
          .filter { it.requiresProfileSnapshotRefresh() }
          .collect {
            emit(withProfile { queryAll() })
          }
      }
      .flowOn(Dispatchers.IO)

  override suspend fun queryActive(): Profile? {
    return withProfile { queryActive() }
  }

  override suspend fun create(type: Profile.Type, name: String, source: String): Uuid {
    return withProfile { create(type, name, source) }
  }

  override suspend fun clone(uuid: Uuid): Uuid {
    return withProfile { clone(uuid) }
  }

  override suspend fun patch(uuid: Uuid, name: String, source: String, interval: Long) {
    withProfile { patch(uuid, name, source, interval) }
  }

  override suspend fun update(uuid: Uuid) {
    withProfile { update(uuid) }
  }

  override suspend fun commit(uuid: Uuid) {
    withProfile { commit(uuid) }
  }

  override suspend fun release(uuid: Uuid) {
    withProfile { release(uuid) }
  }

  override suspend fun delete(uuid: Uuid) {
    withProfile { delete(uuid) }
  }

  override suspend fun setActive(profile: Profile) {
    withProfile { setActive(profile) }
  }
}

internal fun Broadcasts.Event.requiresProfileSnapshotRefresh(): Boolean =
  when (this) {
    is Broadcasts.Event.ProfileChanged,
    is Broadcasts.Event.ProfileLoaded,
    is Broadcasts.Event.ProfileUpdateCompleted,
    is Broadcasts.Event.ProfileUpdateFailed -> true
    else -> false
  }
