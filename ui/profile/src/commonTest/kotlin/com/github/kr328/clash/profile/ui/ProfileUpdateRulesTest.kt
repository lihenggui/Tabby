package com.github.kr328.clash.profile.ui

import androidx.compose.material3.SnackbarResult
import com.github.kr328.clash.core.model.Profile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class ProfileUpdateRulesTest {
  @Test
  fun allowsImportedProfilesToActivate() {
    assertEquals(
      ProfileActivationAction.Activate,
      profileActivationAction(profile(type = Profile.Type.Url, imported = true)),
    )
    assertEquals(
      ProfileActivationAction.Activate,
      profileActivationAction(profile(type = Profile.Type.File, imported = true)),
    )
  }

  @Test
  fun requiresSavingProfilesBeforeActivation() {
    assertEquals(
      ProfileActivationAction.RequireSave,
      profileActivationAction(profile(type = Profile.Type.Url, imported = false)),
    )
    assertEquals(
      ProfileActivationAction.RequireSave,
      profileActivationAction(profile(type = Profile.Type.External, imported = false)),
    )
  }

  @Test
  fun activationEventStateIsOnlyCreatedForUnsavedProfiles() {
    val uuid = Uuid.parse("00000000-0000-0000-0000-000000000006")

    assertEquals(
      null,
      profileActivationEventState(
        action = ProfileActivationAction.Activate,
        profileUuid = uuid,
        requireSaveMessage = "save first",
      ),
    )
    assertEquals(
      ProfilesEventState.ShowEditableMessage("save first", uuid),
      profileActivationEventState(
        action = ProfileActivationAction.RequireSave,
        profileUuid = uuid,
        requireSaveMessage = "save first",
      ),
    )
  }

  @Test
  fun profileListOpenEventStatesCarryNavigationTargets() {
    val uuid = Uuid.parse("00000000-0000-0000-0000-000000000007")

    assertEquals(ProfilesEventState.OpenCreate, profilesOpenCreateEventState())
    assertEquals(ProfilesEventState.OpenEdit(uuid), profilesOpenEditEventState(uuid))
  }

  @Test
  fun profileEventRouteEffectMapsEventStates() {
    val uuid = Uuid.parse("00000000-0000-0000-0000-000000000007")

    assertEquals(
      ProfilesEventRouteEffect.Ignore,
      profilesEventRouteEffect(ProfilesEventState.Idle),
    )
    assertEquals(
      ProfilesEventRouteEffect.OpenCreate,
      profilesEventRouteEffect(ProfilesEventState.OpenCreate),
    )
    assertEquals(
      ProfilesEventRouteEffect.OpenEdit(uuid),
      profilesEventRouteEffect(ProfilesEventState.OpenEdit(uuid)),
    )
    assertEquals(
      ProfilesEventRouteEffect.ShowMessage("Profile updated"),
      profilesEventRouteEffect(ProfilesEventState.ShowMessage("Profile updated")),
    )
    assertEquals(
      ProfilesEventRouteEffect.ShowEditableMessage("Profile update failed", uuid),
      profilesEventRouteEffect(
        ProfilesEventState.ShowEditableMessage("Profile update failed", uuid)
      ),
    )
  }

  @Test
  fun profileUpdateResultEventStatesCarryMessagesAndEditTargets() {
    val uuid = Uuid.parse("00000000-0000-0000-0000-000000000008")

    assertEquals(
      ProfilesEventState.ShowMessage("Profile updated"),
      profileUpdateCompletedEventState("Profile updated"),
    )
    assertEquals(
      ProfilesEventState.ShowEditableMessage("Profile update failed", uuid),
      profileUpdateFailedEventState("Profile update failed", uuid),
    )
  }

  @Test
  fun consumedEventStateResetsToIdle() {
    assertEquals(ProfilesEventState.Idle, profilesConsumedEventState())
  }

  @Test
  fun editableSnackbarActionOpensEditOnlyWhenActionIsPerformed() {
    assertEquals(
      ProfilesEditableSnackbarAction.OpenEdit,
      profilesEditableSnackbarAction(ProfileSnackbarActionResult.ActionPerformed),
    )
    assertEquals(
      ProfilesEditableSnackbarAction.Ignore,
      profilesEditableSnackbarAction(ProfileSnackbarActionResult.Dismissed),
    )
  }

  @Test
  fun snackbarResultMapsToProfileSnackbarActionResult() {
    assertEquals(
      ProfileSnackbarActionResult.ActionPerformed,
      SnackbarResult.ActionPerformed.toProfileSnackbarActionResult(),
    )
    assertEquals(
      ProfileSnackbarActionResult.Dismissed,
      SnackbarResult.Dismissed.toProfileSnackbarActionResult(),
    )
  }

  @Test
  fun updateAllActionQueriesProfilesOnlyWhenNotAlreadyUpdating() {
    assertEquals(
      ProfileUpdateAllAction.QueryProfiles,
      profileUpdateAllAction(ProfilesUiState(allUpdating = false)),
    )
    assertEquals(
      ProfileUpdateAllAction.Ignore,
      profileUpdateAllAction(ProfilesUiState(allUpdating = true)),
    )
  }

  @Test
  fun updateAllActionCanBeSelectedFromPublicUpdatingFlag() {
    assertEquals(
      ProfileUpdateAllAction.QueryProfiles,
      profileUpdateAllAction(allUpdating = false),
    )
    assertEquals(
      ProfileUpdateAllAction.Ignore,
      profileUpdateAllAction(allUpdating = true),
    )
  }

  @Test
  fun marksImportedNonFileProfilesAsUpdatable() {
    assertTrue(isProfileUpdatable(profile(type = Profile.Type.Url, imported = true)))
    assertTrue(isProfileUpdatable(profile(type = Profile.Type.External, imported = true)))
  }

  @Test
  fun excludesFileProfilesAndUnsavedProfilesFromUpdates() {
    assertFalse(isProfileUpdatable(profile(type = Profile.Type.File, imported = true)))
    assertFalse(isProfileUpdatable(profile(type = Profile.Type.Url, imported = false)))
    assertFalse(isProfileUpdatable(profile(type = Profile.Type.External, imported = false)))
  }

  @Test
  fun detectsWhetherAListContainsUpdatableProfiles() {
    assertFalse(
      hasUpdatableProfiles(
        listOf(
          profile(type = Profile.Type.File, imported = true),
          profile(type = Profile.Type.Url, imported = false),
        )
      )
    )
    assertTrue(
      hasUpdatableProfiles(
        listOf(
          profile(type = Profile.Type.File, imported = true),
          profile(type = Profile.Type.Url, imported = true),
        )
      )
    )
  }

  @Test
  fun filtersProfilesThatShouldBeUpdated() {
    val url = profile(type = Profile.Type.Url, imported = true)
    val external = profile(type = Profile.Type.External, imported = true)
    val file = profile(type = Profile.Type.File, imported = true)
    val pendingUrl = profile(type = Profile.Type.Url, imported = false)

    assertEquals(
      listOf(url, external),
      filterUpdatableProfiles(listOf(file, url, pendingUrl, external)),
    )
  }

  @Test
  fun selectsUpdateAllTargetUuidsFromUpdatableProfiles() {
    val urlUuid = Uuid.parse("00000000-0000-0000-0000-000000000001")
    val externalUuid = Uuid.parse("00000000-0000-0000-0000-000000000002")
    val fileUuid = Uuid.parse("00000000-0000-0000-0000-000000000003")
    val pendingUuid = Uuid.parse("00000000-0000-0000-0000-000000000004")

    assertEquals(
      listOf(urlUuid, externalUuid),
      profileUpdateAllTargets(
        listOf(
          profile(type = Profile.Type.File, imported = true, uuid = fileUuid),
          profile(type = Profile.Type.Url, imported = true, uuid = urlUuid),
          profile(type = Profile.Type.Url, imported = false, uuid = pendingUuid),
          profile(type = Profile.Type.External, imported = true, uuid = externalUuid),
        )
      ),
    )
  }

  @Test
  fun updateFailureReasonUsesProvidedReasonWhenNotBlank() {
    assertEquals(
      "network unavailable",
      profileUpdateFailureReasonText(reason = "network unavailable", unknownText = "Unknown"),
    )
  }

  @Test
  fun updateFailureReasonFallsBackToUnknownTextWhenMissingOrBlank() {
    assertEquals("Unknown", profileUpdateFailureReasonText(reason = null, unknownText = "Unknown"))
    assertEquals("Unknown", profileUpdateFailureReasonText(reason = "", unknownText = "Unknown"))
    assertEquals("Unknown", profileUpdateFailureReasonText(reason = "  ", unknownText = "Unknown"))
  }

  private fun profile(
    type: Profile.Type,
    imported: Boolean,
    uuid: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000001"),
  ): Profile {
    return Profile(
      uuid = uuid,
      name = "Profile",
      type = type,
      source = "",
      active = false,
      interval = 0,
      upload = 0,
      download = 0,
      total = 0,
      expire = 0,
      updatedAt = 0,
      imported = imported,
      pending = !imported,
    )
  }
}
