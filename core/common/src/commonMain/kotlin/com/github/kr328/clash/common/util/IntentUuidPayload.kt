package com.github.kr328.clash.common.util

import kotlin.uuid.Uuid

const val TABBY_UUID_URI_SCHEME = "uuid"

fun tabbyUuidFromUriPayload(scheme: String?, schemeSpecificPart: String?): Uuid? {
  if (scheme != TABBY_UUID_URI_SCHEME) {
    return null
  }

  return schemeSpecificPart?.let(Uuid::parse)
}

fun tabbyUuidUriSchemeSpecificPart(uuid: Uuid): String = uuid.toString()
