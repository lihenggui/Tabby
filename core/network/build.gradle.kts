plugins {
  id("tabby.kmp.library")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.kotlin.serialization.json)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.ktor.client.content.negotiation)
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.serialization.kotlinx.json)
    }

    androidMain.dependencies { implementation(libs.ktor.client.android) }

    desktopMain.dependencies { implementation(libs.ktor.client.java) }

    iosMain.dependencies { implementation(libs.ktor.client.darwin) }

    commonTest.dependencies {
      implementation(libs.ktor.client.mock)
      implementation(libs.kotlinx.coroutines.test)
    }
  }
}
