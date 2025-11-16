plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.joaop.matematicadivertida"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.joaop.matematicadivertida"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            storeFile = file("matematica-divertida.jks")
            storePassword = "matematica2024"
            keyAlias = "matematica-divertida-key"
            keyPassword = "matematica2024"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    kotlin {
        jvmToolchain(21)
    }

    // Ensure Java language level for compilation
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    // Configure Kotlin jvm target to match Java toolchain
    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
        kotlinOptions {
            jvmTarget = "21"
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Firebase BOM (Bill of Materials)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    
    // Firebase Analytics
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // Firebase Crashlytics - Rastreamento automático de crashes
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    
    // Firebase Cloud Messaging - Notificações push
    implementation("com.google.firebase:firebase-messaging-ktx")
    
    // Firebase Remote Config - Configurações dinâmicas
    implementation("com.google.firebase:firebase-config-ktx")
    
    // AdMob (Google Mobile Ads)
    implementation("com.google.android.gms:play-services-ads:23.2.0")

    // Consent SDK (User Messaging Platform)
    implementation("com.google.android.ump:user-messaging-platform:2.2.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
