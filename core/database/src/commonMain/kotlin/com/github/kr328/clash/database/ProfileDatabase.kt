package com.github.kr328.clash.database

import app.cash.sqldelight.db.SqlDriver
import com.github.kr328.clash.core.model.Profile
import kotlin.uuid.Uuid

class ProfileDatabase(driver: SqlDriver) {
  private val database = TabbyDatabase(driver)
  private val queries = database.profilesQueries

  fun queryImportedByUuid(uuid: Uuid): ProfileEntity? {
    return queries.selectImportedByUuid(uuid.toString()).executeAsOneOrNull()?.toProfileEntity()
  }

  fun queryPendingByUuid(uuid: Uuid): ProfileEntity? {
    return queries.selectPendingByUuid(uuid.toString()).executeAsOneOrNull()?.toProfileEntity()
  }

  fun queryImportedUuids(): List<Uuid> {
    return queries.selectImportedUuids().executeAsList().map(Uuid::parse)
  }

  fun queryPendingUuids(): List<Uuid> {
    return queries.selectPendingUuids().executeAsList().map(Uuid::parse)
  }

  fun insertImported(profile: ProfileEntity) {
    queries.insertImported(
      uuid = profile.uuid.toString(),
      name = profile.name,
      type = profile.type.name,
      source = profile.source,
      interval = profile.interval,
      upload = profile.upload,
      download = profile.download,
      total = profile.total,
      expire = profile.expire,
      createdAt = profile.createdAt,
    )
  }

  fun upsertImported(profile: ProfileEntity) {
    if (importedExists(profile.uuid)) {
      updateImported(profile)
    } else {
      insertImported(profile)
    }
  }

  fun updateImported(profile: ProfileEntity) {
    queries.updateImported(
      uuid = profile.uuid.toString(),
      name = profile.name,
      type = profile.type.name,
      source = profile.source,
      interval = profile.interval,
      upload = profile.upload,
      download = profile.download,
      total = profile.total,
      expire = profile.expire,
      createdAt = profile.createdAt,
    )
  }

  fun deleteImported(uuid: Uuid) {
    queries.transaction {
      queries.deleteProxySelectionsByUuid(uuid.toString())
      queries.deleteImported(uuid.toString())
    }
  }

  fun importedExists(uuid: Uuid): Boolean {
    return queries.countImportedByUuid(uuid.toString()).executeAsOne() > 0
  }

  fun upsertPending(profile: ProfileEntity) {
    queries.upsertPending(
      uuid = profile.uuid.toString(),
      name = profile.name,
      type = profile.type.name,
      source = profile.source,
      interval = profile.interval,
      upload = profile.upload,
      download = profile.download,
      total = profile.total,
      expire = profile.expire,
      createdAt = profile.createdAt,
    )
  }

  fun deletePending(uuid: Uuid) {
    queries.deletePending(uuid.toString())
  }

  fun pendingExists(uuid: Uuid): Boolean {
    return queries.countPendingByUuid(uuid.toString()).executeAsOne() > 0
  }

  fun setProxySelection(selection: ProxySelectionEntity) {
    queries.setProxySelection(
      uuid = selection.uuid.toString(),
      proxy = selection.proxy,
      selected = selection.selected,
    )
  }

  fun deleteProxySelection(uuid: Uuid, proxy: String) {
    queries.deleteProxySelection(uuid.toString(), proxy)
  }

  fun queryProxySelections(uuid: Uuid): List<ProxySelectionEntity> {
    return queries.selectProxySelections(uuid.toString()).executeAsList().map { it.toEntity() }
  }

  fun deleteProxySelections(uuid: Uuid, proxies: List<String>) {
    queries.transaction {
      proxies.forEach { proxy -> queries.deleteProxySelection(uuid.toString(), proxy) }
    }
  }

  fun deleteProxySelections(uuid: Uuid) {
    queries.deleteProxySelectionsByUuid(uuid.toString())
  }

  fun transaction(block: ProfileDatabase.() -> Unit) {
    queries.transaction { block() }
  }
}

data class ProfileEntity(
  val uuid: Uuid,
  val name: String,
  val type: Profile.Type,
  val source: String,
  val interval: Long,
  val upload: Long,
  val download: Long,
  val total: Long,
  val expire: Long,
  val createdAt: Long,
)

data class ProxySelectionEntity(
  val uuid: Uuid,
  val proxy: String,
  val selected: String,
)

private fun Imported.toProfileEntity(): ProfileEntity {
  return ProfileEntity(
    uuid = Uuid.parse(uuid),
    name = name,
    type = Profile.Type.valueOf(type),
    source = source,
    interval = interval,
    upload = upload,
    download = download,
    total = total,
    expire = expire,
    createdAt = createdAt,
  )
}

private fun Pending.toProfileEntity(): ProfileEntity {
  return ProfileEntity(
    uuid = Uuid.parse(uuid),
    name = name,
    type = Profile.Type.valueOf(type),
    source = source,
    interval = interval,
    upload = upload,
    download = download,
    total = total,
    expire = expire,
    createdAt = createdAt,
  )
}

private fun Selections.toEntity(): ProxySelectionEntity {
  return ProxySelectionEntity(
    uuid = Uuid.parse(uuid),
    proxy = proxy,
    selected = selected,
  )
}
