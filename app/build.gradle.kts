import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

// Forwards the API port from the device to this PC so the app can reach the local server.
// Harmless if no device is connected.
val adbReverse = tasks.register<Exec>("adbReverse") {
    val sdkDir = localProps.getProperty("sdk.dir")
    commandLine("${sdkDir}/platform-tools/adb", "reverse", "tcp:3000", "tcp:3000")
    isIgnoreExitValue = true
}
tasks.matching { it.name == "assembleDebug" }.configureEach { finalizedBy(adbReverse) }

android {
    namespace = "com.example.projectcapstone"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.projectcapstone"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Default works with `adb reverse` (run automatically by the build). For a phone on Wi-Fi, set apiBaseUrl in local.properties (e.g. http://192.168.1.5:3000/).
        buildConfigField("String", "API_BASE_URL", "\"${localProps.getProperty("apiBaseUrl", "http://127.0.0.1:3000/")}\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}