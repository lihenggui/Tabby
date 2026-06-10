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
