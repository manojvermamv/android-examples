plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.jetbrainsKotlinKapt)
}

android {
    namespace = "com.android.interviewdemo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.android.interviewdemo"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // default resources values (don't remove resValue 'app_name' because it used by project)
        resValue("string", "app_name", "Android Example")
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

    // - - Koin
    //implementation(libs.koin.android)
    //implementation(libs.koin.core)

    // - - Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")

    // - - Room
    implementation("androidx.room:room-runtime:2.2.1")
    kapt("androidx.room:room-compiler:2.2.1")

    // - - Image Loading
    implementation("com.squareup.picasso:picasso:2.71828")

    // - - Lifecycle Extensions and ViewModel
    implementation("androidx.lifecycle:lifecycle-extensions:2.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.1.0")
    implementation("androidx.activity:activity:1.0.0")
    implementation("androidx.activity:activity-ktx:1.0.0")
    implementation("androidx.fragment:fragment-ktx:1.1.0")

    // - - Retrofit2
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.3.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.3.1")
}