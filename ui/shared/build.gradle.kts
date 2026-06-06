plugins { id("tabby.cmp.feature") }

compose.resources { publicResClass = true }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.core.model)
      api(libs.jetbrains.navigation3.ui)
      implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)
    }

    androidMain {
      dependencies {
        implementation(projects.glue)

        implementation(libs.androidx.activity.compose)
        implementation(libs.jetbrains.lifecycle.viewmodel.compose)
      }
    }
  }
}
