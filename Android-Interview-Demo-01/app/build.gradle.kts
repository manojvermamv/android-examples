import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.android.interviewdemo"
    compileSdk = 34

    val appId = "01"
    defaultConfig {
        applicationId = "com.android.interviewdemo"
        applicationIdSuffix = ".debug$appId"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        archivesName = "android-interview-demo-$appId"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // default resources values (don't remove resValue 'app_name' because it used by project)
        resValue("string", "app_name", "Android-Interview-Demo-$appId")
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
        buildConfig = true
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // - - Retrofit2
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.3.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.3.1")
}