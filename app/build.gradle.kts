plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.com.google.dagger.hilt.android)
    alias(libs.plugins.com.google.devtools.ksp)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.stability.analyzer)
}


// Updated by roar
val vName = "0.1.2"

// Compute versionCode based on versionName, by padding the segments in thousands groups,
// allowing for up to 1000 patch versions per major/minor.
// v0 will be codes less than 1_000_000.
// https://pl.kotl.in/LUdtQtZh6
val (vMajor, vMinor, vPatch) = vName
    .split(".")
    .map { s -> s.toInt(radix = 10).also { require(it < 1000) } }
val vCode = "%d%03d%03d".format(vMajor, vMinor, vPatch)
    .toInt(radix = 10)
    .also { require(it < 2100000000) { "Exceeded greatest value for Google Play: $it" } }

android {
    namespace = "io.github.janmalch.volcanicglass"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "io.github.janmalch.volcanicglass"
        minSdk = 24
        targetSdk = 36
        versionName = vName
        versionCode = vCode

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        val release = getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.create("release") {
                storeFile = file("keystore/android_keystore.jks")
                storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("releaseSignDebug") {
            initWith(release)
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.timber)
    implementation(libs.shed)
    implementation(libs.shed.autoload)

    implementation(libs.bundles.coil)

    implementation(libs.collections.immutable)

    implementation(libs.markdown.renderer)
    implementation(libs.markdown.renderer.m3)
    implementation(libs.markdown.renderer.android)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.documentfile)
    implementation(libs.coroutines.android)
    implementation(libs.google.oss.licenses)
    implementation(libs.hilt.common)
    implementation(libs.hilt.android)

    ksp(libs.hilt.compiler)
    ksp(libs.hilt.android.compiler)
    androidTestImplementation(libs.hilt.testing)
    kspAndroidTest(libs.hilt.compiler)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
