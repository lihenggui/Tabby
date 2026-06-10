package com.github.kr328.clash.service

import android.content.Context
import android.content.SharedPreferences
import com.github.kr328.clash.common.constants.Authorities
import com.github.kr328.clash.common.service.TabbyServicePreferenceBackend
import com.github.kr328.clash.common.service.tabbyServicePreferenceBackend
import rikka.preference.MultiProcessPreference
import rikka.preference.PreferenceProvider

class PreferenceProvider : PreferenceProvider() {
  override fun onCreatePreference(context: Context): SharedPreferences {
    return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
  }

  companion object {
    private const val FILE_NAME = "service"

    fun createSharedPreferencesFromContext(context: Context): SharedPreferences {
      return when (
        tabbyServicePreferenceBackend(
          localServiceContext = context is BaseService || context is TunService
        )
      ) {
        TabbyServicePreferenceBackend.Direct ->
          context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        TabbyServicePreferenceBackend.MultiProcess ->
          MultiProcessPreference(context, Authorities.SETTINGS_PROVIDER)
      }
    }
  }
}
