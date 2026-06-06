plugins {
  id("tabby.kmp.library")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.kotlin.serialization.json)
      implementation(libs.kotlinx.datetime)
    }
  }
}
