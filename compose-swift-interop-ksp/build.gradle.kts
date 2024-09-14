plugins {
    kotlin("jvm")
    alias(libs.plugins.maven.publish)
}

dependencies {
    implementation(libs.ksp)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    implementation(libs.kasechange)
}
