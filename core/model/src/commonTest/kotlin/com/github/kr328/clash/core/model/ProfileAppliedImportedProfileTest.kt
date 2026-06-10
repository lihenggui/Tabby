package com.github.kr328.clash.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProfileAppliedImportedProfileTest {
  @Test
  fun urlProfileUsesSubscriptionUserInfoAndExistingCreatedAt() {
    assertEquals(
      AppliedImportedProfile(
        profile =
          storedProfile(
            type = Profile.Type.Url,
            upload = 11,
            download = 22,
            total = 33,
            expire = 44,
          ),
        createdAt = 100,
      ),
      profileAppliedImportedProfile(
        pending = storedProfile(type = Profile.Type.Url),
        oldCreatedAt = 100,
        currentTimeMillis = 200,
        subscriptionUserInfo =
          ProfileSubscriptionUserInfo(upload = 11, download = 22, total = 33, expire = 44),
      ),
    )
  }

  @Test
  fun urlProfileClearsSubscriptionFieldsWhenUserInfoIsMissing() {
    assertEquals(
      AppliedImportedProfile(
        profile = storedProfile(type = Profile.Type.Url),
        createdAt = 200,
      ),
      profileAppliedImportedProfile(
        pending =
          storedProfile(
            type = Profile.Type.Url,
            upload = 1,
            download = 2,
            total = 3,
            expire = 4,
          ),
        oldCreatedAt = null,
        currentTimeMillis = 200,
        subscriptionUserInfo = null,
      ),
    )
  }

  @Test
  fun fileProfileClearsSubscriptionFields() {
    assertEquals(
      AppliedImportedProfile(
        profile = storedProfile(type = Profile.Type.File),
        createdAt = 200,
      ),
      profileAppliedImportedProfile(
        pending =
          storedProfile(
            type = Profile.Type.File,
            upload = 1,
            download = 2,
            total = 3,
            expire = 4,
          ),
        oldCreatedAt = null,
        currentTimeMillis = 200,
        subscriptionUserInfo =
          ProfileSubscriptionUserInfo(upload = 11, download = 22, total = 33, expire = 44),
      ),
    )
  }

  @Test
  fun externalProfileDoesNotProduceImportedMetadata() {
    assertNull(
      profileAppliedImportedProfile(
        pending = storedProfile(type = Profile.Type.External),
        oldCreatedAt = null,
        currentTimeMillis = 200,
        subscriptionUserInfo =
          ProfileSubscriptionUserInfo(upload = 11, download = 22, total = 33, expire = 44),
      )
    )
  }
}

private fun storedProfile(
  type: Profile.Type,
  upload: Long = 0,
  download: Long = 0,
  total: Long = 0,
  expire: Long = 0,
): StoredProfile {
  return StoredProfile(
    name = "Profile",
    type = type,
    source = "https://example.com/config.yaml",
    interval = 60,
    upload = upload,
    download = download,
    total = total,
    expire = expire,
  )
}
