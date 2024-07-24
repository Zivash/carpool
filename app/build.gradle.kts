plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.carpoolapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.carpoolapp"
        minSdk = 24
        targetSdk = 34
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    //ROOM
    //noinspection KaptUsageInsteadOfKsp
    kapt(libs.androidx.room.compiler)
    implementation(libs.glide)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    //Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    //Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.storage.ktx)

    //Maps API
    implementation(libs.places)
    implementation(libs.play.services.location)
    implementation(libs.okhttp)
    implementation(libs.json)
    implementation(libs.google.android.maps.utils)
    implementation(libs.material.v120)
    implementation(libs.play.services.maps.v2020)
    implementation(libs.material.v140)
    implementation(libs.androidx.appcompat.v131)
    implementation(libs.places.v250)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}