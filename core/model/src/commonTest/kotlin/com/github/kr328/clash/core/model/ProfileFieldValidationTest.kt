package com.github.kr328.clash.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileFieldValidationTest {
  @Test
  fun profileFieldValidationAcceptsFileWithoutSourceAndUrlWithSupportedSource() {
    assertEquals(
      null,
      profileFieldValidationError(
        type = Profile.Type.File,
        name = "Local",
        sourceMissing = true,
        sourceSupported = true,
        interval = 0,
      ),
    )
    assertEquals(
      null,
      profileFieldValidationError(
        type = Profile.Type.Url,
        name = "Remote",
        sourceMissing = false,
        sourceSupported = true,
        interval = 900_000,
      ),
    )
  }

  @Test
  fun profileFieldValidationRejectsEmptyNameBeforeOtherInvalidFields() {
    assertEquals(
      ProfileFieldValidationError.EmptyName,
      profileFieldValidationError(
        type = Profile.Type.Url,
        name = " ",
        sourceMissing = true,
        sourceSupported = false,
        interval = 1,
      ),
    )
  }

  @Test
  fun profileFieldValidationRejectsMissingSourceForNonFileProfiles() {
    assertEquals(
      ProfileFieldValidationError.MissingSource,
      profileFieldValidationError(
        type = Profile.Type.Url,
        name = "Remote",
        sourceMissing = true,
        sourceSupported = true,
        interval = 0,
      ),
    )
    assertEquals(
      ProfileFieldValidationError.MissingSource,
      profileFieldValidationError(
        type = Profile.Type.External,
        name = "External",
        sourceMissing = true,
        sourceSupported = true,
        interval = 0,
      ),
    )
  }

  @Test
  fun profileFieldValidationRejectsUnsupportedSourceBeforeInvalidInterval() {
    assertEquals(
      ProfileFieldValidationError.UnsupportedSource,
      profileFieldValidationError(
        type = Profile.Type.Url,
        name = "Remote",
        sourceMissing = false,
        sourceSupported = false,
        interval = 1,
      ),
    )
  }

  @Test
  fun profileFieldValidationRejectsInvalidIntervalAfterValidNameAndSource() {
    assertEquals(
      ProfileFieldValidationError.InvalidInterval,
      profileFieldValidationError(
        type = Profile.Type.Url,
        name = "Remote",
        sourceMissing = false,
        sourceSupported = true,
        interval = 899_999,
      ),
    )
  }
}
