package com.github.kr328.clash.profile.ui

import com.github.kr328.clash.core.model.Profile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class ProfileTrafficUsageTest {
  @Test
  fun hidesTrafficUsageWhenDownloadedBytesAreBelowThreshold() {
    assertEquals(false, profile(download = 1, total = 100).showsTrafficUsage())
  }

  @Test
  fun hidesTrafficUsageWhenTotalBytesAreBelowThreshold() {
    assertEquals(false, profile(download = 2, total = 1).showsTrafficUsage())
  }

  @Test
  fun showsTrafficUsageWhenDownloadedAndTotalBytesPassThresholds() {
    assertEquals(true, profile(download = 2, total = 2).showsTrafficUsage())
  }

  @Test
  fun trafficProgressUsesThousandPointScale() {
    assertEquals(
      500,
      profile(download = 25, upload = 25, total = 100).trafficProgress(usedTraffic = 50),
    )
  }

  @Test
  fun trafficProgressClampsToFullScale() {
    assertEquals(
      1000,
      profile(download = 90, upload = 20, total = 100).trafficProgress(usedTraffic = 110),
    )
  }

  private fun profile(
    download: Long,
    upload: Long = 0,
    total: Long,
  ): Profile {
    return Profile(
      uuid = Uuid.parse("00000000-0000-0000-0000-000000000001"),
      name = "Profile",
      type = Profile.Type.Url,
      source = "",
      active = false,
      interval = 0,
      upload = upload,
      download = download,
      total = total,
      expire = 0,
      updatedAt = 0,
      imported = true,
      pending = false,
    )
  }
}
