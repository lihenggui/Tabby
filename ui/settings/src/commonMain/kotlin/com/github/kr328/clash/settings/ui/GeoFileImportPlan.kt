package com.github.kr328.clash.settings.ui

private val supportedGeoDatabaseExtensions = listOf(".metadb", ".db", ".dat", ".mmdb")

internal sealed interface GeoFileImportPlan {
  data class Supported(val displayName: String, val outputFileName: String) : GeoFileImportPlan

  data class UnsupportedFormat(val supportedExtensionsSummary: String) : GeoFileImportPlan
}

internal fun planGeoFileImport(
  displayName: String,
  importType: GeoFileImportType,
): GeoFileImportPlan {
  val extension = "." + displayName.substringAfterLast(".").lowercase()

  if (extension !in supportedGeoDatabaseExtensions) {
    return GeoFileImportPlan.UnsupportedFormat(
      supportedExtensionsSummary = supportedGeoDatabaseExtensions.joinToString("/")
    )
  }

  val outputFileName =
    when (importType) {
      GeoFileImportType.GeoIp -> "geoip$extension"
      GeoFileImportType.GeoSite -> "geosite$extension"
      GeoFileImportType.Country -> "country$extension"
      GeoFileImportType.ASN -> "ASN$extension"
    }

  return GeoFileImportPlan.Supported(displayName = displayName, outputFileName = outputFileName)
}
