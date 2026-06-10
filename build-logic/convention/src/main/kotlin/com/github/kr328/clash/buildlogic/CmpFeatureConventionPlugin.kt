package com.github.kr328.clash.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class CmpFeatureConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("tabby.kmp.library")
      pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
      pluginManager.apply("org.jetbrains.compose")

      configureComposeCompiler()
      addCommonComposeDependencies()
    }
  }
}
