plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.jetbrainsCompose)
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
            api(compose.runtime)
            api(compose.runtimeSaveable)
            api(libs.ktxSerialization.core)
            api(libs.ktxSerialization.json)
            api(libs.ktxCoroutines.core)
        }
    }
}


android {
    namespace = "co.touchlab.compose.swift.interop.common"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}
