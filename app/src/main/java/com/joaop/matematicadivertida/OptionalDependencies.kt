package com.joaop.matematicadivertida

import android.content.Context
import android.util.Log

/**
 * Wrapper para dependências opcionais que podem não estar presentes em builds debug.
 * Usa reflection e try-catch para evitar ClassNotFoundErrors.
 */
object OptionalDependencies {
    private const val TAG = "OptionalDependencies"

    /**
     * Inicializa Mobile Ads SDK de forma segura em builds onde está disponível.
     */
    fun initMobileAds(context: Context) {
        try {
            val mobileAdsClass = Class.forName("com.google.android.gms.ads.MobileAds")
            val initializeMethod = mobileAdsClass.getMethod("initialize", Context::class.java)
            initializeMethod.invoke(null, context)
            
            val requestConfigClass = Class.forName("com.google.android.gms.ads.RequestConfiguration")
            val builderClass = Class.forName("com.google.android.gms.ads.RequestConfiguration\$Builder")
            
            val testDeviceIds = listOf("EMULATOR")
            val builder = builderClass.getDeclaredConstructor().newInstance()
            val setTestDevicesMethod = builderClass.getMethod("setTestDeviceIds", List::class.java)
            setTestDevicesMethod.invoke(builder, testDeviceIds)
            
            // Política de Famílias (COPPA & Classificação Livre G)
            try {
                val setChildDirectedMethod = builderClass.getMethod("setTagForChildDirectedTreatment", Int::class.javaPrimitiveType)
                setChildDirectedMethod.invoke(builder, 1) // TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE = 1
                val setUnderAgeMethod = builderClass.getMethod("setTagForUnderAgeOfConsent", Int::class.javaPrimitiveType)
                setUnderAgeMethod.invoke(builder, 1) // TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE = 1
                val setMaxRatingMethod = builderClass.getMethod("setMaxAdContentRating", String::class.java)
                setMaxRatingMethod.invoke(builder, "G") // MAX_AD_CONTENT_RATING_G = "G"
            } catch (e: Exception) {
                Log.w(TAG, "Configurações avançadas de RequestConfiguration não aplicadas: ${e.message}")
            }
            
            val buildMethod = builderClass.getMethod("build")
            val configuration = buildMethod.invoke(builder)
            
            val setConfigMethod = mobileAdsClass.getMethod("setRequestConfiguration", requestConfigClass)
            setConfigMethod.invoke(null, configuration)
            
            Log.d(TAG, "✅ Mobile Ads SDK inicializado com sucesso via reflection")
        } catch (e: ClassNotFoundException) {
            Log.d(TAG, "ℹ️ Mobile Ads SDK não disponível em build (debug mode sem play-services-ads)")
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Erro ao inicializar Mobile Ads: ${e.message}")
        }
    }

    /**
     * Solicita consentimento UMP de forma segura.
     */
    fun requestConsent(context: Context, onConsentReady: (Boolean) -> Unit) {
        try {
            val umpClass = Class.forName("com.google.android.ump.ConsentForm")
            val consentInfoClass = Class.forName("com.google.android.ump.ConsentInformation")
            
            // Sem UMP disponível em debug, apenas log
            Log.d(TAG, "ℹ️ Consentimento UMP não disponível em build (debug mode sem user-messaging-platform)")
            onConsentReady(false)
        } catch (e: ClassNotFoundException) {
            Log.d(TAG, "ℹ️ UMP SDK não disponível em build (debug mode)")
            onConsentReady(false)
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Erro ao solicitar consentimento: ${e.message}")
            onConsentReady(false)
        }
    }

    /**
     * Inicializa Firebase Analytics de forma segura.
     */
    fun initFirebaseAnalytics(context: Context) {
        try {
            val firebaseClass = Class.forName("com.google.firebase.ktx.Firebase")
            val analyticsMethod = firebaseClass.getMethod("getAnalytics")
            val analytics = analyticsMethod.invoke(null)
            Log.d(TAG, "✅ Firebase Analytics inicializado com sucesso via reflection")
        } catch (e: ClassNotFoundException) {
            Log.d(TAG, "ℹ️ Firebase Analytics não disponível em build (debug mode)")
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Erro ao inicializar Firebase Analytics: ${e.message}")
        }
    }

    /**
     * Inicializa Firebase Crashlytics de forma segura.
     */
    fun initFirebaseCrashlytics() {
        try {
            val crashlyticsClass = Class.forName("com.google.firebase.crashlytics.FirebaseCrashlytics")
            val getInstanceMethod = crashlyticsClass.getMethod("getInstance")
            val crashlytics = getInstanceMethod.invoke(null)
            Log.d(TAG, "✅ Firebase Crashlytics inicializado com sucesso via reflection")
        } catch (e: ClassNotFoundException) {
            Log.d(TAG, "ℹ️ Firebase Crashlytics não disponível em build (debug mode)")
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Erro ao inicializar Firebase Crashlytics: ${e.message}")
        }
    }

    /**
     * Log um evento no Firebase Analytics de forma segura.
     */
    fun logFirebaseEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        try {
            val firebaseClass = Class.forName("com.google.firebase.ktx.Firebase")
            val analyticsMethod = firebaseClass.getMethod("getAnalytics")
            val analytics = analyticsMethod.invoke(null)
            
            val analyticsClass = analytics?.javaClass
            val logEventMethod = analyticsClass?.getMethod("logEvent", String::class.java, android.os.Bundle::class.java)
            
            val bundle = android.os.Bundle()
            params.forEach { (key, value) ->
                when (value) {
                    is String -> bundle.putString(key, value)
                    is Int -> bundle.putInt(key, value)
                    is Long -> bundle.putLong(key, value)
                    is Double -> bundle.putDouble(key, value)
                    is Boolean -> bundle.putBoolean(key, value)
                }
            }
            
            logEventMethod?.invoke(analytics, eventName, bundle)
            Log.d(TAG, "📊 Evento Firebase '$eventName' logado com sucesso")
        } catch (e: ClassNotFoundException) {
            Log.d(TAG, "ℹ️ Firebase Analytics não disponível para logar eventos")
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Erro ao logar evento Firebase: ${e.message}")
        }
    }
}
