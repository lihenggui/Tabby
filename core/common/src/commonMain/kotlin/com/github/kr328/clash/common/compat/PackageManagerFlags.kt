package com.github.kr328.clash.common.compat

const val TABBY_PACKAGE_MANAGER_TYPED_FLAGS_MIN_SDK = 33

fun tabbyPackageManagerUsesTypedFlags(platformSdk: Int): Boolean {
  return platformSdk >= TABBY_PACKAGE_MANAGER_TYPED_FLAGS_MIN_SDK
}
