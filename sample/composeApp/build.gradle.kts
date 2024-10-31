import co.touchlab.skie.plugin.switflink.ProcessSwiftSourcesTask
import com.google.devtools.ksp.gradle.KspTaskNative
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.skie)
    alias(libs.plugins.maps)
    alias(libs.plugins.ksp)
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
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        all {
            languageSettings {
                optIn("androidx.compose.material.ExperimentalMaterialApi")
            }
        }

        commonMain.dependencies {
            implementation(projects.composeSwiftBridge)
            implementation(compose.runtime)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(libs.skie.annotations)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.maps.compose)
            implementation(libs.maps.playService)
        }
    }
}

android {
    namespace = "co.touchlab.compose.swift.interop"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "co.touchlab.compose.swift.interop"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    "kspCommonMainMetadata"(projects.composeSwiftBridgeKsp)
    "kspAndroid"(projects.composeSwiftBridgeKsp)

    "kspIosSimulatorArm64"(projects.composeSwiftBridgeKsp)
    "kspIosArm64"(projects.composeSwiftBridgeKsp)
    "kspIosX64"(projects.composeSwiftBridgeKsp)

    skieSubPlugin(projects.composeSwiftBridgeSkie)
}

tasks.withType<KspTaskNative>().configureEach {
    options.add(SubpluginOption("apoption", "compose-swift-interop.targetName=$target"))
}

tasks.withType<KotlinCompile<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

kotlin.sourceSets.commonMain {
    kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
}
