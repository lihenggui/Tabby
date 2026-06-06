plugins { alias(libs.plugins.android.library) }

dependencies {
  api(projects.core)
  api(projects.core.settingsStore)
  api(projects.service)
  api(projects.common)

  implementation(libs.kotlin.coroutine)
  implementation(libs.kotlin.serialization.json)
  implementation(libs.androidx.browser)
  implementation(libs.androidx.core)
}
