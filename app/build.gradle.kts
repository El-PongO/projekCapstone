import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

val props = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
val baseUrl = (props.getProperty("apiBaseUrl") ?: "http://10.10.3.199:3001/").let {
    if (it.endsWith("/")) it else "$it/"
}

android {
    namespace = "com.example.projectcapstone"
    compileSdk { version = release(37) }
    defaultConfig {
        applicationId = "com.example.projectcapstone"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "API_BASE_URL", "\"$baseUrl\"")
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.activity.compose)
    implementation(libs.core.ktx)

    // Retrofit + Moshi: sama seperti pola App.kt / WebService.kt tutor
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.coroutines.android)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.material)
}
