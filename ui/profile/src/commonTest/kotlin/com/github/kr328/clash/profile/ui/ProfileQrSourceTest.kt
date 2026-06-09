package com.github.kr328.clash.profile.ui

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ProfileQrSourceTest {
  @Test
  fun prefersRawValueWhenPresent() {
    assertEquals(
      "https://example.com/raw-value.yaml",
      decodeProfileQrSource(
        rawValue = "https://example.com/raw-value.yaml",
        rawBytes = "https://example.com/raw-bytes.yaml".encodeToByteArray(),
      ),
    )
  }

  @Test
  fun decodesRawBytesWhenRawValueIsMissing() {
    assertEquals(
      "https://example.com/raw-bytes.yaml",
      decodeProfileQrSource(
        rawValue = null,
        rawBytes = "https://example.com/raw-bytes.yaml".encodeToByteArray(),
      ),
    )
  }

  @Test
  fun returnsEmptySourceWhenPayloadIsMissing() {
    assertEquals("", decodeProfileQrSource(rawValue = null, rawBytes = null))
  }

  @Test
  fun qrSuccessCreatesUrlProfileFromRawValue() {
    assertEquals(
      ProfileQrAction.CreateUrlProfile("https://example.com/raw-value.yaml"),
      profileQrAction(
        kind = ProfileQrResultKind.Success,
        rawValue = "https://example.com/raw-value.yaml",
        rawBytes = "https://example.com/raw-bytes.yaml".encodeToByteArray(),
      ),
    )
  }

  @Test
  fun qrSuccessCreatesUrlProfileFromRawBytesWhenRawValueIsMissing() {
    assertEquals(
      ProfileQrAction.CreateUrlProfile("https://example.com/raw-bytes.yaml"),
      profileQrAction(
        kind = ProfileQrResultKind.Success,
        rawValue = null,
        rawBytes = "https://example.com/raw-bytes.yaml".encodeToByteArray(),
      ),
    )
  }

  @Test
  fun qrScanResultCreatesUrlProfileFromRawPayload() {
    assertEquals(
      ProfileQrAction.CreateUrlProfile("https://example.com/scan-result.yaml"),
      profileQrAction(
        ProfileQrScanResult(
          kind = ProfileQrResultKind.Success,
          rawValue = null,
          rawBytes = "https://example.com/scan-result.yaml".encodeToByteArray(),
        )
      ),
    )
  }

  @Test
  fun qrScanResultFromSourceKeepsSuccessPayload() {
    val rawBytes = "https://example.com/source-bytes.yaml".encodeToByteArray()
    val result =
      profileQrScanResultFromSource(
        kind = ProfileQrScanSourceResultKind.Success,
        rawValue = "https://example.com/source-value.yaml",
        rawBytes = rawBytes,
      )

    assertEquals(ProfileQrResultKind.Success, result.kind)
    assertEquals("https://example.com/source-value.yaml", result.rawValue)
    assertContentEquals(rawBytes, result.rawBytes)
  }

  @Test
  fun qrScanResultFromSourceMapsNonSuccessKindsWithoutPayload() {
    val payload = "https://example.com/ignored.yaml".encodeToByteArray()

    listOf(
        ProfileQrScanSourceResultKind.UserCanceled to ProfileQrResultKind.UserCanceled,
        ProfileQrScanSourceResultKind.MissingPermission to ProfileQrResultKind.MissingPermission,
        ProfileQrScanSourceResultKind.Error to ProfileQrResultKind.Error,
      )
      .forEach { (sourceKind, expectedKind) ->
        val result =
          profileQrScanResultFromSource(
            kind = sourceKind,
            rawValue = "https://example.com/ignored.yaml",
            rawBytes = payload,
          )

        assertEquals(expectedKind, result.kind)
        assertEquals(null, result.rawValue)
        assertEquals(null, result.rawBytes)
      }
  }

  @Test
  fun qrUserCanceledIsIgnored() {
    assertEquals(
      ProfileQrAction.Ignore,
      profileQrAction(ProfileQrResultKind.UserCanceled),
    )
  }

  @Test
  fun qrMissingPermissionShowsPermissionMessage() {
    assertEquals(
      ProfileQrAction.ShowMissingPermission,
      profileQrAction(ProfileQrResultKind.MissingPermission),
    )
  }

  @Test
  fun qrErrorShowsScanErrorMessage() {
    assertEquals(
      ProfileQrAction.ShowScanError,
      profileQrAction(ProfileQrResultKind.Error),
    )
  }
}
