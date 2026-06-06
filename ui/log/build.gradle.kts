plugins {
  id("tabby.cmp.feature")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  android { androidResources { enable = true } }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.core.model)
      implementation(projects.ui.shared)

      implementation(libs.kotlinx.coroutines.core)
    }

    androidMain {
      dependencies {
        implementation(projects.core.engineAndroid)
        implementation(projects.glue)

        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.core)
        implementation(libs.androidx.lifecycle.viewmodel.compose)
        implementation(libs.jetbrains.navigation3.ui)
        implementation(libs.kotlin.serialization.json)
      }
    }
  }
}
