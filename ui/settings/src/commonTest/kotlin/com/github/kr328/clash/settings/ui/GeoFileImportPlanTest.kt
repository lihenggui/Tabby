package com.github.kr328.clash.settings.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class GeoFileImportPlanTest {
  @Test
  fun initialResultUsesIdleState() {
    assertEquals(GeoFileImportResult.Idle, geoFileImportInitialResult())
  }

  @Test
  fun ignoresPickerResultWhenPendingImportTypeIsMissing() {
    assertEquals(
      GeoFileImportPickerResultAction.Ignore,
      geoFileImportPickerResultAction(source = TestSource("geoip.mmdb"), pendingImportType = null),
    )
  }

  @Test
  fun createsImportActionForSelectedPickerSource() {
    val source = TestSource("geoip.mmdb")

    assertEquals(
      GeoFileImportPickerResultAction.Import(
        source = source,
        importType = GeoFileImportType.GeoIp,
      ),
      geoFileImportPickerResultAction(
        source = source,
        pendingImportType = GeoFileImportType.GeoIp,
      ),
    )
  }

  @Test
  fun createsImportActionForMissingPickerSourceWhenTypeIsPending() {
    assertEquals(
      GeoFileImportPickerResultAction.Import(
        source = null,
        importType = GeoFileImportType.Country,
      ),
      geoFileImportPickerResultAction<TestSource>(
        source = null,
        pendingImportType = GeoFileImportType.Country,
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

  private data class TestSource(val displayName: String)
}
