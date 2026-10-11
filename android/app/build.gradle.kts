// Nastavenia zostavenia samotnej appky.
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Verejné nastavenia servera zo súboru android/server.properties.
val server = Properties().apply {
    rootProject.file("server.properties").inputStream().use { load(it) }
}

android {
    namespace = "sk.weup.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "sk.weup.app"
        minSdk = 29 // Android 10
        targetSdk = 36 // Android 16
        versionCode = 1
        versionName = "0.1.0"

        buildConfigField("String", "SUPABASE_ADRESA", "\"${server.getProperty("supabaseAdresa")}\"")
        buildConfigField("String", "SUPABASE_VEREJNY_KLUC", "\"${server.getProperty("supabaseVerejnyKluc")}\"")
        buildConfigField("String", "GOOGLE_KLIENT_ID", "\"${server.getProperty("googleKlientId", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
