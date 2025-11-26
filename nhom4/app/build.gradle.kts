plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.nhom4"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.nhom4"
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
        // --- BẬT TÍNH NĂNG DESUGARING TẠI ĐÂY ---
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

val cameraxVersion = "1.3.4"

dependencies {
    // --- THÊM THƯ VIỆN DESUGARING TẠI ĐÂY ---
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation("io.reactivex.rxjava3:rxjava:3.1.5")
    implementation(libs.impress)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.firebase.database)
    implementation(libs.rendering)
    implementation(libs.firebase.storage)
    implementation(libs.datastore.core)
    implementation(libs.contentpager)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // CameraX
    implementation("androidx.camera:camera-core:${cameraxVersion}")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
    implementation("androidx.camera:camera-view:${cameraxVersion}")
    implementation("androidx.camera:camera-video:${cameraxVersion}")
    // Lưu ý: Dòng firebase-storage ở dưới bị trùng lặp, tôi đã giữ nguyên nhưng bạn có thể xóa bớt 1 dòng nếu muốn
    implementation("com.google.firebase:firebase-storage")
}
