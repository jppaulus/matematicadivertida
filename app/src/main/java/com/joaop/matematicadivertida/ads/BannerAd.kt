package com.joaop.matematicadivertida

import android.Manifest
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.drawscope.Fill
import kotlin.random.Random
import android.util.Log
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.compose.material3.Switch
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.Build
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdListener
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Único formato de anúncio do app.
 *
 * A Política para Famílias proíbe anúncios que interfiram no uso do app, então aqui só
 * existe banner ancorado — nunca intersticial, tela cheia ou recompensado. Ele é
 * renderizado na bottomBar do Scaffold, que reserva o espaço e evita sobreposição.
 */
@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val consentInfo = remember { UserMessagingPlatform.getConsentInformation(context) }
    if (!consentInfo.canRequestAds() && !MainActivity.canShowAds) {
        Log.w("JogoInfantil", "⚠️ Banner: consentimento não disponível, anúncio não será carregado")
        Spacer(modifier = Modifier.height(0.dp))
        return
    }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            Log.d("JogoInfantil", "📥 Carregando banner...")
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = ctx.getString(R.string.admob_banner_id)
                Log.d("JogoInfantil", "🆔 Banner ID: ${ctx.getString(R.string.admob_banner_id)}")
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Log.d("JogoInfantil", "✅ Banner carregado com sucesso")
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.e("JogoInfantil", "❌ Falha ao carregar banner")
                        Log.e("JogoInfantil", "   Código: ${error.code}")
                        Log.e("JogoInfantil", "   Mensagem: ${error.message}")
                        Log.e("JogoInfantil", "   Domínio: ${error.domain}")
                        Log.e("JogoInfantil", "   Causa: ${error.cause}")
                    }
                    override fun onAdClicked() {
                        Log.d("JogoInfantil", "👆 Banner clicado")
                    }
                    override fun onAdOpened() {
                        Log.d("JogoInfantil", "📖 Banner aberto")
                    }
                    override fun onAdClosed() {
                        Log.d("JogoInfantil", "📕 Banner fechado")
                    }
                }
                val adRequest = AdRequest.Builder()
                    .build()
                Log.d("JogoInfantil", "🔄 Iniciando carregamento do banner...")
                loadAd(adRequest)
            }
        },
        // Cada troca de tela recria o banner; sem destroy() o AdView (e sua WebView) vazava.
        onRelease = { it.destroy() }
    )
}
