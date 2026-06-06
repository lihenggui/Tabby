package com.github.kr328.clash.app

data class TabbyGeoAsset(
  val assetName: String,
  val outputName: String,
)

sealed interface TabbyGeoFileUpdateAction {
  data object DeleteStaleFile : TabbyGeoFileUpdateAction

  data object ExtractMissingFile : TabbyGeoFileUpdateAction

  data object Ignore : TabbyGeoFileUpdateAction
}

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

fun tabbyGeoFileUpdateAction(
  fileExists: Boolean,
  fileLastModifiedMillis: Long,
  packageLastUpdateMillis: Long,
): TabbyGeoFileUpdateAction =
  when {
    tabbyGeoFileNeedsRefresh(
      fileExists = fileExists,
      fileLastModifiedMillis = fileLastModifiedMillis,
      packageLastUpdateMillis = packageLastUpdateMillis,
    ) -> TabbyGeoFileUpdateAction.DeleteStaleFile
    tabbyGeoFileNeedsExtract(fileExists = fileExists) -> TabbyGeoFileUpdateAction.ExtractMissingFile
    else -> TabbyGeoFileUpdateAction.Ignore
  }

fun tabbyGeoFileDeletedAction(fileExistsAfterDelete: Boolean): TabbyGeoFileUpdateAction =
  if (tabbyGeoFileNeedsExtract(fileExists = fileExistsAfterDelete)) {
    TabbyGeoFileUpdateAction.ExtractMissingFile
  } else {
    TabbyGeoFileUpdateAction.Ignore
  }
