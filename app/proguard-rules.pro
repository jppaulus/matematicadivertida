# Mantém classes do Google Mobile Ads
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# User Messaging Platform (UMP)
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.ump.**

# Jetpack Compose
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-dontwarn androidx.compose.**

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Data classes e Serialização
-keepclassmembers class * {
    public <init>(...);
}
-keep class com.joaop.matematicadivertida.** { *; }

# Android
-keep class * extends android.app.Activity
-keep class * extends android.app.Application
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider

# Remove logs em produção
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
