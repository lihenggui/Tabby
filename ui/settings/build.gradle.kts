plugins {
  id("tabby.cmp.feature")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  android { androidResources { enable = true } }

  sourceSets {
    commonMain.dependencies {
      api(projects.core.engineApi)
      api(projects.core.model)
      api(projects.core.settingsStore)
      implementation(projects.ui.shared)
      implementation(libs.composePreference)
      implementation(libs.reorderable)
    }

    androidMain {
      dependencies {
        implementation(projects.core.engineAndroid)
        implementation(projects.glue)

        implementation(libs.androidx.activity.compose)
        implementation(libs.jetbrains.navigation3.ui)
        implementation(libs.jetbrains.lifecycle.viewmodel.compose)

        implementation(project.dependencies.platform(libs.koin.bom))
        implementation(libs.koin.core)
      }
    }
  }
}
