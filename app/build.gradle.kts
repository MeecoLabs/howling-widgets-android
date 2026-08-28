import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.koin.compiler)
}

android {
    namespace = "eu.meecolabs.howlingwidgets"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    val versionInfo = Properties().apply {
        project.file("version.properties").inputStream().use { load(it) }
    }

    val configProperties = Properties().apply {
        project.file("config.properties").inputStream().use { load(it) }
    }

    defaultConfig {
        applicationId = "eu.meecolabs.howlingwidgets"
        minSdk = 31
        versionCode = versionInfo.getProperty("VERSION_CODE").toInt()
        versionName = versionInfo.getProperty("VERSION_NAME")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        //noinspection WrongGradleMethod
        configProperties.forEach { (key, value) ->
            val propertyKey = key.toString()
            if (propertyKey.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*"))) {
                val propertyValue = value.toString().replace("\"", "\\\"")
                buildConfigField("String", propertyKey, "\"$propertyValue\"")
            } else {
                throw Exception("Warning: Property key '$propertyKey' from settings.properties is not a valid Java identifier. Skipping BuildConfig field generation for this key.")
            }
        }
    }

    signingConfigs {
        create("release") {
            storeFile = System.getenv("ANDROID_KEYSTORE_FILE")?.let { File(it) }
            storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
            keyAlias = System.getenv("ANDROID_KEY_ALIAS")
            keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        disable += "ModifierParameter"
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Compose
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(composeBom)

    // Breezy Weather
    implementation(libs.breezy.weather.data.sharing.lib)

    // App widgets
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // Serialization
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)

    // Navigation 3
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.core)

    // Work Manager
    implementation(libs.androidx.work.runtime.ktx)
    androidTestImplementation(libs.androidx.work.testing)

    // Koin/DI
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.compose.navigation3)
    implementation(libs.koin.annotations)
    implementation(libs.insert.koin.koin.android)
    implementation(libs.koin.androidx.workmanager)

    // Lottie
    implementation(libs.lottie.compose)

    // App Updates
    implementation(libs.app.updates)

    // Unit testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}