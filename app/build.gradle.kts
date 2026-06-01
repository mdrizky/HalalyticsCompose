plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
}

import java.util.Properties

// Java & Kotlin compatibility
android {
    namespace = "com.example.halalyticscompose"
    compileSdk = 34

    // 🔒 SECURE: Load API keys from local.properties
    val localProperties = Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) {
            file.inputStream().use { load(it) }
        }
    }

    defaultConfig {
        applicationId = "com.example.halalyticscompose"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Backend configuration
        val apiBaseUrl = (localProperties.getProperty("API_BASE_URL") ?: "http://10.0.2.2:8000/api/").replace("\"", "\\\"")
        val reverbBaseUrl = (localProperties.getProperty("REVERB_BASE_URL") ?: "ws://10.0.2.2:8080").replace("\"", "\\\"")
        val reverbAppKey = (localProperties.getProperty("REVERB_APP_KEY") ?: "").replace("\"", "\\\"")
        val apiCertPin = (localProperties.getProperty("API_CERT_PIN") ?: "").replace("\"", "\\\"")
        val googleClientId = (localProperties.getProperty("GOOGLE_CLIENT_ID") ?: "").replace("\"", "\\\"")
        val facebookAppId = (localProperties.getProperty("FACEBOOK_APP_ID") ?: "").replace("\"", "\\\"")
        
        // BuildConfig fields
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("String", "API_CERT_PIN", "\"$apiCertPin\"")
        buildConfigField("String", "REVERB_BASE_URL", "\"$reverbBaseUrl\"")
        buildConfigField("String", "REVERB_APP_KEY", "\"$reverbAppKey\"")
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"$googleClientId\"")
        buildConfigField("String", "FACEBOOK_APP_ID", "\"$facebookAppId\"")
        // Facebook SDK ApplicationId (strings resource) — isi FACEBOOK_APP_ID di local.properties
        resValue("string", "facebook_app_id", facebookAppId.ifBlank { "000000000000000" })
        
        // Default AI Keys (will be overridden in buildTypes)
        buildConfigField("String", "GEMINI_API_KEY", "\"YOUR_DEV_KEY\"")
        buildConfigField("String", "GROQ_API_KEY", "\"YOUR_DEV_KEY\"")
        buildConfigField("String", "NEWSDATA_API_KEY", "\"YOUR_DEV_KEY\"")
        buildConfigField("String", "ANTHROPIC_API_KEY", "\"YOUR_DEV_KEY\"")
        buildConfigField("String", "UNSPLASH_API_KEY", "\"YOUR_DEV_KEY\"")
        buildConfigField("boolean", "DEBUG_BUILD", "true")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            
            // Development API keys from localProperties
            val geminiApiKey = (localProperties.getProperty("GEMINI_API_KEY") ?: "YOUR_DEV_KEY").replace("\"", "\\\"")
            val groqApiKey = (localProperties.getProperty("GROQ_API_KEY") ?: "YOUR_DEV_KEY").replace("\"", "\\\"")
            val newsDataApiKey = (localProperties.getProperty("NEWSDATA_API_KEY") ?: "YOUR_DEV_KEY").replace("\"", "\\\"")
            val anthropicApiKey = (localProperties.getProperty("ANTHROPIC_API_KEY") ?: "YOUR_DEV_KEY").replace("\"", "\\\"")
            val unsplashApiKey = (localProperties.getProperty("UNSPLASH_API_KEY") ?: "YOUR_DEV_KEY").replace("\"", "\\\"")
            
            // Override build config fields for debug
            buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
            buildConfigField("String", "GROQ_API_KEY", "\"$groqApiKey\"")
            buildConfigField("String", "NEWSDATA_API_KEY", "\"$newsDataApiKey\"")
            buildConfigField("String", "ANTHROPIC_API_KEY", "\"$anthropicApiKey\"")
            buildConfigField("String", "UNSPLASH_API_KEY", "\"$unsplashApiKey\"")
            buildConfigField("boolean", "DEBUG_BUILD", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // Production: Empty API keys (fetch from backend)
            buildConfigField("String", "GEMINI_API_KEY", "\"\"")
            buildConfigField("String", "GROQ_API_KEY", "\"\"")
            buildConfigField("String", "NEWSDATA_API_KEY", "\"\"")
            buildConfigField("String", "ANTHROPIC_API_KEY", "\"\"")
            buildConfigField("String", "UNSPLASH_API_KEY", "\"\"")
            buildConfigField("boolean", "DEBUG_BUILD", "false")
        }
    }

    // ✅ Java & Kotlin compatibility
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
        allWarningsAsErrors = false
    }

    // ✅ Aktifkan Compose
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    // 🔹 Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // 🔹 Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(libs.androidx.navigation.compose)
    implementation(libs.generativeai)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.benchmark.baseline.profile.gradle.plugin)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material)
    implementation(libs.google.material)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // 🔹 Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // 🔹 Library Tambahan (tetap dipertahankan)
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Navigation (buat pindah antar screen)
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Retrofit + OkHttp (buat koneksi API Laravel)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:retrofit:2.10.0")
    implementation("com.squareup.retrofit2:converter-gson:2.10.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Facebook Login
    implementation("com.facebook.android:facebook-android-sdk:17.0.0")
    implementation("com.facebook.android:facebook-login:17.0.0")

    //Animasi
    implementation("androidx.compose.animation:animation:1.5.4")

    // 🔹 CameraX
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

    // 🔹 ML Kit Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // 🔹 Image Loading (Coil)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // 🔹 File Picker & Image Upload
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // 🔹 Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")

    // 🔹 Firebase (BoM 33.8.0 for Kotlin 2.0+ compatibility)
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-vertexai")
    implementation("com.google.firebase:firebase-database-ktx")
    
    // 🔹 Room Database
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    // 🔹 SQLCipher (Database Encryption)
    implementation("net.zetetic:sqlcipher-android:4.6.1@aar")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")

    // 🔹 DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")


    // 🔹 Charts (for Weekly Journal)
    implementation("co.yml:ycharts:2.1.0")
    
    // 🔹 Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    ksp("androidx.hilt:hilt-compiler:1.2.0")
    
    // 🔹 WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("com.google.accompanist:accompanist-permissions:0.37.2")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // 🔹 QR Code Generation (Zxing)
    implementation("com.google.zxing:core:3.5.3")
}
