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

data class AppliedImportedProfile(val profile: StoredProfile, val createdAt: Long)

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
