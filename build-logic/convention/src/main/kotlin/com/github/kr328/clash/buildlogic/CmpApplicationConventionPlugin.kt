package com.github.kr328.clash.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

class CmpApplicationConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("tabby.cmp.feature")

      extensions.configure<KotlinMultiplatformExtension> {
        targets.withType<KotlinNativeTarget>().configureEach {
          binaries.framework {
            binaryOption(
              "bundleId",
              "io.github.goooler.tabby.${project.path.drop(1).replace(':', '.').replace('-', '.')}",
            )
            baseName =
              project.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
              }
            isStatic = true
          }
        }
      }

      dependencies {
        "androidMainImplementation"(libs.findLibrary("androidx-activity-compose").get())
        "desktopMainImplementation"(libs.findLibrary("kotlinx-coroutines-swing").get())
      }
    }
  }
}
