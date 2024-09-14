rootProject.name = "compose-swift-interop-root"
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

include(":sample:composeApp")
include(":sample:multimodule:feature-detail")
include(":sample:multimodule:feature-list")
include(":sample:multimodule:ios-umbrella")
include(":sample:multimodule:common")
include(":sample:multimodule:navigation")
include(":compose-swift-interop-ksp")
include(":compose-swift-interop-skie")
include(":compose-swift-interop")
