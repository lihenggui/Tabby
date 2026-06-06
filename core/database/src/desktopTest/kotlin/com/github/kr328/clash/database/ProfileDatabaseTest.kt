package com.github.kr328.clash.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.github.kr328.clash.core.model.Profile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class ProfileDatabaseTest {
  @Test
  fun profileTablesSupportRoomEquivalentOperations() {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    TabbyDatabase.Schema.create(driver)
    val database = ProfileDatabase(driver)
    val uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")
    val imported =
      ProfileEntity(
        uuid = uuid,
        name = "Default",
        type = Profile.Type.Url,
        source = "https://example.com/sub.yaml",
        interval = 3600,
        upload = 1,
        download = 2,
        total = 3,
        expire = 4,
        createdAt = 5,
      )

    database.insertImported(imported)

    assertTrue(database.importedExists(uuid))
    assertEquals(listOf(uuid), database.queryImportedUuids())
    assertEquals(imported, database.queryImportedByUuid(uuid))

    val pending = imported.copy(name = "Edited", interval = 7200, createdAt = 6)
    database.upsertPending(pending)

    assertTrue(database.pendingExists(uuid))
    assertEquals(listOf(uuid), database.queryPendingUuids())
    assertEquals(pending, database.queryPendingByUuid(uuid))

    val selection = ProxySelectionEntity(uuid = uuid, proxy = "GLOBAL", selected = "DIRECT")
    database.setProxySelection(selection)

    assertEquals(listOf(selection), database.queryProxySelections(uuid))

    database.deleteProxySelection(uuid, "GLOBAL")

    assertEquals(emptyList(), database.queryProxySelections(uuid))

    database.setProxySelection(selection)
    database.upsertImported(imported.copy(name = "Updated"))

    assertEquals("Updated", database.queryImportedByUuid(uuid)?.name)

    database.deletePending(uuid)
    database.deleteImported(uuid)

    assertFalse(database.pendingExists(uuid))
    assertFalse(database.importedExists(uuid))
    assertEquals(emptyList(), database.queryProxySelections(uuid))
  }
}
