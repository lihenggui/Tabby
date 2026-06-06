package com.github.kr328.clash.profile.ui

import kotlin.test.Test
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
  fun platformPayloadCreatesSuccessfulQrScanResultWithRawPayload() {
    val result =
      profileQrScanResultFromPlatformPayload(
        kind = ProfileQrResultKind.Success,
        rawValue = "https://example.com/raw-value.yaml",
        rawBytes = "https://example.com/raw-bytes.yaml".encodeToByteArray(),
      )

    assertEquals(ProfileQrResultKind.Success, result.kind)
    assertEquals("https://example.com/raw-value.yaml", result.rawValue)
    assertEquals("https://example.com/raw-bytes.yaml", result.rawBytes?.decodeToString())
  }

  @Test
  fun platformPayloadDropsRawPayloadForNonSuccessfulQrScanResults() {
    val result =
      profileQrScanResultFromPlatformPayload(
        kind = ProfileQrResultKind.Error,
        rawValue = "https://example.com/ignored.yaml",
        rawBytes = "https://example.com/ignored-bytes.yaml".encodeToByteArray(),
      )

    assertEquals(ProfileQrResultKind.Error, result.kind)
    assertEquals(null, result.rawValue)
    assertEquals(null, result.rawBytes)
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
