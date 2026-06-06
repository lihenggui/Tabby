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
