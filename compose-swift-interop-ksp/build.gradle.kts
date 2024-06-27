plugins {
    kotlin("jvm")
}

dependencies {
    implementation(libs.ksp)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    implementation(libs.kasechange)
}
