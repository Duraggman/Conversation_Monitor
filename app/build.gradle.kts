plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.conversation_monitor"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.conversation_monitor"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Adding the Mozilla Speech Library
    implementation("com.github.mozilla:mozillaspeechlibrary:2.0.0")
    implementation("commons-io:commons-io:2.15.1") // Just in case you want to use the utils for downloading/unzipping

}