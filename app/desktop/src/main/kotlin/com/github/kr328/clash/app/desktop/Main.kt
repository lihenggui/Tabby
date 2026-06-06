package com.github.kr328.clash.app.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.kr328.clash.app.PlaceholderTabbyApp
import com.github.kr328.clash.app.desktopEngineEnvironment

fun main() = application {
  val engineEnvironment = desktopEngineEnvironment()
  Window(onCloseRequest = ::exitApplication, title = "Tabby") {
    PlaceholderTabbyApp(engineEnvironment = engineEnvironment)
  }
}
