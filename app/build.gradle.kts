plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.a2uidevfest"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.a2uidevfest"
        minSdk = 26
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.matching { it.name.startsWith("check") && it.name.endsWith("AarMetadata") }.configureEach {
    enabled = false
}

configurations.configureEach {
    resolutionStrategy {
        force("androidx.concurrent:concurrent-futures:1.2.0")
        force("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    }
}

dependencies {
    implementation("com.google.guava:guava:31.1-android") {
        isTransitive = false
    }
    implementation("androidx.concurrent:concurrent-futures:1.2.0") {
        isTransitive = false
    }
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)

    // Jetpack A2UI Stack (Architecture: Model + Engine + Compose Runtime/UI + Material3 Catalog)
    implementation(libs.androidx.a2ui.model)
    implementation(libs.androidx.a2ui.engine)
    implementation(libs.androidx.a2ui.compose.runtime)
    implementation(libs.androidx.a2ui.compose.ui)
    implementation(libs.androidx.compose.material3.a2ui)
    implementation(libs.androidx.concurrent.futures)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
