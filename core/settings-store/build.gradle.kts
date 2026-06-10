plugins { id("tabby.kmp.library") }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.core.common)
      api(projects.core.model)
      api(libs.multiplatform.settings)
      implementation(libs.kotlin.serialization.core)
      implementation(libs.multiplatform.settings.serialization)
    }

    commonTest.dependencies {
      implementation(libs.multiplatform.settings.test)
    }
  }
}
