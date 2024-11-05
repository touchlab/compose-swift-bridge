rootProject.name = "compose-swift-bridge-root"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

//include(":sample:composeApp")
//include(":sample:multimodule:feature-detail")
//include(":sample:multimodule:feature-list")
//include(":sample:multimodule:ios-umbrella")
//include(":sample:multimodule:common")
//include(":sample:multimodule:navigation")
include(":compose-swift-bridge-ksp")
include(":compose-swift-bridge-skie")
include(":compose-swift-bridge")
