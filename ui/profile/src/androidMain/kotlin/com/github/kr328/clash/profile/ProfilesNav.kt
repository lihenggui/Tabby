package com.github.kr328.clash.profile

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.profile.ui.FilesScreen
import com.github.kr328.clash.profile.ui.NewProfileScreen
import com.github.kr328.clash.profile.ui.ProfilesScreen
import com.github.kr328.clash.profile.ui.PropertiesScreen
import com.github.kr328.clash.profile.ui.ProvidersScreen

fun EntryProviderScope<NavKey>.profilesEntries() {
  profilesEntries(
    profilesContent = { key ->
      ProfilesRouteContent(
        route = key,
        profilesContent = { onOpenCreate, onOpenEdit ->
          ProfilesScreen(onOpenCreate = onOpenCreate, onOpenEdit = onOpenEdit)
        },
        newProfileContent = { onProperties, _ ->
          NewProfileScreen(onProperties = onProperties)
        },
        propertiesContent = { uuid, onBrowseFiles, onFinish ->
          PropertiesScreen(uuid = uuid, onBrowseFiles = onBrowseFiles, onFinish = onFinish)
        },
        filesContent = { uuid, onFinish -> FilesScreen(uuid = uuid, onFinish = onFinish) },
      )
    },
    providersContent = { ProvidersScreen() },
  )
}
