plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.zeljkobilbija001.autostradeinfo"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.zeljkobilbija001.autostradeinfo"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // R8 (skraćivanje i maskiranje) – Google traži bar 25% „offuscamento“.
            // Pravila u proguard-rules.pro čuvaju WorkDatabase_Impl (WorkManager iz AdMob-a; Room ga traži po imenu,
            // bez pravila je release verzija padala pri pokretanju, InterstateInfo 16.11.2026).
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.core.splashscreen)
    implementation(libs.recyclerview)
    implementation(libs.play.services.ads)
    implementation(libs.ump)
    implementation(libs.billing)
    implementation(libs.play.services.location)
    implementation(libs.material)
    implementation(libs.work.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}