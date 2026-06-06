plugins {
  id("tabby.kmp.library")
  alias(libs.plugins.sqldelight)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.core.model)
      implementation(libs.sqldelight.runtime)
    }

    androidMain.dependencies { implementation(libs.sqldelight.android.driver) }

    desktopMain.dependencies { implementation(libs.sqldelight.sqlite.driver) }

    nativeMain.dependencies { implementation(libs.sqldelight.native.driver) }

    desktopTest.dependencies { implementation(libs.sqldelight.sqlite.driver) }
  }
}

sqldelight {
  databases {
    create("TabbyDatabase") {
      packageName.set("com.github.kr328.clash.database")
      schemaOutputDirectory.set(file("schemas"))
      verifyMigrations.set(true)
    }
  }
}
