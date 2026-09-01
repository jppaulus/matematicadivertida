// Root build file kept intentionally minimal.
// This helps tooling detect the project as a Gradle (wrapper) build.

plugins {
    id("com.android.application") apply false
    id("org.jetbrains.kotlin.android") apply false
    id("com.google.gms.google-services") apply false
    id("com.google.firebase.crashlytics") apply false
}
