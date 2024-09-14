plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.skie)
    alias(libs.plugins.ksp)
}

kotlin {

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true

            export(projects.sample.multimodule.featureDetail)
            export(projects.sample.multimodule.featureList)
        }
    }

    sourceSets {
        all {
            languageSettings {
                optIn("androidx.compose.material.ExperimentalMaterialApi")
            }
        }

        commonMain.dependencies {
            api(projects.sample.multimodule.featureDetail)
            api(projects.sample.multimodule.featureList)
            api(projects.sample.multimodule.navigation)
            implementation(projects.composeSwiftInterop)
            implementation(compose.runtime)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(libs.skie.annotations)

            implementation(libs.voyager.navigator)
            implementation(libs.voyager.lifecycleKmp)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
        }
    }
}

dependencies {
    skieSubPlugin(projects.composeSwiftInteropSkie)
}

kotlin.sourceSets.commonMain {
    kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
}

skie {
    features {
        enableSwiftUIObservingPreview = true
    }
}
