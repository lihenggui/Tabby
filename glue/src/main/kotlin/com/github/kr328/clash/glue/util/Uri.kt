package com.github.kr328.clash.glue.util

import android.net.Uri
import com.github.kr328.clash.common.util.tabbyFileNameFromSchemeSpecificPart

val Uri.fileName: String?
  get() = tabbyFileNameFromSchemeSpecificPart(schemeSpecificPart)
