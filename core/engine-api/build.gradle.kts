plugins { id("tabby.kmp.library") }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.core.model)
      implementation(libs.kotlinx.coroutines.core)
    }
  }
}
