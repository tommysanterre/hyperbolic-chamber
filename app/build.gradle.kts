import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val localSigning = Properties().apply {
    val propertiesFile = rootProject.file("signing/keystore.properties")
    if (propertiesFile.exists()) propertiesFile.inputStream().use(::load)
}
val signingStoreFile = providers.environmentVariable("SNACKLOOP_KEYSTORE_PATH").orNull
    ?: localSigning.getProperty("storeFile")
val signingStorePassword = providers.environmentVariable("SNACKLOOP_STORE_PASSWORD").orNull
    ?: localSigning.getProperty("storePassword")
val signingKeyAlias = providers.environmentVariable("SNACKLOOP_KEY_ALIAS").orNull
    ?: localSigning.getProperty("keyAlias")
val signingKeyPassword = providers.environmentVariable("SNACKLOOP_KEY_PASSWORD").orNull
    ?: localSigning.getProperty("keyPassword")
val hasReleaseSigning = !signingStoreFile.isNullOrBlank() &&
    !signingStorePassword.isNullOrBlank() &&
    !signingKeyAlias.isNullOrBlank() &&
    !signingKeyPassword.isNullOrBlank()

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

android {
    namespace = "ca.tommysanterre.snackloop"
    compileSdk = 36

    defaultConfig {
        applicationId = "ca.tommysanterre.snackloop"
        minSdk = 26
        targetSdk = 36
        versionCode = providers.gradleProperty("snackloopVersionCode").orNull?.toInt() ?: 1000
        versionName = providers.gradleProperty("snackloopVersionName").orNull ?: "0.2.0"
    }

    if (hasReleaseSigning) {
        signingConfigs {
            create("snackloopRelease") {
                storeFile = file(signingStoreFile!!)
                storePassword = signingStorePassword
                keyAlias = signingKeyAlias
                keyPassword = signingKeyPassword
            }
        }
    }

    buildTypes {
        getByName("release") {
            if (hasReleaseSigning) signingConfig = signingConfigs.getByName("snackloopRelease")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

val verifyReleaseSigning = tasks.register("verifyReleaseSigning") {
    doLast {
        check(hasReleaseSigning) {
            "Release signing is missing. Configure signing/keystore.properties or SNACKLOOP signing environment variables."
        }
    }
}
tasks.matching { it.name == "assembleRelease" }.configureEach { dependsOn(verifyReleaseSigning) }

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
