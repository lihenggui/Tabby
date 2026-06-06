package com.github.kr328.clash.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.github.kr328.clash.ui.icon.BaselineArrowBack
import com.github.kr328.clash.ui.icon.TabbyIcons
import org.jetbrains.compose.resources.stringResource
import tabby.ui.shared.generated.resources.Res
import tabby.ui.shared.generated.resources.close

@Composable
fun TabbyScaffold(
  title: String,
  modifier: Modifier = Modifier,
  onBack: () -> Unit = rememberDefaultBackAction(),
  actions: @Composable RowScope.() -> Unit = {},
  scrollBehavior: TopAppBarScrollBehavior? = null,
  topBar: @Composable () -> Unit = {
    TopAppBar(
      title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
      navigationIcon = {
        IconButton(onClick = onBack) {
          Icon(
            imageVector = TabbyIcons.BaselineArrowBack,
            contentDescription = stringResource(Res.string.close),
          )
        }
      },
      actions = actions,
      scrollBehavior = scrollBehavior,
    )
  },
  snackbarHostState: SnackbarHostState? = null,
  snackbarHost: @Composable () -> Unit = {
    snackbarHostState?.let { SnackbarHost(hostState = it) }
  },
  floatingActionButton: @Composable () -> Unit = {},
  content: @Composable (PaddingValues) -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = topBar,
    snackbarHost = snackbarHost,
    floatingActionButton = floatingActionButton,
    content = content,
  )
}
