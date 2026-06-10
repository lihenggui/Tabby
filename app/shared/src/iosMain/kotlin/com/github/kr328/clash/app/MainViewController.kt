package com.github.kr328.clash.app

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
  PlaceholderTabbyApp(engineEnvironment = iosEngineEnvironment())
}
