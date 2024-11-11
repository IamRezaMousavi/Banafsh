plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

android {
    namespace = "app.banafsh.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.banafsh.android"
        minSdk = 21
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
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.core.ktx)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.animation)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.util)
    implementation(libs.compose.ui.fonts)
    implementation(libs.compose.ripple)
    implementation(libs.compose.material3)

    implementation(libs.compose.activity)
    implementation(libs.compose.navigation)
    implementation(libs.compose.shimmer)
    implementation(libs.compose.coil)
    implementation(libs.compose.lottie)

    implementation(libs.kotlin.immutable)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

    coreLibraryDesugaring(libs.desugaring)

    implementation(projects.materialColorUtilities)

    detektPlugins(libs.detekt.compose)
}
