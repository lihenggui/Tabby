package com.github.kr328.clash.common.compat

const val TABBY_SERIALIZABLE_EXTRA_TYPED_API_MIN_SDK = 33

fun tabbySerializableExtraUsesTypedApi(platformSdk: Int): Boolean {
  return platformSdk >= TABBY_SERIALIZABLE_EXTRA_TYPED_API_MIN_SDK
}
