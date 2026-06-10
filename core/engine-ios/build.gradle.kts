plugins { alias(libs.plugins.kotlin.multiplatform) }

kotlin {
  applyDefaultHierarchyTemplate()

  iosArm64()
  iosSimulatorArm64()

  targets.configureEach {
    compilations.configureEach {
      compileTaskProvider.configure {
        compilerOptions {
          allWarningsAsErrors.set(true)
          freeCompilerArgs.addAll("-Xcontext-sensitive-resolution")
        }
      }
    }
  }

  sourceSets {
    commonMain.dependencies {
      api(projects.core.engineApi)
      implementation(libs.kotlinx.coroutines.core)
    }
  }
}
