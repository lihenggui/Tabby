package com.github.kr328.clash.app

import com.github.kr328.clash.core.model.Profile

data class TabbyInstallProfileRequest(
  val type: Profile.Type,
  val name: String,
  val source: String,
)

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
