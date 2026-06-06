package com.github.kr328.clash.profile.vm

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.engine.android.AndroidProfileRepository
import com.github.kr328.clash.engine.api.ProfileRepository
import com.github.kr328.clash.profile.R
import com.github.kr328.clash.profile.model.ProfileProvider
import com.github.kr328.clash.profile.ui.NewProfileCreateAction
import com.github.kr328.clash.profile.ui.NewProfileDetailAction
import com.github.kr328.clash.profile.ui.NewProfileEventState
import com.github.kr328.clash.profile.ui.NewProfileExternalProviderResultAction
import com.github.kr328.clash.profile.ui.NewProfileUiState
import com.github.kr328.clash.profile.ui.ProfileQrAction
import com.github.kr328.clash.profile.ui.ProfileQrResultKind
import com.github.kr328.clash.profile.ui.newProfileCreateAction
import com.github.kr328.clash.profile.ui.newProfileDetailAction
import com.github.kr328.clash.profile.ui.newProfileErrorEventState
import com.github.kr328.clash.profile.ui.newProfileExternalProviderResultAction
import com.github.kr328.clash.profile.ui.profileQrAction
import com.github.kr328.clash.profile.ui.withNewProfileProviders
import io.github.g00fy2.quickie.QRResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class NewProfileViewModel(app: Application) : AndroidViewModel(app) {
  private val profileRepository: ProfileRepository = AndroidProfileRepository()

  val uiState: StateFlow<NewProfileUiState<ProfileProvider>>
    field = MutableStateFlow(NewProfileUiState())

  val eventState: StateFlow<NewProfileEventState<Intent, Uri>>
    field = MutableStateFlow<NewProfileEventState<Intent, Uri>>(NewProfileEventState.Idle)

  init {
    loadProviders()
  }

  fun consumeEvent() {
    eventState.value = NewProfileEventState.Idle
  }

  fun onCreate(provider: ProfileProvider) {
    when (val action = newProfileCreateAction(provider.kind)) {
      is NewProfileCreateAction.CreateProfile -> createProfile(action.type)
      NewProfileCreateAction.LaunchQRScanner ->
        eventState.value = NewProfileEventState.LaunchQRScanner
      NewProfileCreateAction.LaunchExternalProvider -> {
        val externalProvider = provider as ProfileProvider.External
        eventState.value = NewProfileEventState.LaunchExternalProvider(externalProvider.intent)
      }
    }
  }

  fun onDetail(provider: ProfileProvider.External) {
    when (val action = newProfileDetailAction(provider.intent.component?.packageName)) {
      is NewProfileDetailAction.OpenAppSettings -> {
        val uri = Uri.fromParts("package", action.packageName, null)
        eventState.value = NewProfileEventState.OpenAppSettings(uri)
      }
      NewProfileDetailAction.Ignore -> Unit
    }
  }

  fun onExternalProviderResult(uri: Uri, name: String?) {
    when (
      val action =
        newProfileExternalProviderResultAction(
          resultAccepted = true,
          sourceSelected = true,
          name = name,
        )
    ) {
      is NewProfileExternalProviderResultAction.CreateProfile -> {
        viewModelScope.launch {
          try {
            val profileName = application.getString(CommonR.string.new_profile)
            val uuid =
              profileRepository.create(External, action.name ?: profileName, uri.toString())
            eventState.value = NewProfileEventState.LaunchProperties(uuid)
          } catch (e: Exception) {
            Log.e("Create external profile failed: ${e.message}", e)
            eventState.value =
              newProfileErrorEventState(e.message, application.getString(CommonR.string.unknown))
          }
        }
      }
      NewProfileExternalProviderResultAction.Ignore -> Unit
    }
  }

  fun onQRResult(result: QRResult) {
    val action =
      when (result) {
        is QRSuccess ->
          profileQrAction(
            kind = ProfileQrResultKind.Success,
            rawValue = result.content.rawValue,
            rawBytes = result.content.rawBytes,
          )
        QRUserCanceled -> profileQrAction(ProfileQrResultKind.UserCanceled)
        QRMissingPermission -> profileQrAction(ProfileQrResultKind.MissingPermission)
        is QRError -> profileQrAction(ProfileQrResultKind.Error)
      }

    when (action) {
      is ProfileQrAction.CreateUrlProfile -> {
        viewModelScope.launch {
          try {
            val uuid =
              profileRepository.create(
                type = Url,
                name = application.getString(CommonR.string.new_profile),
                source = action.source,
              )
            eventState.value = NewProfileEventState.LaunchProperties(uuid)
          } catch (e: Exception) {
            Log.e("Create QR profile failed: ${e.message}", e)
            eventState.value =
              newProfileErrorEventState(e.message, application.getString(CommonR.string.unknown))
          }
        }
      }
      ProfileQrAction.Ignore -> Unit
      ProfileQrAction.ShowMissingPermission ->
        eventState.value =
          NewProfileEventState.ShowMessage(
            application.getString(R.string.import_from_qr_no_permission)
          )
      ProfileQrAction.ShowScanError ->
        eventState.value =
          NewProfileEventState.ShowMessage(application.getString(R.string.import_from_qr_exception))
    }
  }

  private fun createProfile(type: Profile.Type) {
    viewModelScope.launch {
      try {
        val name = application.getString(CommonR.string.new_profile)
        val uuid = profileRepository.create(type, name)
        eventState.value = NewProfileEventState.LaunchProperties(uuid)
      } catch (e: Exception) {
        Log.e("Create profile failed: ${e.message}", e)
        eventState.value =
          newProfileErrorEventState(e.message, application.getString(CommonR.string.unknown))
      }
    }
  }

  private fun loadProviders() {
    viewModelScope.launch {
      val providers =
        withContext(Dispatchers.IO) {
          val externalProviders =
            application.packageManager
              .queryIntentActivities(Intent(Intents.ACTION_PROVIDE_URL), 0)
              .map {
                val activity = it.activityInfo
                val name = activity.applicationInfo.loadLabel(application.packageManager)
                val summary = activity.loadLabel(application.packageManager)
                val icon = activity.loadIcon(application.packageManager)
                val intent =
                  Intent(Intents.ACTION_PROVIDE_URL)
                    .setComponent(ComponentName(activity.packageName, activity.name))
                ProfileProvider.External(name.toString(), summary.toString(), icon, intent)
              }

          listOf(
            ProfileProvider.File(application),
            ProfileProvider.Url(application),
            ProfileProvider.QR(application),
          ) + externalProviders
        }
      uiState.update { it.withNewProfileProviders(providers) }
    }
  }
}
