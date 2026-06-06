package com.github.kr328.clash.app

data class TabbyGeoAsset(
  val assetName: String,
  val outputName: String,
)

fun tabbyGeoAssets(): List<TabbyGeoAsset> =
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
  )

fun tabbyGeoFileNeedsRefresh(
  fileExists: Boolean,
  fileLastModifiedMillis: Long,
  packageLastUpdateMillis: Long,
): Boolean = fileExists && fileLastModifiedMillis < packageLastUpdateMillis

fun tabbyGeoFileNeedsExtract(fileExists: Boolean): Boolean = !fileExists
