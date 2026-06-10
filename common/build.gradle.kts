plugins { alias(libs.plugins.android.library) }

dependencies {
  api(projects.core.common)

  implementation(libs.kotlin.coroutine)
  implementation(libs.androidx.core)

  implementation(platform(libs.koin.bom))
  implementation(libs.koin.core)
}
