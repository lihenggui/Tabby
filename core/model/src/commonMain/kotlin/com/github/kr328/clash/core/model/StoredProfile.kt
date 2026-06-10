package com.github.kr328.clash.core.model

import kotlin.uuid.Uuid

data class StoredProfile(
  val name: String,
  val type: Profile.Type,
  val source: String,
  val interval: Long,
  val upload: Long,
  val download: Long,
  val total: Long,
  val expire: Long,
)

data class ProfileSubscriptionUserInfo(
  val upload: Long,
  val download: Long,
  val total: Long,
  val expire: Long,
)

fun profileCreatedPendingProfile(type: Profile.Type, name: String, source: String): StoredProfile {
  return StoredProfile(
    name = name,
    type = type,
    source = source,
    interval = 0,
    upload = 0,
    download = 0,
    total = 0,
    expire = 0,
  )
}

fun profileClonedPendingProfile(imported: StoredProfile): StoredProfile {
  return imported.copy(type = Profile.Type.File)
}

fun profilePatchedPendingProfile(
  current: StoredProfile,
  name: String,
  source: String,
  interval: Long,
): StoredProfile {
  return current.copy(
    name = name,
    source = source,
    interval = interval,
    upload = 0,
    download = 0,
    total = 0,
    expire = 0,
  )
}

fun profileWritablePendingProfile(imported: StoredProfile): StoredProfile {
  return imported.copy(
    upload = 0,
    download = 0,
    total = 0,
    expire = 0,
  )
}

data class AppliedImportedProfile(val profile: StoredProfile, val createdAt: Long)

fun profileUpdatedImportedProfile(
  imported: StoredProfile,
  createdAt: Long,
  subscriptionUserInfo: ProfileSubscriptionUserInfo,
): AppliedImportedProfile {
  return AppliedImportedProfile(
    profile =
      imported.copy(
        upload = subscriptionUserInfo.upload,
        download = subscriptionUserInfo.download,
        total = subscriptionUserInfo.total,
        expire = subscriptionUserInfo.expire,
      ),
    createdAt = createdAt,
  )
}

fun profileAppliedImportedProfile(
  pending: StoredProfile,
  oldCreatedAt: Long?,
  currentTimeMillis: Long,
  subscriptionUserInfo: ProfileSubscriptionUserInfo?,
): AppliedImportedProfile? {
  val profile =
    when (pending.type) {
      Profile.Type.Url ->
        pending.copy(
          upload = subscriptionUserInfo?.upload ?: 0,
          download = subscriptionUserInfo?.download ?: 0,
          total = subscriptionUserInfo?.total ?: 0,
          expire = subscriptionUserInfo?.expire ?: 0,
        )
      Profile.Type.File -> pending.copy(upload = 0, download = 0, total = 0, expire = 0)
      Profile.Type.External -> return null
    }

  return AppliedImportedProfile(
    profile = profile,
    createdAt = oldCreatedAt ?: currentTimeMillis,
  )
}

fun profileFromStoredProfileState(
  uuid: Uuid,
  imported: StoredProfile?,
  pending: StoredProfile?,
  activeProfile: Uuid?,
  updatedAt: Long,
): Profile? {
  val current = pending ?: imported ?: return null

  return Profile(
    uuid = uuid,
    name = current.name,
    type = current.type,
    source = current.source,
    active = imported != null && activeProfile == uuid,
    interval = current.interval,
    upload = current.upload,
    download = current.download,
    total = current.total,
    expire = current.expire,
    updatedAt = updatedAt,
    imported = imported != null,
    pending = pending != null,
  )
}
