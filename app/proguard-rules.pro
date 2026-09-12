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

# WorkManager guarda o NOME da classe do Worker no banco dele e a instancia por reflexão.
# Com -repackageclasses ligado, esse nome muda a cada build; sem este keep, um lembrete
# agendado por uma versão antiga do app deixaria de ser encontrado depois de atualizar.
-keep class com.joaop.matematicadivertida.DailyReminderWorker { *; }
