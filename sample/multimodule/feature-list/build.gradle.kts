import com.google.devtools.ksp.gradle.KspTaskNative
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
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
    )

    sourceSets {
        all {
            languageSettings {
                optIn("androidx.compose.material.ExperimentalMaterialApi")
            }
        }

        commonMain.dependencies {
            implementation(projects.sample.multimodule.common)
            implementation(projects.sample.multimodule.navigation)

            implementation(projects.composeSwiftInterop)
            implementation(compose.runtime)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(libs.skie.annotations)

            implementation(libs.voyager.navigator)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.maps.compose)
            implementation(libs.maps.playService)
        }
    }
}

android {
    namespace = "co.touchlab.compose.swift.interop.list"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}

dependencies {
    "kspCommonMainMetadata"(projects.composeSwiftInteropKsp)
    "kspAndroid"(projects.composeSwiftInteropKsp)

    "kspIosSimulatorArm64"(projects.composeSwiftInteropKsp)
    "kspIosArm64"(projects.composeSwiftInteropKsp)
    "kspIosX64"(projects.composeSwiftInteropKsp)
}

ksp {
    arg("compose-swift-interop.defaultFactoryName", "List")
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
