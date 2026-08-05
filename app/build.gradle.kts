import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.apollo)
    alias(libs.plugins.ksp)
    kotlin("plugin.serialization") version "2.0.21"
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { 
        localProperties.load(it) 
    }
}

android {
    namespace = "com.example.anilistapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.animew"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val clientID = localProperties.getProperty("ANILIST_CLIENT_ID") ?: "47691"
        val clientSecret = localProperties.getProperty("ANILIST_CLIENT_SECRET") ?: ""
        buildConfigField("String", "ANILIST_CLIENT_ID", "\"$clientID\"")
        buildConfigField("String", "ANILIST_CLIENT_SECRET", "\"$clientSecret\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    apollo {
        service("service") {
            packageName.set("com.example.anilistapp")
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Apollo
    implementation(libs.apollo.runtime)
    implementation("com.apollographql.apollo:apollo-normalized-cache:5.0.1")

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // YouTube Player
    implementation(libs.youtube.player)

    // DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Glance
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}