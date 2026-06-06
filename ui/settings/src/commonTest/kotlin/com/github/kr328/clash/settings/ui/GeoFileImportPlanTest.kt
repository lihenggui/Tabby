package com.github.kr328.clash.settings.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class GeoFileImportPlanTest {
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
}
