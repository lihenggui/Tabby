package com.github.kr328.clash.buildlogic

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

private val temporaryNativeMetadataWarningsAsErrorsException =
  Regex("""compile(?:Native|Apple|Ios).*KotlinMetadata""")

internal fun Project.configureKotlinMultiplatform(configureIosFrameworks: Boolean = false) {
  extensions.configure<KotlinMultiplatformExtension> {
    applyDefaultHierarchyTemplate()

    jvm("desktop") { compilerOptions { jvmTarget.set(JvmTarget.JVM_21) } }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
      if (configureIosFrameworks) {
        target.binaries.framework {
          baseName =
            project.name.replaceFirstChar {
              if (it.isLowerCase()) it.titlecase() else it.toString()
            }
          isStatic = true
        }
      }
    }

    targets.configureEach {
      compilations.configureEach {
        compileTaskProvider.configure {
          // Temporary exception: current official Compose/AndroidX KMP artifacts can emit
          // duplicate KLIB unique_name warnings while compiling intermediate Native metadata.
          // Tracker: https://youtrack.jetbrains.com/issue/KT-66568
          // Related module-name consistency issue: https://youtrack.jetbrains.com/issue/KT-69701
          // Keep Werror for Android/JVM and leaf target compilations, and remove this when the
          // official dependency graph no longer emits those Kotlin/Native loader warnings.
          val allWarningsAsErrorsForTask =
            !temporaryNativeMetadataWarningsAsErrorsException.matches(name)

          compilerOptions {
            allWarningsAsErrors.set(allWarningsAsErrorsForTask)
            freeCompilerArgs.add("-Xcontext-sensitive-resolution")
          }
        }
      }
    }
  }

  tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_21.toString()
    targetCompatibility = JavaVersion.VERSION_21.toString()
    if (!name.endsWith("JavaWithJavac")) options.release.set(21)
  }

  if (tasks.findByName("testClasses") == null) {
    tasks.register("testClasses") { dependsOn("allTests") }
  }
}
