package com.github.kr328.clash.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.github.kr328.clash.ui.nav.TabbyNavDisplay
import com.github.kr328.clash.ui.nav.addIfNotLast
import kotlin.uuid.Uuid

fun EntryProviderScope<NavKey>.profilesEntries(
  profilesContent: @Composable (ProfilesRoute.Profiles) -> Unit,
  providersContent: @Composable () -> Unit,
) {
  entry<ProfilesRoute.Profiles> { key -> profilesContent(key) }
  entry<ProfilesRoute.Providers> { providersContent() }
}

@Composable
fun ProfilesRouteContent(
  route: ProfilesRoute.Profiles,
  profilesContent: @Composable (onOpenCreate: () -> Unit, onOpenEdit: (Uuid) -> Unit) -> Unit,
  newProfileContent: @Composable (onProperties: (Uuid) -> Unit, onFinish: () -> Unit) -> Unit,
  propertiesContent:
    @Composable
    (uuid: Uuid, onBrowseFiles: (Uuid) -> Unit, onFinish: (success: Boolean) -> Unit) -> Unit,
  filesContent: @Composable (uuid: Uuid, onFinish: () -> Unit) -> Unit,
) {
  val backStack =
    remember(route.openPropertyUuid) {
      mutableStateListOf<NavKey>(ProfilesRoute.Profiles()).apply {
        when {
          route.openPropertyUuid != null -> add(ProfilesRoute.Properties(route.openPropertyUuid))
        }
      }
    }

  TabbyNavDisplay(
    backStack = backStack,
    entryProvider =
      entryProvider<NavKey> {
        entry<ProfilesRoute.Profiles> {
          profilesContent(
            { backStack.addIfNotLast(ProfilesRoute.NewProfile) },
            { uuid -> backStack.addIfNotLast(ProfilesRoute.Properties(uuid)) },
          )
        }
        entry<ProfilesRoute.NewProfile> {
          newProfileContent(
            { uuid -> backStack.addIfNotLast(ProfilesRoute.Properties(uuid)) },
            { backStack.removeLastOrNull() },
          )
        }
        entry<ProfilesRoute.Properties> { key ->
          propertiesContent(
            key.uuid,
            { uuid -> backStack.addIfNotLast(ProfilesRoute.Files(uuid)) },
            { success ->
              backStack.removeLastOrNull()
              if (success && backStack.lastOrNull() is ProfilesRoute.NewProfile) {
                backStack.removeLastOrNull()
              }
            },
          )
        }
        entry<ProfilesRoute.Files> { key ->
          filesContent(key.uuid) { backStack.removeLastOrNull() }
        }
      },
  )
}
