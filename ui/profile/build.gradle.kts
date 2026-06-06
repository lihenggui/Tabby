plugins {
  id("tabby.cmp.feature")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  android { androidResources { enable = true } }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.core.common)
      implementation(projects.ui.shared)
      implementation(libs.composePreference)
    }

    androidMain {
      dependencies {
        implementation(projects.core.engineAndroid)
        implementation(projects.glue)

        implementation(libs.androidx.activity.compose)
        implementation(libs.jetbrains.navigation3.ui)
        implementation(libs.jetbrains.lifecycle.viewmodel.compose)
        implementation(libs.quickie.bundled)
        implementation(libs.bytesize)
      }
    }
  }
}
