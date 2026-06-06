package com.github.kr328.clash.service.data

import android.content.Context
import com.github.kr328.clash.database.ProfileDatabase
import com.github.kr328.clash.database.ProfileEntity
import com.github.kr328.clash.database.ProxySelectionEntity
import com.github.kr328.clash.service.PreferenceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class ProfileDatabaseMigration(
  private val context: Context,
  private val roomDatabase: Database = Database.database,
  private val profileDatabase: ProfileDatabase = SqlDelightDatabase.profileDatabase(context),
) {
  private val preferences = PreferenceProvider.createSharedPreferencesFromContext(context)

  suspend fun migrateIfNeeded() {
    if (preferences.getBoolean(MIGRATED_KEY, false)) return

    mutex.withLock {
      if (preferences.getBoolean(MIGRATED_KEY, false)) return

      withContext(Dispatchers.IO) {
        val imported =
          roomDatabase.importedDao().queryAllUUIDs().mapNotNull { uuid ->
            roomDatabase.importedDao().queryByUUID(uuid)
          }
        val pending =
          roomDatabase.pendingDao().queryAllUUIDs().mapNotNull { uuid ->
            roomDatabase.pendingDao().queryByUUID(uuid)
          }
        val selections = imported.flatMap { profile ->
          roomDatabase.selectionProxyDao().querySelections(profile.uuid)
        }

        profileDatabase.transaction {
          imported.forEach { profile -> upsertImported(profile.toProfileEntity()) }
          pending.forEach { profile -> upsertPending(profile.toProfileEntity()) }
          selections.forEach { selection -> setProxySelection(selection.toProxySelectionEntity()) }
        }

        preferences.edit().putBoolean(MIGRATED_KEY, true).apply()
      }
    }
  }

  companion object {
    private const val MIGRATED_KEY = "__profiles_migrated_to_sqldelight_v1"
    private val mutex = Mutex()
  }
}

internal fun Imported.toProfileEntity(): ProfileEntity {
  return ProfileEntity(
    uuid = uuid,
    name = name,
    type = type,
    source = source,
    interval = interval,
    upload = upload,
    download = download,
    total = total,
    expire = expire,
    createdAt = createdAt,
  )
}

internal fun Pending.toProfileEntity(): ProfileEntity {
  return ProfileEntity(
    uuid = uuid,
    name = name,
    type = type,
    source = source,
    interval = interval,
    upload = upload,
    download = download,
    total = total,
    expire = expire,
    createdAt = createdAt,
  )
}

internal fun Selection.toProxySelectionEntity(): ProxySelectionEntity {
  return ProxySelectionEntity(
    uuid = uuid,
    proxy = proxy,
    selected = selected,
  )
}

internal fun ProfileEntity.toImported(): Imported {
  return Imported(
    uuid = uuid,
    name = name,
    type = type,
    source = source,
    interval = interval,
    upload = upload,
    download = download,
    total = total,
    expire = expire,
    createdAt = createdAt,
  )
}

internal fun ProfileEntity.toPending(): Pending {
  return Pending(
    uuid = uuid,
    name = name,
    type = type,
    source = source,
    interval = interval,
    upload = upload,
    download = download,
    total = total,
    expire = expire,
    createdAt = createdAt,
  )
}

internal fun ProxySelectionEntity.toSelection(): Selection {
  return Selection(
    uuid = uuid,
    proxy = proxy,
    selected = selected,
  )
}
