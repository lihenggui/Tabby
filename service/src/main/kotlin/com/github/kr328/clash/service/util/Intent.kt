package com.github.kr328.clash.service.util

import android.content.Intent
import com.github.kr328.clash.common.util.tabbyPackageNameFromUriPayload

val Intent.packageName: String?
  get() {
    return tabbyPackageNameFromUriPayload(
      scheme = data?.scheme,
      schemeSpecificPart = data?.schemeSpecificPart,
    )
  }
