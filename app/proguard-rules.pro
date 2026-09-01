# R8 Optimization and Shrinking Configuration
-allowaccessmodification
-repackageclasses 'com.joaop.matematicadivertida.opt'

# Preserve annotations and signatures
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep app models / data structures
-keep class com.joaop.matematicadivertida.models.** { *; }

# Remove debug logging in release builds for performance and security
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
