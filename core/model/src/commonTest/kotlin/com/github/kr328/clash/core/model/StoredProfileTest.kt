package com.github.kr328.clash.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class StoredProfileTest {
  @Test
  fun returnsNullWhenNoStoredProfileExists() {
    assertNull(
      profileFromStoredProfileState(
        uuid = PROFILE_UUID,
        imported = null,
        pending = null,
        activeProfile = PROFILE_UUID,
        updatedAt = 42,
      )
    )
  }

  @Test
  fun pendingProfileFieldsOverrideImportedFieldsWhilePreservingStorageFlags() {
    assertEquals(
      Profile(
        uuid = PROFILE_UUID,
        name = "Pending",
        type = Profile.Type.File,
        source = "content://pending",
        active = true,
        interval = 120,
        upload = 4,
        download = 5,
        total = 6,
        expire = 7,
        updatedAt = 99,
        imported = true,
        pending = true,
      ),
      profileFromStoredProfileState(
        uuid = PROFILE_UUID,
        imported =
          storedProfile(
            name = "Imported",
            type = Profile.Type.Url,
            source = "https://example.com/imported.yaml",
            interval = 60,
            upload = 1,
            download = 2,
            total = 3,
            expire = 4,
          ),
        pending =
          storedProfile(
            name = "Pending",
            type = Profile.Type.File,
            source = "content://pending",
            interval = 120,
            upload = 4,
            download = 5,
            total = 6,
            expire = 7,
          ),
        activeProfile = PROFILE_UUID,
        updatedAt = 99,
      ),
    )
  }

  @Test
  fun activeProfileRequiresImportedProfileAndMatchingUuid() {
    assertEquals(
      false,
      profileFromStoredProfileState(
          uuid = PROFILE_UUID,
          imported = null,
          pending = storedProfile(name = "Pending"),
          activeProfile = PROFILE_UUID,
          updatedAt = 1,
        )
        ?.active,
    )
    assertEquals(
      false,
      profileFromStoredProfileState(
          uuid = PROFILE_UUID,
          imported = storedProfile(name = "Imported"),
          pending = null,
          activeProfile = OTHER_PROFILE_UUID,
          updatedAt = 1,
        )
        ?.active,
    )
    assertEquals(
      true,
      profileFromStoredProfileState(
          uuid = PROFILE_UUID,
          imported = storedProfile(name = "Imported"),
          pending = null,
          activeProfile = PROFILE_UUID,
          updatedAt = 1,
        )
        ?.active,
    )
  }
}

private val PROFILE_UUID = Uuid.parse("00000000-0000-0000-0000-000000000001")
private val OTHER_PROFILE_UUID = Uuid.parse("00000000-0000-0000-0000-000000000002")

private fun storedProfile(
  name: String = "Profile",
  type: Profile.Type = Profile.Type.Url,
  source: String = "https://example.com/config.yaml",
  interval: Long = 60,
  upload: Long = 1,
  download: Long = 2,
  total: Long = 3,
  expire: Long = 4,
): StoredProfile {
  return StoredProfile(
    name = name,
    type = type,
    source = source,
    interval = interval,
    upload = upload,
    download = download,
    total = total,
    expire = expire,
  )
}
