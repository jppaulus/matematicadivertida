package com.joaop.matematicadivertida

import android.content.Context
import android.util.Log
import android.view.View
import androidx.compose.runtime.Composable

/**
 * Stub/no-op implementations para tipos de Ads e UMP que não estão disponíveis em builds debug.
 * Isso permite que o código compile mesmo sem as libs.
 */

// Tipos stub para compilação em debug (nunca serão instanciados)
class RewardedAd {
    companion object {
        fun load(context: Context, unitId: String, request: Any, callback: Any) {
            Log.d("JogoInfantil", "Ads desabilitados em modo debug")
        }
    }
    var fullScreenContentCallback: FullScreenContentCallback? = null
    var onUserEarnedRewardListener: ((RewardItem) -> Unit)? = null
    fun show(activity: Any, listener: ((RewardItem) -> Unit)? = null) {}
}

class RewardItem(val type: String = "", val amount: Int = 0)

open class RewardedAdLoadCallback {
    open fun onAdLoaded(ad: RewardedAd) {}
    open fun onAdFailedToLoad(error: LoadAdError) {}
}

class InterstitialAd {
    companion object {
        fun load(context: Context, unitId: String, request: Any, callback: Any) {
            Log.d("JogoInfantil", "Ads desabilitados em modo debug")
        }
    }
    var fullScreenContentCallback: FullScreenContentCallback? = null
    fun show(activity: Any) {}
}

open class InterstitialAdLoadCallback {
    open fun onAdLoaded(ad: InterstitialAd) {}
    open fun onAdFailedToLoad(error: LoadAdError) {}
}

class AdView(context: Context? = null, attrs: Any? = null) : View(context) {
    init {
        Log.d("JogoInfantil", "AdView criado (stub em debug)")
    }
    fun setAdSize(size: Any) {}
    var adUnitId: String = ""
    var adListener: AdListener? = null
    fun loadAd(request: Any) {}
}

open class AdListener {
    open fun onAdLoaded() {}
    open fun onAdFailedToLoad(error: LoadAdError) {}
    open fun onAdClicked() {}
    open fun onAdOpened() {}
    open fun onAdClosed() {}
}

open class FullScreenContentCallback {
    open fun onAdDismissedFullScreenContent() {}
    open fun onAdFailedToShowFullScreenContent(error: AdError) {}
    open fun onAdShowedFullScreenContent() {}
}

class AdError(val code: Int = 0, val message: String = "", val domain: String = "", val cause: Exception? = null) {
    override fun toString() = "AdError: $message"
}

class LoadAdError(val code: Int = 0, val message: String = "", val domain: String = "", val cause: Exception? = null) {
    override fun toString() = "LoadAdError: $message"
}

class AdSize {
    companion object {
        val BANNER: AdSize = AdSize()
        val MEDIUM_RECTANGLE: AdSize = AdSize()
    }
}

class AdRequest {
    companion object {
        const val DEVICE_ID_EMULATOR = "EMULATOR"
    }
    
    class Builder {
        fun build(): AdRequest = AdRequest()
    }
}

object MobileAds {
    fun initialize(context: Context, callback: ((InitializationStatus) -> Unit)? = null) {
        Log.d("JogoInfantil", "Mobile Ads não inicializado (debug mode)")
        callback?.invoke(InitializationStatus())
    }
    fun setRequestConfiguration(config: RequestConfiguration) {
        Log.d("JogoInfantil", "RequestConfiguration definida (stub em debug)")
    }
}

class InitializationStatus {
    val adapterStatusMap: Map<String, AdapterStatus> = emptyMap()
}

class AdapterStatus {
    val initializationState: String = "NOT_READY"
    val description: String = "Adapter not loaded"
}

object UserMessagingPlatform {
    fun getConsentInformation(context: Context): ConsentInformation {
        return ConsentInformation.stub()
    }
    
    fun requestConsentInfoUpdate(context: Context, parameters: ConsentRequestParameters, onSuccess: (ConsentInformation) -> Unit, onError: (Exception) -> Unit) {
        onSuccess(ConsentInformation.stub())
    }
    
    fun loadAndShowConsentFormIfRequired(activity: Context, callback: (Exception?) -> Unit) {
        callback(null)
    }
}

class ConsentInformation {
    fun canRequestAds(): Boolean = true
    val isConsentFormAvailable: Boolean = false
    
    fun requestConsentInfoUpdate(context: Context, parameters: ConsentRequestParameters, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        onSuccess()
    }
    
    fun loadAndShowConsentFormIfRequired(activity: Context, callback: (Exception?) -> Unit) {
        callback(null)
    }
    
    companion object {
        fun stub() = ConsentInformation()
    }
}

class ConsentRequestParameters {
    class Builder {
        fun setTagForUnderAgeOfConsent(tag: Boolean) = this
        fun build() = ConsentRequestParameters()
    }
}

class RequestConfiguration {
    class Builder {
        fun setTestDeviceIds(ids: List<String>) = this
        fun build() = RequestConfiguration()
    }
}

// Versão completa com requestConsentInfoUpdate
object ConsentInfo {
    fun requestConsentInfoUpdate(context: Context, parameters: ConsentRequestParameters, onSuccess: (ConsentInformation) -> Unit, onError: (Exception) -> Unit) {
        onSuccess(ConsentInformation.stub())
    }
}

// Composable stub para anúncios banner em debug
@Composable
fun BannerAdStub() {
    Log.d("JogoInfantil", "Banner ad desabilitado em modo debug")
}
