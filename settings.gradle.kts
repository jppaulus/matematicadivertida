pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        id("com.android.application") version "8.5.2"
        // Versão estável compatível com Compose Compiler 1.5.15
        id("org.jetbrains.kotlin.android") version "1.9.25"
        // Google Services para Firebase
        id("com.google.gms.google-services") version "4.4.2" apply false
        // Firebase Crashlytics
        id("com.google.firebase.crashlytics") version "3.0.2" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "JogoInfantil"
include(":app")