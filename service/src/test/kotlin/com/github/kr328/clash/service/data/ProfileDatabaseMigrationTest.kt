package com.github.kr328.clash.service.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.database.ProfileDatabase
import com.github.kr328.clash.database.TabbyDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class ProfileDatabaseMigrationTest {
  @Test
  fun importProfileMigrationSnapshotCopiesRoomRowsIntoSqlDelight() {
    val database = inMemoryProfileDatabase()
    val importedUuid = Uuid.parse("00000000-0000-0000-0000-000000000001")
    val pendingUuid = Uuid.parse("00000000-0000-0000-0000-000000000002")
    val snapshot =
      ProfileMigrationSnapshot(
        imported =
          listOf(
            Imported(
              uuid = importedUuid,
              name = "Imported",
              type = Profile.Type.Url,
              source = "https://example.com/imported.yaml",
              interval = 3600,
              upload = 1,
              download = 2,
              total = 3,
              expire = 4,
              createdAt = 5,
            )
          ),
        pending =
          listOf(
            Pending(
              uuid = pendingUuid,
              name = "Pending",
              type = Profile.Type.File,
              source = "",
              interval = 0,
              upload = 0,
              download = 0,
              total = 0,
              expire = 0,
              createdAt = 6,
            )
          ),
        selections =
          listOf(
            Selection(
              uuid = importedUuid,
              proxy = "GLOBAL",
              selected = "DIRECT",
            )
          ),
      )

    database.importProfileMigrationSnapshot(snapshot)

    assertEquals(
      snapshot.imported.single().toProfileEntity(),
      database.queryImportedByUuid(importedUuid),
    )
    assertEquals(
      snapshot.pending.single().toProfileEntity(),
      database.queryPendingByUuid(pendingUuid),
    )
    assertEquals(
      listOf(snapshot.selections.single().toProxySelectionEntity()),
      database.queryProxySelections(importedUuid),
    )
  }

  @Test
  fun importProfileMigrationSnapshotCanBeRetriedAfterFlagWriteFailure() {
    val database = inMemoryProfileDatabase()
    val uuid = Uuid.parse("00000000-0000-0000-0000-000000000003")
    val first =
      ProfileMigrationSnapshot(
        imported =
          listOf(
            Imported(
              uuid = uuid,
              name = "Old",
              type = Profile.Type.Url,
              source = "https://example.com/old.yaml",
              interval = 3600,
              upload = 1,
              download = 2,
              total = 3,
              expire = 4,
              createdAt = 5,
            )
          ),
        pending = emptyList(),
        selections = listOf(Selection(uuid = uuid, proxy = "GLOBAL", selected = "DIRECT")),
      )
    val retry =
      first.copy(
        imported =
          listOf(
            first.imported
              .single()
              .copy(
                name = "New",
                source = "https://example.com/new.yaml",
                upload = 10,
              )
          ),
        selections = listOf(Selection(uuid = uuid, proxy = "GLOBAL", selected = "REJECT")),
      )

    database.importProfileMigrationSnapshot(first)
    database.importProfileMigrationSnapshot(retry)

    assertEquals(retry.imported.single().toProfileEntity(), database.queryImportedByUuid(uuid))
    assertEquals(
      listOf(retry.selections.single().toProxySelectionEntity()),
      database.queryProxySelections(uuid),
    )
  }

  private fun inMemoryProfileDatabase(): ProfileDatabase {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    TabbyDatabase.Schema.create(driver)
    return ProfileDatabase(driver)
  }
}
