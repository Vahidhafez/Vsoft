plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.vsoft.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vsoft.app"
        minSdk = 26
        targetSdk = 35
        versionCode = providers.gradleProperty("VSOFT_VERSION_CODE").orElse("2").get().toInt()
        versionName = providers.gradleProperty("VSOFT_VERSION_NAME").orElse("1.0").get()
    }

    val signingFile = file("signing/vsoft-release.jks")
    val signingEnabled = System.getenv("VSOFT_SIGNING_ENABLED") == "true" && signingFile.exists()

    if (signingEnabled) {
        signingConfigs {
            create("vsoft") {
                storeFile = signingFile
                storePassword = System.getenv("KSTOREPWD")
                keyAlias = System.getenv("KEYALIAS")
                keyPassword = System.getenv("KEYPWD")
            }
        }
    }

    buildTypes {
        debug {
            if (signingEnabled) {
                signingConfig = signingConfigs.getByName("vsoft")
            }
        }
        release {
            isMinifyEnabled = false
            if (signingEnabled) {
                signingConfig = signingConfigs.getByName("vsoft")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.google.firebase:firebase-firestore")
}
