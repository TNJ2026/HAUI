plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
}

android {
    namespace = "ai.tnj.haui.core.model"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)

    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
