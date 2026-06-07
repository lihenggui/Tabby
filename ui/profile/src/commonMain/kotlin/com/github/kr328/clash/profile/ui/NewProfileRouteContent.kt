package com.github.kr328.clash.profile.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.ui.icon.BaselineAttachFile
import com.github.kr328.clash.ui.icon.BaselineCloudDownload
import com.github.kr328.clash.ui.icon.BaselineQrCodeScanner
import com.github.kr328.clash.ui.icon.TabbyIcons
import org.jetbrains.compose.resources.stringResource
import tabby.ui.profile.generated.resources.Res as ProfileRes
import tabby.ui.profile.generated.resources.import_from_file
import tabby.ui.profile.generated.resources.import_from_qr
import tabby.ui.profile.generated.resources.import_from_url
import tabby.ui.profile.generated.resources.qr
import tabby.ui.shared.generated.resources.Res as SharedRes
import tabby.ui.shared.generated.resources.file
import tabby.ui.shared.generated.resources.url

enum class NewProfileRouteBuiltInProvider {
  File,
  Url,
  QR,
}

sealed interface NewProfileRouteCreateAction {
  data class CreateProfile(val type: Profile.Type) : NewProfileRouteCreateAction

  data object LaunchQrScanner : NewProfileRouteCreateAction
}

data class NewProfileRouteExternalProvider(
  val key: String,
  val name: String,
  val summary: String,
  val iconPainter: Painter? = null,
  val hasDetail: Boolean = true,
)

@Composable
fun NewProfileRouteContent(
  modifier: Modifier = Modifier,
  externalProviders: List<NewProfileRouteExternalProvider> = emptyList(),
  onCreateBuiltIn: (NewProfileRouteBuiltInProvider) -> Unit = {},
  onCreateExternal: (NewProfileRouteExternalProvider) -> Unit = {},
  onDetailExternal: (NewProfileRouteExternalProvider) -> Unit = {},
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val providers =
    newProfileRouteBuiltInProviders().map(NewProfileRouteProvider::BuiltIn) +
      externalProviders.map(NewProfileRouteProvider::External)

  NewProfileContent(
    modifier = modifier,
    snackbarHostState = snackbarHostState,
    providers = providers.map { it.toNewProfileProviderItem() },
    onCreate = { index ->
      when (val provider = providers.getOrNull(index)) {
        is NewProfileRouteProvider.BuiltIn -> onCreateBuiltIn(provider.provider)
        is NewProfileRouteProvider.External -> onCreateExternal(provider.provider)
        null -> Unit
      }
    },
    onDetail = { index ->
      when (val provider = providers.getOrNull(index)) {
        is NewProfileRouteProvider.BuiltIn -> Unit
        is NewProfileRouteProvider.External -> onDetailExternal(provider.provider)
        null -> Unit
      }
    },
  )
}

fun newProfileRouteCreateAction(
  provider: NewProfileRouteBuiltInProvider
): NewProfileRouteCreateAction {
  return when (provider) {
    NewProfileRouteBuiltInProvider.File ->
      NewProfileRouteCreateAction.CreateProfile(Profile.Type.File)
    NewProfileRouteBuiltInProvider.Url ->
      NewProfileRouteCreateAction.CreateProfile(Profile.Type.Url)
    NewProfileRouteBuiltInProvider.QR -> NewProfileRouteCreateAction.LaunchQrScanner
  }
}

private sealed interface NewProfileRouteProvider {
  data class BuiltIn(val provider: NewProfileRouteBuiltInProvider) : NewProfileRouteProvider

  data class External(val provider: NewProfileRouteExternalProvider) : NewProfileRouteProvider
}

private fun newProfileRouteBuiltInProviders(): List<NewProfileRouteBuiltInProvider> {
  return listOf(
    NewProfileRouteBuiltInProvider.File,
    NewProfileRouteBuiltInProvider.Url,
    NewProfileRouteBuiltInProvider.QR,
  )
}

@Composable
private fun NewProfileRouteProvider.toNewProfileProviderItem(): NewProfileProviderItem {
  return when (this) {
    is NewProfileRouteProvider.BuiltIn -> provider.toNewProfileProviderItem()
    is NewProfileRouteProvider.External ->
      NewProfileProviderItem(
        name = provider.name,
        summary = provider.summary,
        iconPainter = provider.iconPainter,
        hasDetail = provider.hasDetail,
      )
  }
}

@Composable
private fun NewProfileRouteBuiltInProvider.toNewProfileProviderItem(): NewProfileProviderItem {
  val presentation = checkNotNull(newProfileBuiltInProviderPresentation(toProviderKind()))

  return NewProfileProviderItem(
    name = presentation.nameToken.text(),
    summary = presentation.summaryToken.text(),
    iconPainter = presentation.graphicToken.iconPainter(),
    hasDetail = false,
  )
}

private fun NewProfileRouteBuiltInProvider.toProviderKind(): NewProfileProviderKind {
  return when (this) {
    NewProfileRouteBuiltInProvider.File -> NewProfileProviderKind.File
    NewProfileRouteBuiltInProvider.Url -> NewProfileProviderKind.Url
    NewProfileRouteBuiltInProvider.QR -> NewProfileProviderKind.QR
  }
}

@Composable
private fun NewProfileProviderTextToken.text(): String {
  return stringResource(
    newProfileProviderTextPlatformToken(
      token = this,
      file = SharedRes.string.file,
      url = SharedRes.string.url,
      qr = ProfileRes.string.qr,
      importFromFile = ProfileRes.string.import_from_file,
      importFromUrl = ProfileRes.string.import_from_url,
      importFromQr = ProfileRes.string.import_from_qr,
    )
  )
}

@Composable
private fun NewProfileProviderGraphicToken.iconPainter(): Painter {
  return rememberVectorPainter(
    newProfileProviderGraphicPlatformToken(
      token = this,
      file = TabbyIcons.BaselineAttachFile,
      url = TabbyIcons.BaselineCloudDownload,
      qr = TabbyIcons.BaselineQrCodeScanner,
    )
  )
}
