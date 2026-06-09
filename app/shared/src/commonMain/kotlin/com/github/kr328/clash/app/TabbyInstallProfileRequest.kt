package com.github.kr328.clash.app

import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.engine.api.ProfileRepository
import kotlin.uuid.Uuid

data class TabbyInstallProfileRequest(
  val type: Profile.Type,
  val name: String,
  val source: String,
)

private const val INSTALL_PROFILE_SOURCE_QUERY_PARAMETER = "url"
private const val INSTALL_PROFILE_TYPE_QUERY_PARAMETER = "type"
private const val INSTALL_PROFILE_NAME_QUERY_PARAMETER = "name"

sealed interface TabbyInstallProfileResultAction {
  data class OpenRoute(val routeAction: TabbyExternalRouteAction) : TabbyInstallProfileResultAction
}

fun tabbyInstallProfileRequest(
  source: String?,
  type: String?,
  name: String?,
  defaultName: String,
): TabbyInstallProfileRequest? {
  val profileSource = source ?: return null
  return TabbyInstallProfileRequest(
    type =
      when (type?.lowercase()) {
        "url" -> Profile.Type.Url
        "file" -> Profile.Type.File
        else -> Profile.Type.Url
      },
    name = name ?: defaultName,
    source = profileSource,
  )
}

fun <T> tabbyInstallProfileRequestFromPlatformPayload(
  payload: T,
  queryParameter: (T, String) -> String?,
  defaultName: String,
): TabbyInstallProfileRequest? {
  return tabbyInstallProfileRequest(
    source = queryParameter(payload, INSTALL_PROFILE_SOURCE_QUERY_PARAMETER),
    type = queryParameter(payload, INSTALL_PROFILE_TYPE_QUERY_PARAMETER),
    name = queryParameter(payload, INSTALL_PROFILE_NAME_QUERY_PARAMETER),
    defaultName = defaultName,
  )
}

suspend fun tabbyInstallProfile(
  profileRepository: ProfileRepository,
  request: TabbyInstallProfileRequest,
): Uuid {
  val uuid = profileRepository.create(type = request.type, name = request.name)

  profileRepository.patch(
    uuid = uuid,
    name = request.name,
    source = request.source,
    interval = 0,
  )

  return uuid
}

fun tabbyInstallProfileResultAction(uuid: Uuid): TabbyInstallProfileResultAction =
  TabbyInstallProfileResultAction.OpenRoute(TabbyExternalRouteAction.OpenProfileProperties(uuid))
