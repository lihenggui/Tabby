package com.github.kr328.clash.settings.ui

private val supportedGeoDatabaseExtensions = listOf(".metadb", ".db", ".dat", ".mmdb")

internal sealed interface GeoFileImportPlan {
  data class Supported(val displayName: String, val outputFileName: String) : GeoFileImportPlan

  data class UnsupportedFormat(val supportedExtensionsSummary: String) : GeoFileImportPlan
}

internal sealed interface GeoFileImportAction {
  data class Copy(val displayName: String, val outputFileName: String) : GeoFileImportAction

  data class UnsupportedFormat(val summary: String) : GeoFileImportAction
}

internal sealed interface GeoFileImportResult {
  data object Idle : GeoFileImportResult

  data object InProgress : GeoFileImportResult

  data class Success(val displayName: String) : GeoFileImportResult

  data class UnsupportedFormat(val summary: String) : GeoFileImportResult

  data object Failed : GeoFileImportResult
}

internal fun geoFileImportInitialResult(): GeoFileImportResult {
  return GeoFileImportResult.Idle
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

internal fun geoFileImportAction(
  displayName: String,
  importType: GeoFileImportType,
): GeoFileImportAction {
  return when (val plan = planGeoFileImport(displayName = displayName, importType = importType)) {
    is GeoFileImportPlan.Supported ->
      GeoFileImportAction.Copy(displayName = plan.displayName, outputFileName = plan.outputFileName)
    is GeoFileImportPlan.UnsupportedFormat ->
      GeoFileImportAction.UnsupportedFormat(plan.supportedExtensionsSummary)
  }
}

internal fun geoFileImportStartedResult(): GeoFileImportResult {
  return GeoFileImportResult.InProgress
}

internal fun geoFileImportFailedResult(): GeoFileImportResult {
  return GeoFileImportResult.Failed
}

internal fun geoFileImportResult(
  action: GeoFileImportAction,
  copySucceeded: Boolean = true,
): GeoFileImportResult {
  return when (action) {
    is GeoFileImportAction.Copy ->
      if (copySucceeded) GeoFileImportResult.Success(action.displayName)
      else GeoFileImportResult.Failed
    is GeoFileImportAction.UnsupportedFormat ->
      GeoFileImportResult.UnsupportedFormat(action.summary)
  }
}
