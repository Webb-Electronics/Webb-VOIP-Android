import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.compose.compiler)
    id("com.google.devtools.ksp")
}

// Site-specific deployment settings live in `deployment.properties`, which is
// git-ignored. Missing file or missing key resolves to an empty string, which
// each consumer treats as "feature not configured".
// See deployment.properties.example for the available keys.
val deploymentProperties = Properties().apply {
    val file = rootProject.file("deployment.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    } else {
        logger.lifecycle(
            "deployment.properties not found -- building without deployment configuration. " +
                "Copy deployment.properties.example to deployment.properties to set it."
        )
    }
}

fun deploymentProperty(key: String): String = deploymentProperties.getProperty(key, "")

android {
    namespace = "ca.webb.mobile.companionapp.voip.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "ca.webb.mobile.companionapp.voip.android"
        minSdk = 28
        targetSdk = 36
        versionCode = 19
        versionName = "1.0.0"

        buildConfigField(
            "String", "WEBB_DOMAIN_ALIASES", "\"${deploymentProperty("webb.domainAliases")}\""
        )
        buildConfigField(
            "String", "WEBB_NAT_STUN_POLICIES", "\"${deploymentProperty("webb.natStunPolicies")}\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            defaultConfig {
                buildConfigField("String", "UNIQUE_VOIP_ID", "\"voip-6c3e7e5b-2d1a-4f9c-b8a1-3a7f2d9e6c4b-ta\"")
                buildConfigField("String", "UNIQUE_CALLER_ID", "\"voip-call-id-b1d9f2c3-7e4a-4f8c-9d2a-3c5f7b1e6a8d\"")
            }
        }
        debug  {
            defaultConfig {
                buildConfigField("String", "UNIQUE_VOIP_ID", "\"voip-2d1a3a7f-6e4c-4c3e-b8a1-9f6c7e5b2d1a-ta\"")
                buildConfigField("String", "UNIQUE_CALLER_ID", "\"voip-call-id-b1d9f2c3-7e4a-4f8c-9d2a-3c5f7b1e6a8d\"")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Compile with JDK 21 regardless of the JDK running Gradle. Without this the
// build only works on a machine whose default JDK the Kotlin compiler happens
// to support, which is why it previously needed org.gradle.java.home pinned in
// a user-global gradle.properties.
kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.material.icons.extended)

    implementation(libs.volley)
    // Linphone SDK

    debugImplementation(libs.linphone.sdk.android.debug)

    releaseImplementation(libs.linphone.sdk.android)

    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.gson)

    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
}