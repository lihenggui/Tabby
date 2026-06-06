package com.github.kr328.clash.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

@Composable
inline fun rememberNavBackStackBuilder(
  builderAction: MutableList<NavKey>.() -> Unit
): NavBackStack<NavKey> = rememberNavBackStack(*buildList(builderAction).toTypedArray())
