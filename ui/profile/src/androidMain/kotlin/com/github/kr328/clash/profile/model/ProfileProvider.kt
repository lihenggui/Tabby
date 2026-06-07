package com.github.kr328.clash.profile.model

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.profile.R
import com.github.kr328.clash.profile.ui.NewProfileProviderGraphicToken
import com.github.kr328.clash.profile.ui.NewProfileProviderKind
import com.github.kr328.clash.profile.ui.NewProfileProviderTextToken
import com.github.kr328.clash.profile.ui.newProfileBuiltInProviderPresentation
import com.github.kr328.clash.profile.ui.newProfileProviderGraphicPlatformToken
import com.github.kr328.clash.profile.ui.newProfileProviderTextPlatformToken
import com.github.kr328.clash.ui.icon.BaselineAttachFile
import com.github.kr328.clash.ui.icon.BaselineCloudDownload
import com.github.kr328.clash.ui.icon.BaselineQrCodeScanner
import com.github.kr328.clash.ui.icon.TabbyIcons

internal sealed class ProfileProvider {
  class File(context: Context) : BuiltIn(context, NewProfileProviderKind.File)

  class Url(context: Context) : BuiltIn(context, NewProfileProviderKind.Url)

  class QR(context: Context) : BuiltIn(context, NewProfileProviderKind.QR)

  class External(
    override val name: String,
    override val summary: String,
    override val icon: Any?,
    val intent: Intent,
  ) : ProfileProvider() {
    override val kind: NewProfileProviderKind = NewProfileProviderKind.External
  }

  abstract val kind: NewProfileProviderKind
  abstract val name: String
  abstract val summary: String
  abstract val icon: Any?

  abstract class BuiltIn(
    private val context: Context,
    final override val kind: NewProfileProviderKind,
  ) : ProfileProvider() {
    private val presentation = checkNotNull(newProfileBuiltInProviderPresentation(kind))

    override val name: String
      get() = presentation.nameToken.androidString(context)

    override val summary: String
      get() = presentation.summaryToken.androidString(context)

    override val icon: ImageVector
      get() = presentation.graphicToken.androidIcon()
  }
}

private fun NewProfileProviderTextToken.androidString(context: Context): String {
  return context.getString(
    newProfileProviderTextPlatformToken(
      token = this,
      file = CommonR.string.file,
      url = CommonR.string.url,
      qr = R.string.qr,
      importFromFile = R.string.import_from_file,
      importFromUrl = R.string.import_from_url,
      importFromQr = R.string.import_from_qr,
    )
  )
}

private fun NewProfileProviderGraphicToken.androidIcon(): ImageVector {
  return newProfileProviderGraphicPlatformToken(
    token = this,
    file = TabbyIcons.BaselineAttachFile,
    url = TabbyIcons.BaselineCloudDownload,
    qr = TabbyIcons.BaselineQrCodeScanner,
  )
}
