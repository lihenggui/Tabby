package com.github.kr328.clash.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("org.jetbrains.kotlin.multiplatform")
      pluginManager.apply("com.android.kotlin.multiplatform.library")

      configureKotlinMultiplatform()

      extensions.configure<KotlinMultiplatformExtension> {
        targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
          val moduleNamespace =
            if (project.path.startsWith(":ui:")) {
              project.name.replace('-', '.')
            } else {
              project.path.drop(1).replace(':', '.').replace('-', '.')
            }

          namespace = "com.github.kr328.clash.$moduleNamespace"
          compileSdk = 37
          minSdk = 28
          withJava()
          withHostTest {}
          compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
        }
      }

      dependencies {
        "commonTestImplementation"(libs.findLibrary("kotlin-test").get())
        "commonTestImplementation"(libs.findLibrary("kotlinx-coroutines-test").get())
      }
    }
  }
}
