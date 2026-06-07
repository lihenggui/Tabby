plugins {
  id("tabby.cmp.application")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.ui.shared)
      api(projects.core.engineApi)
      api(projects.core.settingsStore)
      implementation(libs.kotlinx.coroutines.core)
      implementation(projects.ui.crash)
      implementation(projects.ui.home)
      implementation(projects.ui.log)
      implementation(projects.ui.profile)
      implementation(projects.ui.proxy)
      implementation(projects.ui.settings)
    }

    commonTest.dependencies { implementation(libs.multiplatform.settings.test) }

    desktopMain.dependencies { implementation(projects.core.engineDesktop) }

    iosMain.dependencies { implementation(projects.core.engineIos) }
  }
}
