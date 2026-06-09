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

internal sealed interface GeoFileImportSourceAction {
  data class Import(val action: GeoFileImportAction) : GeoFileImportSourceAction

  data object Fail : GeoFileImportSourceAction
}

internal sealed interface GeoFileImportRequestAction {
  data class RequestPicker(val importType: GeoFileImportType) : GeoFileImportRequestAction

  data object Ignore : GeoFileImportRequestAction
}

sealed interface GeoFileImportResult {
  data object Idle : GeoFileImportResult

  data object InProgress : GeoFileImportResult

  data class Success(val displayName: String) : GeoFileImportResult

  data class UnsupportedFormat(val summary: String) : GeoFileImportResult

  data object Failed : GeoFileImportResult
}

internal sealed interface GeoFileImportPickerResultAction<out SourceT> {
  data class Import<out SourceT>(val importType: GeoFileImportType, val source: SourceT) :
    GeoFileImportPickerResultAction<SourceT>

  data object Fail : GeoFileImportPickerResultAction<Nothing>

  data object Ignore : GeoFileImportPickerResultAction<Nothing>
}

internal sealed interface GeoFileImportResultDisplayAction {
  data class ShowImported(val displayName: String) : GeoFileImportResultDisplayAction

  data class ShowUnsupportedFormat(val summary: String) : GeoFileImportResultDisplayAction

  data object ShowFailed : GeoFileImportResultDisplayAction

  data object Ignore : GeoFileImportResultDisplayAction
}

internal data class GeoFileImportDisplayState(
  val showUnsupportedFormatDialog: Boolean = false,
  val unsupportedFormatSummary: String = "",
)

internal fun geoFileImportInitialResult(): GeoFileImportResult {
  return GeoFileImportResult.Idle
}

internal fun geoFileImportInitialDisplayState(): GeoFileImportDisplayState {
  return GeoFileImportDisplayState()
}

internal fun geoFileImportRequestAction(
  importType: GeoFileImportType?
): GeoFileImportRequestAction {
  return importType?.let { GeoFileImportRequestAction.RequestPicker(it) }
    ?: GeoFileImportRequestAction.Ignore
}

internal fun <SourceT> geoFileImportPickerResultAction(
  pendingImportType: GeoFileImportType?,
  source: SourceT?,
): GeoFileImportPickerResultAction<SourceT> {
  val importType = pendingImportType ?: return GeoFileImportPickerResultAction.Ignore
  val selectedSource = source ?: return GeoFileImportPickerResultAction.Fail

  return GeoFileImportPickerResultAction.Import(
    importType = importType,
    source = selectedSource,
  )
}

internal fun geoFileImportResultDisplayAction(
  result: GeoFileImportResult
): GeoFileImportResultDisplayAction {
  return when (result) {
    GeoFileImportResult.Idle,
    GeoFileImportResult.InProgress -> GeoFileImportResultDisplayAction.Ignore
    is GeoFileImportResult.Success ->
      GeoFileImportResultDisplayAction.ShowImported(result.displayName)
    is GeoFileImportResult.UnsupportedFormat ->
      GeoFileImportResultDisplayAction.ShowUnsupportedFormat(result.summary)
    GeoFileImportResult.Failed -> GeoFileImportResultDisplayAction.ShowFailed
  }
}

internal fun updateGeoFileImportDisplayStateForAction(
  state: GeoFileImportDisplayState,
  action: GeoFileImportResultDisplayAction,
): GeoFileImportDisplayState {
  return when (action) {
    is GeoFileImportResultDisplayAction.ShowUnsupportedFormat ->
      state.copy(
        showUnsupportedFormatDialog = true,
        unsupportedFormatSummary = action.summary,
      )
    GeoFileImportResultDisplayAction.ShowFailed,
    is GeoFileImportResultDisplayAction.ShowImported,
    GeoFileImportResultDisplayAction.Ignore -> state
  }
}

internal fun dismissGeoFileImportUnsupportedFormatDialog(
  state: GeoFileImportDisplayState
): GeoFileImportDisplayState {
  return state.copy(showUnsupportedFormatDialog = false)
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

internal fun geoFileImportSourceAction(
  sourceAvailable: Boolean,
  sourceReadable: Boolean,
  displayName: String?,
  importType: GeoFileImportType,
): GeoFileImportSourceAction {
  if (!sourceAvailable) return GeoFileImportSourceAction.Fail
  if (!sourceReadable) return GeoFileImportSourceAction.Fail

  return GeoFileImportSourceAction.Import(
    action = geoFileImportAction(displayName = displayName.orEmpty(), importType = importType)
  )
}

internal fun geoFileImportSourceActionFromPlatformState(
  sourceAvailable: Boolean,
  sourceReadable: Boolean,
  displayName: () -> String?,
  importType: GeoFileImportType,
): GeoFileImportSourceAction {
  return geoFileImportSourceAction(
    sourceAvailable = sourceAvailable,
    sourceReadable = sourceReadable,
    displayName = if (sourceAvailable && sourceReadable) displayName() else null,
    importType = importType,
  )
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
