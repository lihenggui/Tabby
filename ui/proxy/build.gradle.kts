plugins {
  id("tabby.cmp.feature")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.core.engineApi)
      implementation(projects.ui.shared)
    }

    androidMain {
      kotlin.srcDir("src/main/kotlin")

      dependencies {
        implementation(projects.core.engineAndroid)
        implementation(projects.glue)

        implementation(libs.jetbrains.lifecycle.viewmodel.compose)
        implementation(libs.jetbrains.navigation3.ui)
      }
    }
  }
}
