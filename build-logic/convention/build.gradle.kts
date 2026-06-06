import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
  alias(libs.plugins.spotless)
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_21 } }

dependencies {
  compileOnly(libs.android.gradlePlugin)
  compileOnly(libs.kotlin.gradlePlugin)
  compileOnly(libs.kotlin.compose.gradlePlugin)
  compileOnly(libs.spotless.gradlePlugin)
}

spotless {
  kotlin {
    target("src/**/*.kt")
    ktfmt(libs.ktfmt.get().version).googleStyle()
  }
  kotlinGradle { ktfmt(libs.ktfmt.get().version).googleStyle() }
}

gradlePlugin {
  plugins {
    register("kmpLibrary") {
      id = "tabby.kmp.library"
      implementationClass = "com.github.kr328.clash.buildlogic.KmpLibraryConventionPlugin"
    }
    register("cmpFeature") {
      id = "tabby.cmp.feature"
      implementationClass = "com.github.kr328.clash.buildlogic.CmpFeatureConventionPlugin"
    }
    register("cmpApplication") {
      id = "tabby.cmp.application"
      implementationClass = "com.github.kr328.clash.buildlogic.CmpApplicationConventionPlugin"
    }
    register("koin") {
      id = "tabby.di.koin"
      implementationClass = "com.github.kr328.clash.buildlogic.KoinConventionPlugin"
    }
  }
}
