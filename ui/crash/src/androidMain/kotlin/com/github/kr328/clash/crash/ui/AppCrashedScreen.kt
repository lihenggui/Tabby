package com.github.kr328.clash.crash.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.kr328.clash.crash.vm.AppCrashedViewModel

@Composable
internal fun AppCrashedScreen(
  modifier: Modifier = Modifier,
  viewModel: AppCrashedViewModel = viewModel(),
) {
  val logs by viewModel.logs.collectAsStateWithLifecycle()

  AppCrashedContent(modifier = modifier, logs = logs)
}
