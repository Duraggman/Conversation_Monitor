import java.util.UUID

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
}

val genUUID by tasks.registering {
    val odir = project.layout.buildDirectory.dir("generated/assets/model-en-us").get().asFile
    doLast {
        val uuid = UUID.randomUUID().toString()
        val ofile = odir.resolve("uuid")
        odir.mkdirs()
        ofile.writeText(uuid)
    }
}

tasks.named("preBuild").configure {
    dependsOn(genUUID)
}



dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.jna)
    implementation(group = "com.alphacephei", name = "vosk-android", version = "0.3.32+")
}