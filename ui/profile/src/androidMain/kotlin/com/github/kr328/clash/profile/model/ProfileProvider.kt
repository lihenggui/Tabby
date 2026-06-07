package com.github.kr328.clash.profile.model

import android.content.Intent

internal sealed interface ProfileProvider {
  class External(
    val name: String,
    val summary: String,
    val icon: Any?,
    val intent: Intent,
  ) : ProfileProvider
}
