import java.io.ByteArrayOutputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    alias(libs.plugins.ksp)
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.kotlin.serialization) // aded for json
}

val configFile = rootProject.file("config/app-config.properties")
val props = Properties()
props.load(configFile.inputStream())

val dbVersion = props["DB_VERSION"].toString().toInt()

fun gitTagVersion(): String {
    return try {
        val stdout = ByteArrayOutputStream()
        exec {
            commandLine("git", "describe", "--tags", "--abbrev=0")
            standardOutput = stdout
        }
        stdout.toString().trim().removePrefix("v")
    } catch (e: Exception) {
        "1"
    }
}

val tagVersion = gitTagVersion()
val numericVersion = tagVersion.filter { it.isDigit() }.toIntOrNull() ?: 1


android {
    namespace = "com.example.axiom"
    compileSdk {
        version = release(36)
    }
    signingConfigs {
        create("release") { // Use create() for Kotlin DSL
            storeFile = file("axiom-release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    defaultConfig {
        applicationId = "com.example.axiom"
        minSdk = 26
        targetSdk = 36
        versionCode = numericVersion
        versionName = tagVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("int", "DB_VERSION", dbVersion.toString())
    }

    buildTypes {

        getByName("release") {
            // Reference the signing config we created above
            signingConfig = signingConfigs.getByName("release")

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        create("profile") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}
// Add this at the very bottom of the file
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}


dependencies {
    implementation("androidx.compose.material:material")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)


    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Calender
    implementation("com.kizitonwose.calendar:compose:2.8.0")

    //dagger
    implementation("com.google.dagger:hilt-android:2.57.1")
    ksp("com.google.dagger:hilt-compiler:2.57.1")
    implementation("javax.inject:javax.inject:1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    //pdf generator lib
    implementation("com.github.UttamPanchasara:PDF-Generator:2.0.0")

    // for json converiosn
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    // for http req
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}