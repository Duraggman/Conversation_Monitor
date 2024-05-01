plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.convo_monitor"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.convo_monitor"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Chaquopy-specific settings
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            // On Apple silicon, you can omit x86_64.
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
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
}


dependencies {
    //implement json
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("net.java.dev.jna:jna:5.14.0@aar")
    implementation("com.alphacephei:vosk-android:0.3.47@aar")
    implementation(project(":models"))
    implementation("be.tarsos.dsp:core:2.5")
    implementation("be.tarsos.dsp:jvm:2.5")
    implementation(libs.json.json)
    implementation(libs.silero)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}