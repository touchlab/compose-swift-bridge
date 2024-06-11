import android.databinding.tool.ext.toCamelCase
import co.touchlab.skie.plugin.util.lowerCamelCaseName
import com.google.devtools.ksp.gradle.KspTaskNative
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
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
            implementation(projects.composeSwiftInterop)
            implementation(compose.runtime)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
        }

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
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

    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

dependencies {
    "kspCommonMainMetadata"(projects.composeSwiftInteropGenerator)
    "kspAndroid"(projects.composeSwiftInteropGenerator)

    "kspIosSimulatorArm64"(projects.composeSwiftInteropGenerator)
    "kspIosArm64"(projects.composeSwiftInteropGenerator)
    "kspIosX64"(projects.composeSwiftInteropGenerator)
}

tasks.withType<KspTaskNative> {
    val skieCompilationAbsolutePath = layout.buildDirectory.file("skie/compilation/").get().asFile.absolutePath
    outputs.dir(skieCompilationAbsolutePath) // forces KSP task cache to sync with SKIE output folder

    options.add(SubpluginOption("apoption", "swiftInterop.targetName=${this.target}"))
    options.add(
        SubpluginOption(
            "apoption",
            "swiftInterop.skieCompilationFolderAbsolutePath=${skieCompilationAbsolutePath}"
        )
    )
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().all {
    if(name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

kotlin.sourceSets.commonMain {
    kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
}