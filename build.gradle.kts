// build.gradle.kts (Project: Ecoriego)  -- NIVEL RAÍZ
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // 🔧 AGREGA ESTA LÍNEA:
    id("com.google.gms.google-services") version "4.4.2" apply false
}
