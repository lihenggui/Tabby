plugins {
  id("tabby.cmp.feature")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  android { androidResources { enable = true } }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.core.engineApi)
      implementation(projects.ui.shared)
      implementation(libs.composePreference)
      implementation(libs.semver)
    }

    androidMain {
      dependencies {
        implementation(projects.core.engineAndroid)
        implementation(projects.core.network)
        implementation(projects.glue)

        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.lifecycle.viewmodel.compose)
        implementation(libs.jetbrains.navigation3.ui)

        implementation(project.dependencies.platform(libs.koin.bom))
        implementation(libs.koin.android)
      }
    }
  }
}
