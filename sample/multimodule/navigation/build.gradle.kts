plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    )

    sourceSets {
        commonMain.dependencies {
            api(libs.voyager.core)
            api(projects.sample.multimodule.common)
        }
    }
}

android {
    namespace = "co.touchlab.compose.swift.interop.navigation"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}
