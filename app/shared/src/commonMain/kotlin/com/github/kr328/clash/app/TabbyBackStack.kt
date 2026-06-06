package com.github.kr328.clash.app

import androidx.compose.runtime.mutableStateListOf
import androidx.navigation3.runtime.NavKey
import com.github.kr328.clash.home.HomeRoute

fun tabbyInitialBackStack(): MutableList<NavKey> = mutableStateListOf(HomeRoute.Home)
