package com.github.kr328.clash.profile

import androidx.navigation3.runtime.NavKey
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

sealed interface ProfilesRoute : NavKey {
  @Serializable data class Profiles(val openPropertyUuid: Uuid? = null) : ProfilesRoute

  @Serializable data object Providers : ProfilesRoute

  @Serializable data object NewProfile : ProfilesRoute

  @Serializable data class Files(val uuid: Uuid) : ProfilesRoute

  @Serializable data class Properties(val uuid: Uuid) : ProfilesRoute
}
