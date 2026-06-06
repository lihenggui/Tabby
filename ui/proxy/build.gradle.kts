plugins {
  id("tabby.cmp.feature")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  targets.named<org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget>("desktop") {
    testRuns["test"].executionTask.configure { useJUnit() }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.core.engineApi)
      implementation(projects.ui.shared)
    }

    desktopTest.dependencies { implementation(libs.kotlin.test.junit) }

    androidMain {
      dependencies {
        implementation(projects.core.engineAndroid)
        implementation(projects.glue)

        implementation(libs.jetbrains.lifecycle.viewmodel.compose)
        implementation(libs.jetbrains.navigation3.ui)
      }
    }
  }
}
