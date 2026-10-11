plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

val releaseKeyStorePath = providers.environmentVariable("ANDROID_SIGNING_KEYSTORE_FILE").orNull
val releaseKeyStorePassword = providers.environmentVariable("ANDROID_SIGNING_STORE_PASSWORD").orNull
val releaseKeyAlias = "homeledger-release"
val releaseBuildRequested = gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }

if (releaseBuildRequested) {
    val missingSigningInputs = buildList {
        if (releaseKeyStorePath.isNullOrBlank()) add("ANDROID_SIGNING_KEYSTORE_FILE")
        if (releaseKeyStorePassword.isNullOrBlank()) add("ANDROID_SIGNING_STORE_PASSWORD")
        if (!releaseKeyStorePath.isNullOrBlank() && !file(releaseKeyStorePath).isFile) add("keystore file at ANDROID_SIGNING_KEYSTORE_FILE")
    }
    if (missingSigningInputs.isNotEmpty()) {
        throw GradleException("Release builds require valid signing inputs: ${missingSigningInputs.joinToString()}")
    }
}

android {
    namespace = "com.homeledger.app"
    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }
    buildToolsVersion = "37.0.0"

    defaultConfig {
        applicationId = "com.homeledger.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (!releaseKeyStorePath.isNullOrBlank()) {
                storeFile = file(releaseKeyStorePath)
                storeType = "PKCS12"
                storePassword = releaseKeyStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyStorePassword
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
        }
    }

    buildFeatures { compose = true }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
    testOptions { unitTests.isReturnDefaultValues = true }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    val composeBom = platform("androidx.compose:compose-bom:2026.09.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.activity:activity-compose:1.12.1")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}
