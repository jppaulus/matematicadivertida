# R8 Optimization and Shrinking Configuration
-allowaccessmodification
-repackageclasses 'com.joaop.matematicadivertida.opt'

# Preserve annotations and signatures
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Crashlytics: mantém arquivo e linha nos stack traces. O mapping enviado pelo plugin
# desofusca os nomes, mas sem estes atributos os relatórios chegam sem número de linha.
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Remove debug logging in release builds for performance and security
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
