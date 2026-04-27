plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)      //plugins para google maps y room
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.sachna.tasky"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sachna.tasky"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true   //activar el viewBinding
    }
}

dependencies {
    implementation(libs.play.services.auth)

    implementation (libs.squareup.retrofit)
    implementation (libs.squareup.converter.gson)


    implementation (libs.glide)
    annotationProcessor (libs.compiler)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.common)
    implementation(libs.play.services.maps)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.preference.v120)
    implementation(libs.androidx.drawerlayout)
    implementation(libs.material.v170)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.ads.mobile.sdk)
    implementation(libs.androidx.cardview)

    implementation(libs.filament.android)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}