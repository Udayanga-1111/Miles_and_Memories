plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.milesmemories"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.milesmemories"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.compose.animation.core.lint)
    implementation(libs.coil.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    //noinspection UseTomlInstead,NewerVersionAvailable
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    var navVersion = "2.9.6"
    implementation("androidx.navigation:navigation-compose:2.9.7")
    //noinspection UseTomlInstead
    implementation("com.google.accompanist:accompanist-flowlayout:0.36.0")
    //noinspection UseTomlInstead,NewerVersionAvailable
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.36.0")

    // Firebase
    //noinspection GradleDependency,UseTomlInstead
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-auth:24.0.1")
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-firestore:26.1.2")
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-storage:22.0.1")

    // Google Login
    implementation("com.google.android.gms:play-services-auth:21.5.1")
}
