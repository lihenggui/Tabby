package com.github.kr328.clash.profile.model

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.kr328.clash.common.R as CommonR
import com.github.kr328.clash.profile.R
import com.github.kr328.clash.profile.ui.NewProfileProviderKind
import com.github.kr328.clash.ui.icon.BaselineAttachFile
import com.github.kr328.clash.ui.icon.BaselineCloudDownload
import com.github.kr328.clash.ui.icon.BaselineQrCodeScanner
import com.github.kr328.clash.ui.icon.TabbyIcons

internal sealed class ProfileProvider {
  class File(private val context: Context) : ProfileProvider() {
    override val kind: NewProfileProviderKind = NewProfileProviderKind.File

    override val name: String
      get() = context.getString(CommonR.string.file)

    override val summary: String
      get() = context.getString(R.string.import_from_file)

    override val icon: ImageVector = TabbyIcons.BaselineAttachFile
  }

  class Url(private val context: Context) : ProfileProvider() {
    override val kind: NewProfileProviderKind = NewProfileProviderKind.Url

    override val name: String
      get() = context.getString(CommonR.string.url)

    override val summary: String
      get() = context.getString(R.string.import_from_url)

    override val icon: ImageVector = TabbyIcons.BaselineCloudDownload
  }

  class QR(private val context: Context) : ProfileProvider() {
    override val kind: NewProfileProviderKind = NewProfileProviderKind.QR

    override val name: String
      get() = context.getString(R.string.qr)

    override val summary: String
      get() = context.getString(R.string.import_from_qr)

    override val icon: ImageVector = TabbyIcons.BaselineQrCodeScanner
  }

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
}
