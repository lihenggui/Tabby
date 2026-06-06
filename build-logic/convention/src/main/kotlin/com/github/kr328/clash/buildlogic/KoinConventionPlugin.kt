package com.github.kr328.clash.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KoinConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      dependencies {
        "commonMainImplementation"(libs.findLibrary("koin-core").get())
        "androidMainImplementation"(libs.findLibrary("koin-android").get())
      }
    }
  }
}
