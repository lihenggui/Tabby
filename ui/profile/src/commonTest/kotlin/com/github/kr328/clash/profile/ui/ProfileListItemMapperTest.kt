package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class ProfileListItemMapperTest {
  @Test
  fun mapsPendingProfileWithTrafficAndExpireText() {
    val profile =
      profile(
        type = Profile.Type.Url,
        pending = true,
        upload = 20,
        download = 30,
        total = 100,
        expire = 1234,
        updatedAt = 900,
      )

    val item = profile.toProfileListItem(currentTime = 1000)

    assertEquals(profile, item.profile)
    assertEquals("Url (Unsaved)", item.typeText)
    assertEquals("50B / 100B", item.usageText)
    assertEquals("expire:1234", item.expireText)
    assertEquals("elapsed:100", item.updatedAtText)
    assertEquals(500, item.trafficProgress)
  }

  @Test
  fun hidesTrafficAndExpireTextWhenProfileHasNoUsableUsage() {
    val profile =
      profile(
        type = Profile.Type.File,
        pending = false,
        upload = 100,
        download = 1,
        total = 100,
        expire = 0,
        updatedAt = 750,
      )

    val item = profile.toProfileListItem(currentTime = 1000)

    assertEquals("File", item.typeText)
    assertNull(item.usageText)
    assertNull(item.expireText)
    assertEquals("elapsed:250", item.updatedAtText)
    assertEquals(0, item.trafficProgress)
  }

  @Test
  fun clampsTrafficProgressToFullScale() {
    val item =
      profile(upload = 90, download = 20, total = 100).toProfileListItem(currentTime = 1000)

    assertEquals("110B / 100B", item.usageText)
    assertEquals(1000, item.trafficProgress)
  }

  @Test
  fun mapsProfileUiStateProfilesInOrderUsingStateCurrentTime() {
    val first = profile(type = Profile.Type.File, updatedAt = 900)
    val second = profile(type = Profile.Type.Url, pending = true, updatedAt = 700)

    val items =
      ProfilesUiState(profiles = listOf(first, second), currentTime = 1000)
        .toProfileListItems(
          formatTypeText = { it.testString() },
          formatBytes = { "${it}B" },
          formatExpire = { "expire:$it" },
          formatElapsedMillis = { "elapsed:$it" },
        )

    assertEquals(listOf(first, second), items.map(ProfileListItem::profile))
    assertEquals(listOf("File", "Url (Unsaved)"), items.map(ProfileListItem::typeText))
    assertEquals(listOf("elapsed:100", "elapsed:300"), items.map(ProfileListItem::updatedAtText))
  }

  @Test
  fun profileTypeTextMapsTypeAndPendingStateToCommonTokens() {
    assertEquals(
      ProfileTypeText(token = ProfileTypeTextToken.File, pending = false),
      profileTypeText(type = Profile.Type.File, pending = false),
    )
    assertEquals(
      ProfileTypeText(token = ProfileTypeTextToken.Url, pending = true),
      profileTypeText(type = Profile.Type.Url, pending = true),
    )
    assertEquals(
      ProfileTypeText(token = ProfileTypeTextToken.External, pending = false),
      profileTypeText(type = Profile.Type.External, pending = false),
    )
  }

  @Test
  fun profileTypeTextResourceTokenMapsTypeTokens() {
    assertEquals(
      "file",
      profileTypeTextResourceToken(
        token = ProfileTypeTextToken.File,
        file = "file",
        url = "url",
        external = "external",
      ),
    )
    assertEquals(
      "url",
      profileTypeTextResourceToken(
        token = ProfileTypeTextToken.Url,
        file = "file",
        url = "url",
        external = "external",
      ),
    )
    assertEquals(
      "external",
      profileTypeTextResourceToken(
        token = ProfileTypeTextToken.External,
        file = "file",
        url = "url",
        external = "external",
      ),
    )
  }

  private fun Profile.toProfileListItem(currentTime: Long): ProfileListItem {
    return toProfileListItem(
      currentTime = currentTime,
      formatTypeText = { it.testString() },
      formatBytes = { "${it}B" },
      formatExpire = { "expire:$it" },
      formatElapsedMillis = { "elapsed:$it" },
    )
  }

  private fun profile(
    type: Profile.Type = Profile.Type.Url,
    pending: Boolean = false,
    upload: Long = 0,
    download: Long = 0,
    total: Long = 0,
    expire: Long = 0,
    updatedAt: Long = 0,
  ): Profile {
    return Profile(
      uuid = Uuid.parse("00000000-0000-0000-0000-000000000001"),
      name = "Profile",
      type = type,
      source = "",
      active = false,
      interval = 0,
      upload = upload,
      download = download,
      total = total,
      expire = expire,
      updatedAt = updatedAt,
      imported = true,
      pending = pending,
    )
  }

  private fun ProfileTypeText.testString(): String {
    val text = token.name

    return if (pending) "$text (Unsaved)" else text
  }
}
