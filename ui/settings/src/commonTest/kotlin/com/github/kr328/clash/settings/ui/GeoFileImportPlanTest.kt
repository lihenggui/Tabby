package com.github.kr328.clash.settings.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class GeoFileImportPlanTest {
  @Test
  fun initialResultUsesIdleState() {
    assertEquals(GeoFileImportResult.Idle, geoFileImportInitialResult())
  }

  @Test
  fun initialDisplayStateHidesUnsupportedFormatDialog() {
    assertEquals(GeoFileImportDisplayState(), geoFileImportInitialDisplayState())
  }

  @Test
  fun importRequestActionRequestsPickerForSelectedImportType() {
    assertEquals(
      GeoFileImportRequestAction.RequestPicker(GeoFileImportType.GeoSite),
      geoFileImportRequestAction(GeoFileImportType.GeoSite),
    )
  }

  @Test
  fun importRequestActionIgnoresMissingImportType() {
    assertEquals(GeoFileImportRequestAction.Ignore, geoFileImportRequestAction(null))
  }

  @Test
  fun ignoresPickerResultWhenPendingImportTypeIsMissing() {
    assertEquals(
      GeoFileImportPickerResultAction.Ignore,
      geoFileImportPickerResultAction(
        pendingImportType = null,
        source = "content://geoip.mmdb",
      ),
    )
  }

  @Test
  fun failsPickerResultWhenSelectedSourceIsMissing() {
    assertEquals(
      GeoFileImportPickerResultAction.Fail,
      geoFileImportPickerResultAction<String>(
        pendingImportType = GeoFileImportType.GeoIp,
        source = null,
      ),
    )
  }

  @Test
  fun createsImportActionForPendingImportTypeAndSelectedSource() {
    assertEquals(
      GeoFileImportPickerResultAction.Import(
        importType = GeoFileImportType.GeoIp,
        source = "content://geoip.mmdb",
      ),
      geoFileImportPickerResultAction(
        pendingImportType = GeoFileImportType.GeoIp,
        source = "content://geoip.mmdb",
      ),
    )
  }

  @Test
  fun ignoresIdleAndInProgressImportResultsForDisplay() {
    assertEquals(
      GeoFileImportResultDisplayAction.Ignore,
      geoFileImportResultDisplayAction(GeoFileImportResult.Idle),
    )
    assertEquals(
      GeoFileImportResultDisplayAction.Ignore,
      geoFileImportResultDisplayAction(GeoFileImportResult.InProgress),
    )
  }

  @Test
  fun mapsSuccessfulImportResultToImportedDisplayAction() {
    assertEquals(
      GeoFileImportResultDisplayAction.ShowImported("GeoSite.DAT"),
      geoFileImportResultDisplayAction(GeoFileImportResult.Success("GeoSite.DAT")),
    )
  }

  @Test
  fun mapsUnsupportedImportResultToUnsupportedFormatDisplayAction() {
    assertEquals(
      GeoFileImportResultDisplayAction.ShowUnsupportedFormat(".metadb/.db/.dat/.mmdb"),
      geoFileImportResultDisplayAction(
        GeoFileImportResult.UnsupportedFormat(".metadb/.db/.dat/.mmdb")
      ),
    )
  }

  @Test
  fun mapsFailedImportResultToFailedDisplayAction() {
    assertEquals(
      GeoFileImportResultDisplayAction.ShowFailed,
      geoFileImportResultDisplayAction(GeoFileImportResult.Failed),
    )
  }

  @Test
  fun unsupportedFormatDisplayActionShowsDialogWithSummary() {
    assertEquals(
      GeoFileImportDisplayState(
        showUnsupportedFormatDialog = true,
        unsupportedFormatSummary = ".metadb/.db/.dat/.mmdb",
      ),
      updateGeoFileImportDisplayStateForAction(
        state = geoFileImportInitialDisplayState(),
        action = GeoFileImportResultDisplayAction.ShowUnsupportedFormat(".metadb/.db/.dat/.mmdb"),
      ),
    )
  }

  @Test
  fun importedAndFailedDisplayActionsKeepExistingDialogState() {
    val state =
      GeoFileImportDisplayState(
        showUnsupportedFormatDialog = true,
        unsupportedFormatSummary = ".dat",
      )

    assertEquals(
      state,
      updateGeoFileImportDisplayStateForAction(
        state = state,
        action = GeoFileImportResultDisplayAction.ShowImported("geosite.dat"),
      ),
    )
    assertEquals(
      state,
      updateGeoFileImportDisplayStateForAction(
        state = state,
        action = GeoFileImportResultDisplayAction.ShowFailed,
      ),
    )
  }

  @Test
  fun dismissUnsupportedFormatDialogPreservesSummary() {
    assertEquals(
      GeoFileImportDisplayState(
        showUnsupportedFormatDialog = false,
        unsupportedFormatSummary = ".dat",
      ),
      dismissGeoFileImportUnsupportedFormatDialog(
        GeoFileImportDisplayState(
          showUnsupportedFormatDialog = true,
          unsupportedFormatSummary = ".dat",
        )
      ),
    )
  }

  @Test
  fun createsGeoIpImportPlanForSupportedExtension() {
    assertEquals(
      GeoFileImportPlan.Supported(
        displayName = "GeoLite2-ASN.metadb",
        outputFileName = "geoip.metadb",
      ),
      planGeoFileImport(
        displayName = "GeoLite2-ASN.metadb",
        importType = GeoFileImportType.GeoIp,
      ),
    )
  }

  @Test
  fun createsTargetSpecificOutputFileNames() {
    assertEquals(
      GeoFileImportPlan.Supported(displayName = "geosite.dat", outputFileName = "geosite.dat"),
      planGeoFileImport(displayName = "geosite.dat", importType = GeoFileImportType.GeoSite),
    )
    assertEquals(
      GeoFileImportPlan.Supported(displayName = "country.mmdb", outputFileName = "country.mmdb"),
      planGeoFileImport(displayName = "country.mmdb", importType = GeoFileImportType.Country),
    )
    assertEquals(
      GeoFileImportPlan.Supported(displayName = "asn.db", outputFileName = "ASN.db"),
      planGeoFileImport(displayName = "asn.db", importType = GeoFileImportType.ASN),
    )
  }

  @Test
  fun lowercasesImportedExtension() {
    assertEquals(
      GeoFileImportPlan.Supported(displayName = "GeoIP.MMDB", outputFileName = "geoip.mmdb"),
      planGeoFileImport(displayName = "GeoIP.MMDB", importType = GeoFileImportType.GeoIp),
    )
  }

  @Test
  fun rejectsUnsupportedExtensions() {
    assertEquals(
      GeoFileImportPlan.UnsupportedFormat(".metadb/.db/.dat/.mmdb"),
      planGeoFileImport(displayName = "geoip.txt", importType = GeoFileImportType.GeoIp),
    )
  }

  @Test
  fun rejectsMissingExtensions() {
    assertEquals(
      GeoFileImportPlan.UnsupportedFormat(".metadb/.db/.dat/.mmdb"),
      planGeoFileImport(displayName = "geoip", importType = GeoFileImportType.GeoIp),
    )
  }

  @Test
  fun createsCopyActionForSupportedImportPlan() {
    assertEquals(
      GeoFileImportAction.Copy(displayName = "GeoSite.DAT", outputFileName = "geosite.dat"),
      geoFileImportAction(displayName = "GeoSite.DAT", importType = GeoFileImportType.GeoSite),
    )
  }

  @Test
  fun createsUnsupportedActionForUnsupportedImportPlan() {
    assertEquals(
      GeoFileImportAction.UnsupportedFormat(".metadb/.db/.dat/.mmdb"),
      geoFileImportAction(displayName = "geoip.txt", importType = GeoFileImportType.GeoIp),
    )
  }

  @Test
  fun sourceActionFailsWhenSourceIsMissing() {
    assertEquals(
      GeoFileImportSourceAction.Fail,
      geoFileImportSourceAction(
        sourceAvailable = false,
        sourceReadable = true,
        displayName = "geoip.mmdb",
        importType = GeoFileImportType.GeoIp,
      ),
    )
  }

  @Test
  fun sourceActionFailsWhenSourceCannotBeRead() {
    assertEquals(
      GeoFileImportSourceAction.Fail,
      geoFileImportSourceAction(
        sourceAvailable = true,
        sourceReadable = false,
        displayName = "geoip.mmdb",
        importType = GeoFileImportType.GeoIp,
      ),
    )
  }

  @Test
  fun sourceActionCreatesImportActionForReadableSource() {
    assertEquals(
      GeoFileImportSourceAction.Import(
        action =
          GeoFileImportAction.Copy(displayName = "GeoSite.DAT", outputFileName = "geosite.dat")
      ),
      geoFileImportSourceAction(
        sourceAvailable = true,
        sourceReadable = true,
        displayName = "GeoSite.DAT",
        importType = GeoFileImportType.GeoSite,
      ),
    )
  }

  @Test
  fun sourceActionUsesEmptyDisplayNameWhenMetadataIsMissing() {
    assertEquals(
      GeoFileImportSourceAction.Import(
        action = GeoFileImportAction.UnsupportedFormat(".metadb/.db/.dat/.mmdb")
      ),
      geoFileImportSourceAction(
        sourceAvailable = true,
        sourceReadable = true,
        displayName = null,
        importType = GeoFileImportType.GeoIp,
      ),
    )
  }

  @Test
  fun platformSourceActionSkipsDisplayNameWhenSourceIsMissing() {
    var displayNameLoaded = false

    assertEquals(
      GeoFileImportSourceAction.Fail,
      geoFileImportSourceActionFromPlatformState(
        sourceAvailable = false,
        sourceReadable = true,
        displayName = {
          displayNameLoaded = true
          "geoip.mmdb"
        },
        importType = GeoFileImportType.GeoIp,
      ),
    )
    assertEquals(false, displayNameLoaded)
  }

  @Test
  fun platformSourceActionSkipsDisplayNameWhenSourceCannotBeRead() {
    var displayNameLoaded = false

    assertEquals(
      GeoFileImportSourceAction.Fail,
      geoFileImportSourceActionFromPlatformState(
        sourceAvailable = true,
        sourceReadable = false,
        displayName = {
          displayNameLoaded = true
          "geoip.mmdb"
        },
        importType = GeoFileImportType.GeoIp,
      ),
    )
    assertEquals(false, displayNameLoaded)
  }

  @Test
  fun platformSourceActionLoadsDisplayNameOnlyForReadableSource() {
    var displayNameLoaded = false

    assertEquals(
      GeoFileImportSourceAction.Import(
        action =
          GeoFileImportAction.Copy(displayName = "GeoSite.DAT", outputFileName = "geosite.dat")
      ),
      geoFileImportSourceActionFromPlatformState(
        sourceAvailable = true,
        sourceReadable = true,
        displayName = {
          displayNameLoaded = true
          "GeoSite.DAT"
        },
        importType = GeoFileImportType.GeoSite,
      ),
    )
    assertEquals(true, displayNameLoaded)
  }

  @Test
  fun importStartedAndFailedResultsMapToCommonStates() {
    assertEquals(GeoFileImportResult.InProgress, geoFileImportStartedResult())
    assertEquals(GeoFileImportResult.Failed, geoFileImportFailedResult())
  }

  @Test
  fun importActionResultMapsUnsupportedFormatToResult() {
    assertEquals(
      GeoFileImportResult.UnsupportedFormat(".metadb/.db/.dat/.mmdb"),
      geoFileImportResult(GeoFileImportAction.UnsupportedFormat(".metadb/.db/.dat/.mmdb")),
    )
  }

  @Test
  fun importActionResultMapsCopyOutcomeToResult() {
    val action =
      GeoFileImportAction.Copy(displayName = "GeoSite.DAT", outputFileName = "geosite.dat")

    assertEquals(
      GeoFileImportResult.Success("GeoSite.DAT"),
      geoFileImportResult(action, copySucceeded = true),
    )
    assertEquals(
      GeoFileImportResult.Failed,
      geoFileImportResult(action, copySucceeded = false),
    )
  }
}
