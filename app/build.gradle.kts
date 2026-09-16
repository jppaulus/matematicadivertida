import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

// Credenciais de assinatura ficam em keystore.properties, que é ignorado pelo git.
// Este repositório é público: nunca coloque senha de keystore aqui.
// Use keystore.properties.example como modelo.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    FileInputStream(keystorePropertiesFile).use { keystoreProperties.load(it) }
}

android {
    namespace = "com.joaop.matematicadivertida"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.joaop.matematicadivertida"
        minSdk = 24
        targetSdk = 36
        // A versão 22 (1.2.4) foi rejeitada pela Política para Famílias.
        // O Play reserva todo versionCode já enviado: 23 e 30 estão ocupados.
        versionCode = 31
        versionName = "1.2.7"

        // Sem isto o AGP usa o runner legado, que não executa testes JUnit4.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
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
            // Sem keystore.properties (CI, clones do repositório) o release sai sem
            // assinatura em vez de quebrar a build no validateSigningRelease.
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            ndk {
                debugSymbolLevel = "FULL"
            }
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

    testOptions {
        // Testes unitários rodam na JVM: chamadas a android.util.Log viram no-op.
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.fragment:fragment-ktx:1.8.5")

    // Firebase BOM (Bill of Materials) - necessário em ambos builds para resolver versões
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    
    // Firebase Analytics
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // Firebase Crashlytics
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    
    // Firebase Cloud Messaging - necessário em ambos (funcionalidade essencial)
    implementation("com.google.firebase:firebase-messaging-ktx")
    
    // AdMob (Google Mobile Ads)
    implementation("com.google.android.gms:play-services-ads:23.2.0")

    // Consent SDK (User Messaging Platform)
    implementation("com.google.android.ump:user-messaging-platform:2.2.0")

    // Lembrete diário local (retenção)
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Pedido de avaliação dentro do app (Play In-App Review)
    implementation("com.google.android.play:review:2.0.2")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    // androidx.test 1.7/Espresso 3.7: as versões anteriores chamam InputManager.getInstance(),
    // que não existe no Android 16 (API 36), e todo teste de UI Compose falhava no emulador.
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:core:1.7.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("junit:junit:4.13.2")
}
