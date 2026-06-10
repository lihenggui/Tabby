package com.github.kr328.clash.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.withType

internal fun Project.configureAndroid(commonExtension: CommonExtension) {
  commonExtension.apply {
    compileSdk = 37
    defaultConfig.apply {
      minSdk = 28
      externalNativeBuild.cmake.abiFilters += listOf("arm64-v8a", "x86_64")
    }
    ndkVersion = "29.0.14206865"
    compileOptions.apply {
      sourceCompatibility = JavaVersion.VERSION_21
      targetCompatibility = JavaVersion.VERSION_21
    }
  }

  tasks.withType<Test>().configureEach { failOnNoDiscoveredTests = false }
}
