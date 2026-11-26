plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false

    id("com.google.gms.google-services") version "4.6.0" apply false
}
