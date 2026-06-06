package com.github.kr328.clash.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TabbyGeoAssetTest {
  @Test
  fun tabbyGeoAssetsKeepStableAssetAndOutputNames() {
    assertEquals(
      listOf(
        TabbyGeoAsset(
          assetName = "geoip.metadb",
          outputName = "geoip.metadb",
        ),
        TabbyGeoAsset(
          assetName = "geosite.dat",
          outputName = "geosite.dat",
        ),
        TabbyGeoAsset(
          assetName = "ASN.mmdb",
          outputName = "ASN.mmdb",
        ),
      ),
      tabbyGeoAssets(),
    )
  }

  @Test
  fun tabbyGeoFileNeedsRefreshOnlyWhenExistingFileIsOlderThanPackage() {
    assertFalse(
      tabbyGeoFileNeedsRefresh(
        fileExists = false,
        fileLastModifiedMillis = 1,
        packageLastUpdateMillis = 2,
      )
    )
    assertTrue(
      tabbyGeoFileNeedsRefresh(
        fileExists = true,
        fileLastModifiedMillis = 1,
        packageLastUpdateMillis = 2,
      )
    )
    assertFalse(
      tabbyGeoFileNeedsRefresh(
        fileExists = true,
        fileLastModifiedMillis = 2,
        packageLastUpdateMillis = 2,
      )
    )
    assertFalse(
      tabbyGeoFileNeedsRefresh(
        fileExists = true,
        fileLastModifiedMillis = 3,
        packageLastUpdateMillis = 2,
      )
    )
  }

  @Test
  fun tabbyGeoFileNeedsExtractOnlyWhenFileIsMissing() {
    assertTrue(tabbyGeoFileNeedsExtract(fileExists = false))
    assertFalse(tabbyGeoFileNeedsExtract(fileExists = true))
  }
}
