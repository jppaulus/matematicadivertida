package com.joaop.matematicadivertida

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.random.Random
import com.google.android.ump.*
import android.util.Log
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.Build
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import java.text.SimpleDateFormat
import java.util.*

private val AppBackgroundColor = Color(0xFFD6E9FC) // Slightly deeper blue for better contrast

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "JogoInfantil"
        private val allowAdsState = mutableStateOf(false)
        var allowAdsWithoutConsent: Boolean
            get() = allowAdsState.value
            set(value) {
                allowAdsState.value = value
            }
    }

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var crashlytics: FirebaseCrashlytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "🎮 Iniciando aplicativo...")
        allowAdsWithoutConsent = false
        
        // Inicializar Firebase Analytics
        firebaseAnalytics = Firebase.analytics
        crashlytics = FirebaseCrashlytics.getInstance()
        
        // Log de inicialização
        firebaseAnalytics.logEvent("app_start") {
            param("screen", "main")
        }
        Log.d(TAG, "🔥 Firebase Analytics e Crashlytics inicializados")
        
        // Splash API (Android 12+)
        installSplashScreen()
        enableEdgeToEdge()

        // Solicita consentimento (GDPR/COPPA) otimizado para Brasil
        Log.d(TAG, "📋 Solicitando consentimento UMP...")
        requestConsent()

        // Inicialização do Mobile Ads pode ocorrer de imediato; o carregamento
        // de anúncios é condicionado por canRequestAds() nas views.
        Log.d(TAG, "📢 Inicializando Mobile Ads SDK...")
        MobileAds.initialize(this) { initializationStatus ->
            Log.d(TAG, "✅ Mobile Ads SDK inicializado com sucesso")
            val statusMap = initializationStatus.adapterStatusMap
            for (adapterClass in statusMap.keys) {
                val status = statusMap[adapterClass]
                Log.d(TAG, "Adapter: $adapterClass - Status: ${status?.initializationState} - Descrição: ${status?.description}")
            }
        }
        
        // Configurar dispositivo como teste para garantir anúncios
        val testDeviceIds = listOf(AdRequest.DEVICE_ID_EMULATOR)
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDeviceIds)
            .build()
        MobileAds.setRequestConfiguration(configuration)
        Log.d(TAG, "🧪 Dispositivo configurado como teste para anúncios")

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = AppBackgroundColor) {
                    GameApp()
                }
            }
        }
    }
}

// Minimal interactive visuals used by SolutionDialog (NumberLine & BlocksGrid)
@Composable
fun NumberLine(maxValue: Int, highlighted: Int, startOffset: Int = 0, modifier: Modifier = Modifier, onTickClick: ((Int) -> Unit)? = null) {
    if (maxValue < startOffset) return
    val ticks = (maxValue - startOffset + 1).coerceAtMost(24)
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        (startOffset..maxValue).take(ticks).forEach { value ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Canvas(modifier = Modifier.size(24.dp)) {
                    val color = if (value == highlighted) Color(0xFF4CAF50) else Color(0xFFBDBDBD)
                    drawCircle(color = color, radius = size.minDimension / 2, style = Fill)
                }
                Text(value.toString(), fontSize = 12.sp, textAlign = TextAlign.Center,
                    modifier = if (onTickClick != null) Modifier.testTag("numberLineTick_$value").clickable { onTickClick(value) } else Modifier)
            }
        }
    }
}

@Composable
fun BlocksGrid(rows: Int, cols: Int, highlightCols: Int, modifier: Modifier = Modifier, onColClick: ((Int) -> Unit)? = null) {
    if (rows <= 0 || cols <= 0) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) { Text("", fontSize = 12.sp) }
        return
    }
    val safeRows = rows.coerceIn(1, 12)
    val safeCols = cols.coerceIn(1, 12)
    val selectedCols = remember { mutableStateListOf<Boolean>().apply { for (i in 0 until safeCols) add(false) } }
    Column(modifier = modifier) {
        for (r in 0 until safeRows) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(2.dp)) {
                for (c in 0 until safeCols) {
                    val active = c < highlightCols
                    val selected = selectedCols.getOrElse(c) { false }
                    val bgColor = if (selected || active) Color(0xFF4CAF50) else Color(0xFFEEEEEE)
                    Box(
                        modifier = (
                            if (onColClick != null) Modifier.testTag("blocksGridCol_$c").clickable {
                                selectedCols[c] = !selectedCols[c]
                                val totalSelected = selectedCols.count { it }
                                try { onColClick(totalSelected) } catch (_: Exception) {}
                            } else Modifier
                        ).size(24.dp).background(bgColor, RoundedCornerShape(4.dp))
                    ) {}
                }
            }
        }
    }
}

@Composable
fun GameApp() {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("JogoInfantil", Context.MODE_PRIVATE) }
    
    val totalLevels = Int.MAX_VALUE // Fases infinitas!
    var level by rememberSaveable { mutableIntStateOf(prefs.getInt("level", 1)) }
    var correctThisLevel by rememberSaveable { mutableIntStateOf(0) }
    var wrong by rememberSaveable { mutableIntStateOf(prefs.getInt("wrong", 0)) }
    var lives by rememberSaveable { mutableIntStateOf(3) }
    var showHint by remember { mutableStateOf(false) }
    var hintsUsed by rememberSaveable { mutableIntStateOf(0) }
    
    // Sistema de adaptação de dificuldade
    var totalCorrect by rememberSaveable { mutableIntStateOf(prefs.getInt("totalCorrect", 0)) }
    var totalWrong by rememberSaveable { mutableIntStateOf(prefs.getInt("totalWrong", 0)) }
    var consecutiveCorrect by rememberSaveable { mutableIntStateOf(prefs.getInt("consecutiveCorrect", 0)) }
    var consecutiveWrong by rememberSaveable { mutableIntStateOf(0) }
    
    // Gamificação: XP e Moedas
    var xp by rememberSaveable { mutableIntStateOf(prefs.getInt("xp", 0)) }
    var coins by rememberSaveable { mutableIntStateOf(prefs.getInt("coins", 0)) }
    var playerLevel by rememberSaveable { mutableIntStateOf(prefs.getInt("playerLevel", 1)) }
    
    // Estatísticas por operação
    var addStats by remember { mutableStateOf(GameDataManager.loadOperationStats(prefs, "add")) }
    var subStats by remember { mutableStateOf(GameDataManager.loadOperationStats(prefs, "sub")) }
    var mulStats by remember { mutableStateOf(GameDataManager.loadOperationStats(prefs, "mul")) }
    var divStats by remember { mutableStateOf(GameDataManager.loadOperationStats(prefs, "div")) }
    
    // Conquistas
    var achievements by remember { mutableStateOf(GameDataManager.loadAchievements(prefs)) }
    
    // UI States
    var showStats by remember { mutableStateOf(false) }
    var showAchievements by remember { mutableStateOf(false) }
    var showTrainingMode by remember { mutableStateOf(false) }
    var trainingOp by remember { mutableStateOf<Op?>(null) }
    var showFeedbackAnimation by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    var feedbackEmoji by remember { mutableStateOf("") }
    var feedbackIsCorrect by remember { mutableStateOf(true) }
    var questionStartTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    // Desafio diário
    var dailyChallenge by remember { mutableStateOf(GameDataManager.loadDailyChallenge(prefs)) }
    
    // Sons e vibração
    val vibrator = remember { 
        try {
            ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } catch (e: Exception) {
            Log.e("JogoInfantil", "Erro ao obter vibrator: ${e.message}")
            null
        }
    }
    
    fun playSound(isCorrect: Boolean) {
        // Sons serão implementados com recursos de áudio
        try {
            if (isCorrect && vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(100)
                }
            }
        } catch (e: Exception) {
            Log.e("JogoInfantil", "Erro na vibração: ${e.message}")
        }
    }
    
    // Salvar progresso sempre que mudar
    LaunchedEffect(level, totalCorrect, totalWrong, consecutiveCorrect, wrong) {
        prefs.edit().apply {
            putInt("level", level)
            putInt("totalCorrect", totalCorrect)
            putInt("totalWrong", totalWrong)
            putInt("consecutiveCorrect", consecutiveCorrect)
            putInt("wrong", wrong)
            apply()
        }
        Log.d("JogoInfantil", "💾 Progresso salvo: Fase $level, Acertos $totalCorrect, Erros $totalWrong")
    }
    
    val config = remember(level, totalCorrect, totalWrong) { 
        generateAdaptiveLevel(level, totalCorrect, totalWrong, consecutiveCorrect) 
    }

    var question by remember(level, correctThisLevel) { mutableStateOf(generateQuestion(config)) }
    var showCompleted by remember { mutableStateOf(false) }
    var showGameOver by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var interstitialAd by remember { mutableStateOf<InterstitialAd?>(null) }
    var rewardedAd by remember { mutableStateOf<RewardedAd?>(null) }
    var showRewardedAdDialog by remember { mutableStateOf(false) }

    fun loadInterstitial() {
        val consentInfo = UserMessagingPlatform.getConsentInformation(context)
        if (!consentInfo.canRequestAds() && !MainActivity.allowAdsWithoutConsent) {
            Log.w("JogoInfantil", "⚠️ Intersticial: Consentimento não disponível")
            return
        }
        Log.d("JogoInfantil", "📥 Carregando anúncio intersticial...")
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            context.getString(R.string.admob_interstitial_id),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d("JogoInfantil", "✅ Intersticial carregado com sucesso")
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdShowedFullScreenContent() {
                            Log.d("JogoInfantil", "👁️ Intersticial exibido")
                        }
                        override fun onAdDismissedFullScreenContent() {
                            Log.d("JogoInfantil", "✖️ Intersticial fechado pelo usuário")
                            interstitialAd = null
                            loadInterstitial()
                        }
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e("JogoInfantil", "❌ Falha ao exibir intersticial: ${adError.message}")
                            interstitialAd = null
                        }
                    }
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    Log.e("JogoInfantil", "❌ Falha ao carregar intersticial: ${error.message}")
                }
            }
        )
    }

    fun loadRewardedAd() {
        val consentInfo = UserMessagingPlatform.getConsentInformation(context)
        if (!consentInfo.canRequestAds() && !MainActivity.allowAdsWithoutConsent) {
            Log.w("JogoInfantil", "⚠️ Recompensado: Consentimento não disponível")
            return
        }
        Log.d("JogoInfantil", "📥 Carregando anúncio recompensado...")
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            context.getString(R.string.admob_rewarded_id),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d("JogoInfantil", "✅ Anúncio recompensado carregado")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    Log.e("JogoInfantil", "❌ Falha ao carregar recompensado: ${error.message}")
                }
            }
        )
    }

    LaunchedEffect(MainActivity.allowAdsWithoutConsent) {
        val consentInfo = UserMessagingPlatform.getConsentInformation(context)
        if (consentInfo.canRequestAds() || MainActivity.allowAdsWithoutConsent) {
            loadInterstitial()
            loadRewardedAd()
        } else {
            Log.w("JogoInfantil", "⚠️ Aguardando consentimento para carregar anúncios")
        }
    }

    if (showGameOver) {
        GameOverDialog(
            level = level,
            correctAnswers = correctThisLevel,
            onRestart = {
                showGameOver = false
                level = 1
                correctThisLevel = 0
                wrong = 0
                lives = 3
                hintsUsed = 0
                totalCorrect = 0
                totalWrong = 0
                consecutiveCorrect = 0
                consecutiveWrong = 0
                // Limpar progresso salvo
                prefs.edit().clear().apply()
                Log.d("JogoInfantil", "🔄 Progresso resetado - começando do zero")
            },
            onWatchAd = {
                showGameOver = false
                if (rewardedAd != null) {
                    Log.d("JogoInfantil", "🎬 Exibindo anúncio recompensado para continuar...")
                    rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d("JogoInfantil", "✅ Anúncio recompensado fechado (continuar)")
                            rewardedAd = null
                            loadRewardedAd()
                        }
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e("JogoInfantil", "❌ Falha ao exibir recompensado: ${adError.message}")
                            rewardedAd = null
                            loadRewardedAd()
                        }
                    }
                    rewardedAd?.show(context as Activity) { rewardItem ->
                        Log.d("JogoInfantil", "🎁 Recompensa concedida: continuar jogando")
                        lives = 3 // Restaura 3 vidas
                        wrong = 0
                    }
                } else {
                    Log.w("JogoInfantil", "⚠️ Anúncio recompensado não disponível")
                    // Fallback: reinicia do início
                    level = 1
                    correctThisLevel = 0
                    wrong = 0
                    lives = 3
                    hintsUsed = 0
                    totalCorrect = 0
                    totalWrong = 0
                    consecutiveCorrect = 0
                    consecutiveWrong = 0
                }
            },
            hasRewardedAd = rewardedAd != null
        )
    }

    if (showCompleted) {
        LevelCompletedDialog(
            level = level,
            totalLevels = totalLevels,
            onNext = {
                showCompleted = false
                if (level < totalLevels) {
                    if (interstitialAd != null && level % 3 == 0) { // Intersticial a cada 3 fases
                        Log.d("JogoInfantil", "🎬 Exibindo intersticial antes da fase ${level + 1}")
                        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                Log.d("JogoInfantil", "➡️ Avançando para fase ${level + 1}")
                                interstitialAd = null
                                loadInterstitial()
                                level += 1
                                correctThisLevel = 0
                                wrong = 0
                                lives = 3
                                hintsUsed = 0
                                showHint = false
                            }
                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                Log.e("JogoInfantil", "⚠️ Intersticial falhou, avançando mesmo assim")
                                interstitialAd = null
                                level += 1
                                correctThisLevel = 0
                                wrong = 0
                                lives = 3
                                hintsUsed = 0
                                showHint = false
                            }
                        }
                        interstitialAd?.show(context as Activity)
                    } else {
                        Log.w("JogoInfantil", "⏭️ Intersticial não carregado, avançando para fase ${level + 1}")
                        level += 1
                        correctThisLevel = 0
                        wrong = 0
                        lives = 3
                        hintsUsed = 0
                        showHint = false
                    }
                } else {
                    // Recomeçar após a última fase
                    Log.d("JogoInfantil", "🎉 Todas as fases concluídas! Reiniciando jogo...")
                    level = 1
                    correctThisLevel = 0
                    wrong = 0
                    lives = 3
                    hintsUsed = 0
                    showHint = false
                }
            }
        )
    }

    Scaffold(
        containerColor = AppBackgroundColor,
        bottomBar = {
            // Banner Ad na parte inferior
            BannerAdView(modifier = Modifier.fillMaxWidth())
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabeçalho da fase
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEB3B)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(48.dp))
                        Text(
                            text = "📚 Fase $level${if (level > 30) " (Infinita)" else " de 30"}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2)
                            )
                        )
                        Button(
                            onClick = {
                                level = 1
                                correctThisLevel = 0
                                wrong = 0
                                lives = 3
                                hintsUsed = 0
                                totalCorrect = 0
                                totalWrong = 0
                                consecutiveCorrect = 0
                                consecutiveWrong = 0
                                prefs.edit().clear().apply()
                                Log.d("JogoInfantil", "🔄 Jogo resetado manualmente")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                            modifier = Modifier.size(48.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("🔄", fontSize = 20.sp)
                        }
                    }
                    Text(
                        text = config.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { correctThisLevel / config.targetCorrect.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp),
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0xFFE0E0E0),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(3) { index ->
                                Text(
                                    text = if (index < lives) "❤️" else "🖤",
                                    fontSize = 20.sp
                                )
                            }
                        }
                        Text(
                            text = "✅ $correctThisLevel/${config.targetCorrect}",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4CAF50)
                            )
                        )
                        Text(
                            text = "💡 Dicas: ${3 - hintsUsed}",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFF9800)
                            )
                        )
                    }
                }
            }

            // Barra de informações XP e Moedas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚡", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$xp XP", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800)),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🪙", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$coins", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF9C27B0)),
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏅", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nível $playerLevel", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            // Botões de menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showStats = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text("📊")
                }
                Button(
                    onClick = { showAchievements = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text("🏆")
                }
                Button(
                    onClick = { showTrainingMode = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("🎓")
                }
            }
            
            // Desafio diário
            DailyChallengeCard(
                challenge = dailyChallenge,
                onClick = { /* Pode implementar navegação para desafio */ }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sistema de dicas
            if (showHint) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE082)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "💡", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dica:",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6D4C41)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = getSmartHint(question),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 15.sp,
                                color = Color(0xFF5D4037)
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Pergunta
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = question.text,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                    )
                }
            }

            // Botão de dica
            if (!showHint) {
                if (hintsUsed < 3) {
                    OutlinedButton(
                        onClick = {
                            showHint = true
                            hintsUsed += 1
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFF9800)
                        )
                    ) {
                        Text(
                            text = "💡 Ver Dica (${3 - hintsUsed} restantes)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    // Botão para assistir anúncio e ganhar mais dicas
                    Button(
                        onClick = {
                            showRewardedAdDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800)
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📺 Assistir Anúncio = +3 Dicas",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Opções de resposta
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                question.options.forEach { option ->
                    Button(
                        onClick = {
                            val responseTime = System.currentTimeMillis() - questionStartTime
                            
                            if (option == question.correct) {
                                // ACERTOU! 🎉
                                correctThisLevel += 1
                                totalCorrect += 1
                                consecutiveCorrect += 1
                                consecutiveWrong = 0
                                
                                // Ganhar XP e Moedas
                                val xpGain = 10 + (consecutiveCorrect * 2)
                                xp += xpGain
                                coins += 5
                                
                                // Atualizar estatísticas da operação
                                val opKey = when {
                                    question.text.contains("+") -> "add"
                                    question.text.contains("-") -> "sub"
                                    question.text.contains("×") -> "mul"
                                    question.text.contains("÷") -> "div"
                                    else -> "add"
                                }
                                
                                when (opKey) {
                                    "add" -> {
                                        addStats = addStats.copy(
                                            correct = addStats.correct + 1,
                                            totalTime = addStats.totalTime + responseTime,
                                            count = addStats.count + 1
                                        )
                                        GameDataManager.saveOperationStats(prefs, "add", addStats)
                                    }
                                    "sub" -> {
                                        subStats = subStats.copy(
                                            correct = subStats.correct + 1,
                                            totalTime = subStats.totalTime + responseTime,
                                            count = subStats.count + 1
                                        )
                                        GameDataManager.saveOperationStats(prefs, "sub", subStats)
                                    }
                                    "mul" -> {
                                        mulStats = mulStats.copy(
                                            correct = mulStats.correct + 1,
                                            totalTime = mulStats.totalTime + responseTime,
                                            count = mulStats.count + 1
                                        )
                                        GameDataManager.saveOperationStats(prefs, "mul", mulStats)
                                    }
                                    "div" -> {
                                        divStats = divStats.copy(
                                            correct = divStats.correct + 1,
                                            totalTime = divStats.totalTime + responseTime,
                                            count = divStats.count + 1
                                        )
                                        GameDataManager.saveOperationStats(prefs, "div", divStats)
                                    }
                                }
                                
                                // Verificar conquistas
                                val newAchievements = GameDataManager.checkAndUnlockAchievements(
                                    prefs, totalCorrect, level, consecutiveCorrect, wrong,
                                    addStats, subStats, mulStats, divStats
                                )
                                if (newAchievements.isNotEmpty()) {
                                    achievements = GameDataManager.loadAchievements(prefs)
                                }
                                
                                // Feedback visual e vibração
                                playSound(true)
                                feedbackMessage = when {
                                    consecutiveCorrect >= 10 -> "IMPARÁVEL!"
                                    consecutiveCorrect >= 5 -> "EM CHAMA!"
                                    responseTime < 3000 -> "RÁPIDO!"
                                    else -> "MUITO BEM!"
                                }
                                feedbackEmoji = when {
                                    consecutiveCorrect >= 10 -> "⚡"
                                    consecutiveCorrect >= 5 -> "🔥"
                                    responseTime < 3000 -> "⚡"
                                    else -> "🎉"
                                }
                                feedbackIsCorrect = true
                                showFeedbackAnimation = true
                                
                                // Salvar XP e moedas
                                prefs.edit().apply {
                                    putInt("xp", xp)
                                    putInt("coins", coins)
                                    apply()
                                }
                                
                                showHint = false
                                if (correctThisLevel >= config.targetCorrect) {
                                    showCompleted = true
                                } else {
                                    question = generateQuestion(config)
                                    questionStartTime = System.currentTimeMillis()
                                }
                            } else {
                                // ERROU 😢
                                wrong += 1
                                totalWrong += 1
                                lives -= 1
                                consecutiveWrong += 1
                                consecutiveCorrect = 0
                                
                                // Atualizar estatísticas de erro
                                val opKey = when {
                                    question.text.contains("+") -> "add"
                                    question.text.contains("-") -> "sub"
                                    question.text.contains("×") -> "mul"
                                    question.text.contains("÷") -> "div"
                                    else -> "add"
                                }
                                
                                when (opKey) {
                                    "add" -> {
                                        addStats = addStats.copy(wrong = addStats.wrong + 1)
                                        GameDataManager.saveOperationStats(prefs, "add", addStats)
                                    }
                                    "sub" -> {
                                        subStats = subStats.copy(wrong = subStats.wrong + 1)
                                        GameDataManager.saveOperationStats(prefs, "sub", subStats)
                                    }
                                    "mul" -> {
                                        mulStats = mulStats.copy(wrong = mulStats.wrong + 1)
                                        GameDataManager.saveOperationStats(prefs, "mul", mulStats)
                                    }
                                    "div" -> {
                                        divStats = divStats.copy(wrong = divStats.wrong + 1)
                                        GameDataManager.saveOperationStats(prefs, "div", divStats)
                                    }
                                }
                                
                                // Feedback de erro
                                feedbackMessage = "TENTE NOVAMENTE!"
                                feedbackEmoji = "😢"
                                feedbackIsCorrect = false
                                showFeedbackAnimation = true
                                
                                if (lives <= 0) {
                                    showGameOver = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text(
                            text = option.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    
    // Diálogos das novas funcionalidades
    if (showStats) {
        StatsScreen(
            addStats = addStats,
            subStats = subStats,
            mulStats = mulStats,
            divStats = divStats,
            totalCorrect = totalCorrect,
            totalWrong = totalWrong,
            level = level,
            xp = xp,
            playerLevel = playerLevel,
            onDismiss = { showStats = false }
        )
    }
    
    if (showAchievements) {
        AchievementsScreen(
            achievements = achievements,
            onDismiss = { showAchievements = false }
        )
    }
    
    if (showTrainingMode) {
        TrainingModeSelector(
            onSelectOperation = { op ->
                trainingOp = op
                showTrainingMode = false
                // Implementar navegação para modo treino
            },
            onDismiss = { showTrainingMode = false }
        )
    }
    
    // Animação de feedback
    FeedbackAnimation(
        show = showFeedbackAnimation,
        message = feedbackMessage,
        emoji = feedbackEmoji,
        isCorrect = feedbackIsCorrect,
        onDismiss = { showFeedbackAnimation = false }
    )
}

@Composable
fun LevelCompletedDialog(level: Int, totalLevels: Int, onNext: () -> Unit) {
    val isInfiniteMode = level > 30
    AlertDialog(
        onDismissRequest = onNext,
        title = {
            Text(
                text = when {
                    level == 30 -> "🎉 Parabéns, Mestre!"
                    isInfiniteMode -> "⭐ Fase $level Completa!"
                    else -> "⭐ Fase Concluída!"
                },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
            )
        },
        text = {
            Text(
                text = when {
                    level == 30 -> "Você completou todas as 30 fases base!\n\nAgora o jogo vai gerar fases infinitas cada vez mais desafiadoras! 🚀"
                    isInfiniteMode -> "Você está no modo infinito!\nContinue evoluindo! 💪"
                    else -> "Ótimo trabalho!\nVamos para a fase ${level + 1}! 🚀"
                },
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
            )
        },
        confirmButton = {
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isInfiniteMode) "➡️ Continuar" else "➡️ Próxima",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = Color(0xFFFFF8E1),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun GameOverDialog(
    level: Int, 
    correctAnswers: Int, 
    onRestart: () -> Unit,
    onWatchAd: () -> Unit,
    hasRewardedAd: Boolean
) {
    AlertDialog(
        onDismissRequest = onRestart,
        title = {
            Text(
                text = "😢 Fim de Jogo",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF44336)
                )
            )
        },
        text = {
            Column {
                Text(
                    text = "Você chegou até a fase $level!",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (hasRewardedAd) {
                    Text(
                        text = "Assista a um anúncio e ganhe mais 3 vidas para continuar! 💪",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                } else {
                    Text(
                        text = "Continue praticando e você vai melhorar! 💪",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (hasRewardedAd) {
                    Button(
                        onClick = onWatchAd,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "📺 Continuar (+3 Vidas)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "🔄 Reiniciar do Início",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = Color(0xFFFFF8E1),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val consentInfo = remember { UserMessagingPlatform.getConsentInformation(context) }
    if (!consentInfo.canRequestAds() && !MainActivity.allowAdsWithoutConsent) {
        Log.w("JogoInfantil", "⚠️ Banner: Consentimento não disponível")
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
        }
    )
}

// Funções auxiliares para carregar dados
fun loadOperationStats(prefs: SharedPreferences, op: String): OperationStats {
    return OperationStats(
        correct = prefs.getInt("${op}_correct", 0),
        wrong = prefs.getInt("${op}_wrong", 0),
        totalTime = prefs.getLong("${op}_time", 0),
        count = prefs.getInt("${op}_count", 0)
    )
}

fun saveOperationStats(prefs: SharedPreferences, op: String, stats: OperationStats) {
    prefs.edit().apply {
        putInt("${op}_correct", stats.correct)
        putInt("${op}_wrong", stats.wrong)
        putLong("${op}_time", stats.totalTime)
        putInt("${op}_count", stats.count)
        apply()
    }
}

fun loadAchievements(prefs: SharedPreferences): List<Achievement> {
    val unlocked = prefs.getStringSet("achievements", emptySet()) ?: emptySet()
    return listOf(
        Achievement("first_correct", "Primeira Acerto", "Acertou sua primeira questão!", "🎯", "first_correct" in unlocked),
        Achievement("ten_correct", "Iniciante", "10 questões corretas!", "⭐", "ten_correct" in unlocked),
        Achievement("fifty_correct", "Aprendiz", "50 questões corretas!", "🌟", "fifty_correct" in unlocked),
        Achievement("hundred_correct", "Mestre", "100 questões corretas!", "🏆", "hundred_correct" in unlocked),
        Achievement("perfect_level", "Perfeito!", "Completou uma fase sem erros!", "💯", "perfect_level" in unlocked),
        Achievement("five_consecutive", "Em Chama!", "5 acertos seguidos!", "🔥", "five_consecutive" in unlocked),
        Achievement("ten_consecutive", "Imparável!", "10 acertos seguidos!", "⚡", "ten_consecutive" in unlocked),
        Achievement("level_10", "Progresso", "Alcançou a fase 10!", "📚", "level_10" in unlocked),
        Achievement("level_20", "Dedicado", "Alcançou a fase 20!", "📖", "level_20" in unlocked),
        Achievement("level_30", "Infinito!", "Alcançou a fase 30!", "♾️", "level_30" in unlocked),
        Achievement("master_add", "Mestre da Adição", "100 adições corretas!", "➕", "master_add" in unlocked),
        Achievement("master_sub", "Mestre da Subtração", "100 subtrações corretas!", "➖", "master_sub" in unlocked),
        Achievement("master_mul", "Mestre da Multiplicação", "100 multiplicações corretas!", "✖️", "master_mul" in unlocked),
        Achievement("master_div", "Mestre da Divisão", "100 divisões corretas!", "➗", "master_div" in unlocked),
    )
}

fun saveAchievement(prefs: SharedPreferences, id: String) {
    val unlocked = prefs.getStringSet("achievements", emptySet())?.toMutableSet() ?: mutableSetOf()
    unlocked.add(id)
    prefs.edit().putStringSet("achievements", unlocked).apply()
}

fun checkAndUnlockAchievements(
    prefs: SharedPreferences,
    totalCorrect: Int,
    level: Int,
    consecutiveCorrect: Int,
    wrongInLevel: Int,
    addStats: OperationStats,
    subStats: OperationStats,
    mulStats: OperationStats,
    divStats: OperationStats
): List<String> {
    val newUnlocks = mutableListOf<String>()
    val unlocked = prefs.getStringSet("achievements", emptySet()) ?: emptySet()
    
    fun unlock(id: String, title: String) {
        if (id !in unlocked) {
            saveAchievement(prefs, id)
            newUnlocks.add(title)
        }
    }
    
    if (totalCorrect >= 1) unlock("first_correct", "🎯 Primeiro Acerto!")
    if (totalCorrect >= 10) unlock("ten_correct", "⭐ Iniciante!")
    if (totalCorrect >= 50) unlock("fifty_correct", "🌟 Aprendiz!")
    if (totalCorrect >= 100) unlock("hundred_correct", "🏆 Mestre!")
    if (wrongInLevel == 0 && totalCorrect > 0) unlock("perfect_level", "💯 Perfeito!")
    if (consecutiveCorrect >= 5) unlock("five_consecutive", "🔥 Em Chama!")
    if (consecutiveCorrect >= 10) unlock("ten_consecutive", "⚡ Imparável!")
    if (level >= 10) unlock("level_10", "📚 Fase 10!")
    if (level >= 20) unlock("level_20", "📖 Fase 20!")
    if (level >= 30) unlock("level_30", "♾️ Infinito!")
    if (addStats.correct >= 100) unlock("master_add", "➕ Mestre da Adição!")
    if (subStats.correct >= 100) unlock("master_sub", "➖ Mestre da Subtração!")
    if (mulStats.correct >= 100) unlock("master_mul", "✖️ Mestre da Multiplicação!")
    if (divStats.correct >= 100) unlock("master_div", "➗ Mestre da Divisão!")
    
    return newUnlocks
}

fun loadDailyChallenge(prefs: SharedPreferences): DailyChallenge {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val savedDate = prefs.getString("challenge_date", "") ?: ""
    
    return if (savedDate == today) {
        DailyChallenge(
            date = today,
            description = prefs.getString("challenge_desc", "Responda 20 questões") ?: "Responda 20 questões",
            targetCorrect = prefs.getInt("challenge_target", 20),
            operation = Op.values()[prefs.getInt("challenge_op", 0)],
            completed = prefs.getBoolean("challenge_completed", false),
            progress = prefs.getInt("challenge_progress", 0)
        )
    } else {
        // Novo desafio
        val op = Op.values().random()
        val challenge = DailyChallenge(
            date = today,
            description = "Responda 20 questões de ${op.toPortuguese()}",
            targetCorrect = 20,
            operation = op,
            completed = false,
            progress = 0
        )
        prefs.edit().apply {
            putString("challenge_date", today)
            putString("challenge_desc", challenge.description)
            putInt("challenge_target", challenge.targetCorrect)
            putInt("challenge_op", op.ordinal)
            putBoolean("challenge_completed", false)
            putInt("challenge_progress", 0)
            apply()
        }
        challenge
    }
}

fun generateAdaptiveLevel(
    level: Int, 
    totalCorrect: Int, 
    totalWrong: Int,
    consecutiveCorrect: Int
): LevelConfig {
    // Taxa de acerto do jogador
    val accuracy = if (totalCorrect + totalWrong > 0) {
        totalCorrect.toFloat() / (totalCorrect + totalWrong)
    } else {
        0.5f
    }
    
    // Fases base seguem progressão original
    val baseConfig = levelConfig(level)
    
    // Se o jogador está indo muito bem (80%+ de acerto e 5+ acertos seguidos), aumenta dificuldade
    if (accuracy > 0.8f && consecutiveCorrect >= 5 && level > 5) {
        return baseConfig.copy(
            min = (baseConfig.min * 1.2).toInt(),
            max = (baseConfig.max * 1.3).toInt(),
            targetCorrect = baseConfig.targetCorrect + 1,
            description = "⚡ ${baseConfig.description} - MODO DESAFIO!"
        )
    }
    
    // Se o jogador está com dificuldade (<50% acerto), facilita um pouco
    if (accuracy < 0.5f && totalCorrect + totalWrong > 10 && level > 3) {
        return baseConfig.copy(
            min = maxOf(1, (baseConfig.min * 0.8).toInt()),
            max = (baseConfig.max * 0.85).toInt(),
            targetCorrect = maxOf(3, baseConfig.targetCorrect - 1),
            description = "🌟 ${baseConfig.description} - Você consegue!"
        )
    }
    
    return baseConfig
}

fun levelConfig(level: Int): LevelConfig = when (level) {
    // Fases 1-5: Adição básica
    in 1..5 -> LevelConfig(listOf(Op.ADD), 1, 10, 5, "Adição simples até 10")
    // Fases 6-10: Adição e subtração até 20
    in 6..10 -> LevelConfig(listOf(Op.ADD, Op.SUB), 1, 20, 6, "Somar e subtrair até 20")
    // Fases 11-15: Multiplicação básica
    in 11..15 -> LevelConfig(listOf(Op.MUL), 2, 10, 6, "Tabuada do 2 ao 10")
    // Fases 16-18: Misto (soma, subtração e multiplicação)
    in 16..18 -> LevelConfig(listOf(Op.ADD, Op.SUB, Op.MUL), 1, 15, 7, "Desafio misto!")
    // Fases 19-22: Números maiores
    in 19..22 -> LevelConfig(listOf(Op.ADD, Op.SUB), 10, 50, 7, "Números até 50")
    // Fases 23-25: Divisão simples
    in 23..25 -> LevelConfig(listOf(Op.DIV), 2, 10, 7, "Divisão exata")
    // Fases 26-28: Multiplicação avançada
    in 26..28 -> LevelConfig(listOf(Op.MUL), 5, 15, 8, "Multiplicação avançada")
    // Fases 29-30: Desafio final (tudo misturado, números maiores)
    in 29..30 -> LevelConfig(listOf(Op.ADD, Op.SUB, Op.MUL, Op.DIV), 5, 30, 10, "🏆 Desafio Final!")
    // Fases 31+: GERAÇÃO INFINITA - Aumenta progressivamente
    else -> {
        val phase = (level - 30) / 5 // A cada 5 fases aumenta dificuldade
        val allOps = listOf(Op.ADD, Op.SUB, Op.MUL, Op.DIV)
        val minRange = 10 + (phase * 10)
        val maxRange = 50 + (phase * 20)
        val target = minOf(15, 10 + phase)
        LevelConfig(
            allOps, 
            minRange, 
            maxRange, 
            target, 
            "⭐ Fase Infinita ${level - 30} - Expert!"
        )
    }
}

fun generateQuestion(cfg: LevelConfig): Question {
    val op = cfg.ops.random()
    val a = Random.nextInt(cfg.min, cfg.max + 1)
    val b = Random.nextInt(cfg.min, cfg.max + 1)

    val (text, correct) = when (op) {
        Op.ADD -> "$a + $b = ?" to (a + b)
        Op.SUB -> {
            val x = maxOf(a, b); val y = minOf(a, b)
            "$x - $y = ?" to (x - y)
        }
        Op.MUL -> "$a × $b = ?" to (a * b)
        Op.DIV -> {
            // Garantir divisão exata
            val divisor = Random.nextInt(2, 11)
            val result = Random.nextInt(cfg.min, cfg.max + 1)
            val dividend = divisor * result
            "$dividend ÷ $divisor = ?" to result
        }
    }

    val options = buildList {
        add(correct)
        var tries = 0
        while (size < 3 && tries < 20) {
            tries++
            val delta = when (op) {
                Op.MUL -> Random.nextInt(1, maxOf(7, correct / 2 + 1))
                Op.DIV -> Random.nextInt(1, 5)
                else -> Random.nextInt(1, maxOf(5, correct / 3 + 1))
            }
            val sign = if (Random.nextBoolean()) 1 else -1
            val cand = (correct + sign * delta).coerceAtLeast(0)
            if (cand != correct && cand !in this) add(cand)
        }
    }.shuffled()

    return Question(text, correct, options)
}

fun getSmartHint(question: Question): String {
    // Extrai os números da pergunta
    val numbers = question.text.replace("=", "").replace("?", "").trim()
    val parts = numbers.split(Regex("[+\\-×÷]")).map { it.trim() }
    
    return when {
        question.text.contains("+") -> {
            val a = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val b = parts.getOrNull(1)?.toIntOrNull() ?: 0
            when {
                a <= 5 && b <= 5 -> "Conte $a dedos de uma mão e $b da outra. Quantos no total?"
                a <= 10 -> "Comece de $a e conte mais $b: ${a+1}, ${a+2}..."
                b == 10 -> "Somar 10 é fácil! $a + 10 = ${a}(1)(0) no final"
                b <= 5 -> "Pense: $a e mais $b dá quanto? Conte a partir de $a!"
                else -> "Divida em partes menores: $a + ${b-5} = ${a+b-5}, depois +5 = ${a+b}"
            }
        }
        question.text.contains("-") -> {
            val a = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val b = parts.getOrNull(1)?.toIntOrNull() ?: 0
            when {
                b <= 5 -> "Comece de $a e volte $b: ${a-1}, ${a-2}..."
                a <= 20 -> "Tire $b de $a. Pense: o que falta para $b chegar em $a?"
                b == 10 -> "Subtrair 10 é fácil! $a - 10 = ${a.toString().dropLast(1)}0"
                else -> "Subtraia aos poucos: $a - ${b/2} = ${a-b/2}, depois - ${b - b/2}"
            }
        }
        question.text.contains("×") -> {
            val a = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val b = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val smaller = minOf(a, b)
            val bigger = maxOf(a, b)
            when {
                smaller == 2 -> "Dobre o número! $bigger + $bigger = ${bigger * 2}"
                smaller == 3 -> "Some 3 vezes: $bigger + $bigger + $bigger"
                smaller == 5 -> "Metade de ${bigger * 10} é ${bigger * 5}"
                smaller == 10 -> "Coloque um 0 no final! $bigger × 10 = ${bigger}0"
                bigger <= 5 -> "Some $smaller vezes o $bigger: " + (1..smaller).joinToString(" + ") { bigger.toString() }
                else -> "Use a tabuada do $smaller: $smaller × $bigger. Lembre: ${smaller} × ${bigger-1} = ${smaller*(bigger-1)}"
            }
        }
        question.text.contains("÷") -> {
            val a = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val b = parts.getOrNull(1)?.toIntOrNull() ?: 0
            when {
                b == 2 -> "Metade de $a é quanto? Divida em 2 partes iguais"
                b <= 5 -> "Quantos grupos de $b cabem em $a? Conte: $b, ${b*2}, ${b*3}..."
                b == 10 -> "Tire o último zero! $a ÷ 10 = ${a.toString().dropLast(1)}"
                else -> "Pense na tabuada: $b × ? = $a. Qual número vezes $b dá $a?"
            }
        }
        else -> "Leia com calma e faça passo a passo. Você consegue!"
    }
}

fun getHint(question: Question, config: LevelConfig): String {
    val op = config.ops.firstOrNull() ?: Op.ADD
    return when {
        question.text.contains("+") -> "Dica: Conte nos dedos ou some os números!"
        question.text.contains("-") -> "Dica: Comece do número maior e conte para trás!"
        question.text.contains("×") -> "Dica: Lembre da tabuada ou some várias vezes!"
        question.text.contains("÷") -> "Dica: Quantas vezes cabe? Pense na multiplicação!"
        else -> "Dica: Leia com calma e faça passo a passo!"
    }
}

private fun ComponentActivity.requestConsent() {
    val params = ConsentRequestParameters.Builder()
        .setTagForUnderAgeOfConsent(false)
        .build()
    val consentInformation = UserMessagingPlatform.getConsentInformation(this)
    Log.d("JogoInfantil", "🔐 Solicitando atualização de consentimento UMP...")
    
    consentInformation.requestConsentInfoUpdate(
        this,
        params,
        {
            Log.d("JogoInfantil", "✅ Informações de consentimento atualizadas")
            Log.d("JogoInfantil", "📊 Status: canRequestAds=${consentInformation.canRequestAds()}")
            Log.d("JogoInfantil", "📊 Consentimento necessário: ${consentInformation.isConsentFormAvailable}")
            
            // Se não precisar de formulário (ex: Brasil), libera anúncios direto
            if (consentInformation.canRequestAds()) {
                Log.d("JogoInfantil", "✅ Anúncios liberados - região não requer consentimento GDPR")
                MainActivity.allowAdsWithoutConsent = true
                return@requestConsentInfoUpdate
            }
            
            // Se precisar, mostra formulário (Europa)
            if (consentInformation.isConsentFormAvailable) {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    this
                ) { formError ->
                    if (formError != null) {
                        Log.e("JogoInfantil", "❌ Erro ao exibir formulário: ${formError.message}")
                        MainActivity.allowAdsWithoutConsent = true
                    } else {
                        Log.d("JogoInfantil", "✅ Formulário de consentimento processado")
                        Log.d("JogoInfantil", "📊 Anúncios permitidos: ${consentInformation.canRequestAds()}")
                        MainActivity.allowAdsWithoutConsent = true
                    }
                }
            } else {
                Log.w("JogoInfantil", "⚠️ Formulário de consentimento não disponível, liberando anúncios sem personalização")
                MainActivity.allowAdsWithoutConsent = true
            }
        },
        { error ->
            Log.e("JogoInfantil", "❌ Falha ao atualizar consentimento: ${error.message}")
            Log.w("JogoInfantil", "⚠️ Continuando sem personalização de anúncios")
            MainActivity.allowAdsWithoutConsent = true
        }
    )
}
