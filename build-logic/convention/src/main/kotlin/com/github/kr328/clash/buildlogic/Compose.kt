package com.github.kr328.clash.buildlogic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

internal fun Project.configureComposeCompiler() {
  extensions.configure(ComposeCompilerGradlePluginExtension::class.java) {
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("stability.conf"))
  }
}

internal fun Project.addCommonComposeDependencies() {
  dependencies {
    "commonMainImplementation"(libs.findLibrary("jetbrains-compose-runtime").get())
    "commonMainImplementation"(libs.findLibrary("jetbrains-compose-foundation").get())
    "commonMainImplementation"(libs.findLibrary("jetbrains-compose-ui").get())
    "commonMainImplementation"(libs.findLibrary("jetbrains-compose-ui-util").get())
    "commonMainImplementation"(libs.findLibrary("jetbrains-compose-ui-tooling-preview").get())
    "commonMainImplementation"(libs.findLibrary("jetbrains-compose-material3").get())
    "commonMainImplementation"(libs.findLibrary("jetbrains-compose-components-resources").get())
  }
}
