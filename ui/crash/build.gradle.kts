plugins {
  id("tabby.cmp.feature")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  android { androidResources { enable = true } }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.ui.shared)
      implementation(libs.composePreference)
    }

    androidMain {
      dependencies {
        implementation(projects.glue)

        implementation(libs.jetbrains.navigation3.ui)
        implementation(libs.androidx.lifecycle.viewmodel.compose)
      }
    }
  }
}
