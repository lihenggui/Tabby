package com.github.kr328.clash.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable internal actual fun rememberDefaultBackAction(): () -> Unit = remember { {} }
