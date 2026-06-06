plugins { alias(libs.plugins.android.library) }

android { namespace = "com.github.kr328.clash.engine.android" }

dependencies {
  api(projects.core.engineApi)

  implementation(projects.glue)
  implementation(projects.service)

  implementation(libs.kotlin.coroutine)
  implementation(libs.kotlin.serialization.json)
}
