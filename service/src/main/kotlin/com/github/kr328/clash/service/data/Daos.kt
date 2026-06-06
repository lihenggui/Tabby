package com.github.kr328.clash.service.data

import com.github.kr328.clash.common.Global
import com.github.kr328.clash.database.ProfileDatabase
import kotlin.uuid.Uuid

fun ImportedDao(): ImportedDao {
  return SqlDelightImportedDao()
}

fun PendingDao(): PendingDao {
  return SqlDelightPendingDao()
}

fun SelectionDao(): SelectionDao {
  return SqlDelightSelectionDao()
}

private class SqlDelightImportedDao(
  private val database: ProfileDatabase = SqlDelightDatabase.profileDatabase(),
  private val migration: ProfileDatabaseMigration = ProfileDatabaseMigration(Global.application),
) : ImportedDao {
  override suspend fun queryByUUID(uuid: Uuid): Imported? {
    migration.migrateIfNeeded()
    return database.queryImportedByUuid(uuid)?.toImported()
  }

  override suspend fun queryAllUUIDs(): List<Uuid> {
    migration.migrateIfNeeded()
    return database.queryImportedUuids()
  }

  override suspend fun insert(imported: Imported): Long {
    migration.migrateIfNeeded()
    database.insertImported(imported.toProfileEntity())
    return 0L
  }

  override suspend fun update(imported: Imported) {
    migration.migrateIfNeeded()
    database.updateImported(imported.toProfileEntity())
  }

  override suspend fun remove(uuid: Uuid) {
    migration.migrateIfNeeded()
    database.deleteImported(uuid)
  }

  override suspend fun exists(uuid: Uuid): Boolean {
    migration.migrateIfNeeded()
    return database.importedExists(uuid)
  }
}

private class SqlDelightPendingDao(
  private val database: ProfileDatabase = SqlDelightDatabase.profileDatabase(),
  private val migration: ProfileDatabaseMigration = ProfileDatabaseMigration(Global.application),
) : PendingDao {
  override suspend fun queryByUUID(uuid: Uuid): Pending? {
    migration.migrateIfNeeded()
    return database.queryPendingByUuid(uuid)?.toPending()
  }

  override suspend fun remove(uuid: Uuid) {
    migration.migrateIfNeeded()
    database.deletePending(uuid)
  }

  override suspend fun exists(uuid: Uuid): Boolean {
    migration.migrateIfNeeded()
    return database.pendingExists(uuid)
  }

  override suspend fun queryAllUUIDs(): List<Uuid> {
    migration.migrateIfNeeded()
    return database.queryPendingUuids()
  }

  override suspend fun insert(pending: Pending) {
    migration.migrateIfNeeded()
    database.upsertPending(pending.toProfileEntity())
  }

  override suspend fun update(pending: Pending) {
    migration.migrateIfNeeded()
    database.upsertPending(pending.toProfileEntity())
  }
}

private class SqlDelightSelectionDao(
  private val database: ProfileDatabase = SqlDelightDatabase.profileDatabase(),
  private val migration: ProfileDatabaseMigration = ProfileDatabaseMigration(Global.application),
) : SelectionDao {
  override suspend fun setSelected(selection: Selection) {
    migration.migrateIfNeeded()
    database.setProxySelection(selection.toProxySelectionEntity())
  }

  override suspend fun removeSelected(uuid: Uuid, proxy: String) {
    migration.migrateIfNeeded()
    database.deleteProxySelection(uuid, proxy)
  }

  override suspend fun querySelections(uuid: Uuid): List<Selection> {
    migration.migrateIfNeeded()
    return database.queryProxySelections(uuid).map { it.toSelection() }
  }

  override suspend fun removeSelections(uuid: Uuid, proxies: List<String>) {
    migration.migrateIfNeeded()
    database.deleteProxySelections(uuid, proxies)
  }
}
