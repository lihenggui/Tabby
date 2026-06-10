plugins { alias(libs.plugins.kotlin.jvm) }

dependencies {
  api(projects.core.engineApi)

  implementation(projects.core.database)
  implementation(projects.core.network)

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlin.serialization.json)
  implementation(libs.ktor.client.core)
  implementation(libs.snakeyaml.engine)

  testImplementation(libs.ktor.client.mock)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.kotlin.test)
}
