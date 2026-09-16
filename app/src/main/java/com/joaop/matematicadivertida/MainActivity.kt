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

// AdManager foi removido de propósito.
//
// Ele carregava intersticial e recompensado — anúncios de tela cheia, proibidos pela
// Política para Famílias em app dirigido a crianças (rejeição "anúncios que não podem
// ser fechados"). Pior: os IDs eram os de TESTE públicos do Google
// (ca-app-pub-3940256099942544/...), hardcoded no build de release. Unidades de teste
// servem criativos de demonstração que ignoram a classificação máxima de conteúdo, o
// que causou a segunda rejeição ("conteúdo do anúncio não condiz com a classificação").
//
// Único formato permitido neste app: banner ancorado (ver BannerAdView).

internal val AppBackgroundColor =Color(0xFFD6E9FC) // Slightly deeper blue for better contrast

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "JogoInfantil"
        private val canShowAdsState = mutableStateOf(false)
        private val mobileAdsInitStarted = AtomicBoolean(false)

        /** Só fica true quando a UMP confirma que anúncios podem ser solicitados. */
        var canShowAds: Boolean
            get() = canShowAdsState.value
            set(value) {
                canShowAdsState.value = value
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash API (Android 12+) - deve ser chamado antes de super.onCreate
        installSplashScreen()

        super.onCreate(savedInstanceState)
        Log.d(TAG, "🎮 Iniciando aplicativo...")

        // Configuração obrigatória para Política de Famílias do Google Play (COPPA & Classificação Livre G).
        // Precisa valer antes de MobileAds.initialize() e de qualquer loadAd().
        // TFUA não é marcado aqui: o Google recomenda não combinar TFCD e TFUA no
        // RequestConfiguration. Para a UMP, TFUA é marcado em requestConsent().
        val requestConfiguration = RequestConfiguration.Builder()
            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
            .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)

        // O SDK de anúncios só é inicializado depois que a UMP libera anúncios, como o Google
        // recomenda. Se a liberação já veio de uma sessão anterior, inicializa agora; senão,
        // requestConsent() inicializa quando a UMP responder. Nenhum anúncio de tela cheia é
        // pré-carregado: o único formato do app é o banner ancorado (BannerAdView).
        if (UserMessagingPlatform.getConsentInformation(this).canRequestAds()) {
            initializeMobileAds()
        }

        // Solicitar consentimento (UMP)
        requestConsent()

        // Retenção: agenda o lembrete diário local (idempotente; respeita a preferência)
        DailyReminder.ensureScheduled(this)
        
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = AppBackgroundColor
                ) {
                    GameApp()
                }
            }
        }
    }

    /** Inicializa o Mobile Ads uma vez por processo, fora da thread principal (evita ANR). */
    internal fun initializeMobileAds() {
        if (!mobileAdsInitStarted.compareAndSet(false, true)) return
        lifecycleScope.launch(Dispatchers.IO) {
            MobileAds.initialize(this@MainActivity) {
                Log.d(TAG, "✅ AdMob inicializado com política para famílias (COPPA & Rating G)")
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
    // Maior fase alcançada. Separada de `level` para que rejogar uma fase pela Trilha de
    // Mundos não apague o progresso nem bloqueie de novo as fases seguintes.
    var maxLevel by rememberSaveable { mutableIntStateOf(maxOf(prefs.getInt("max_level", 1), prefs.getInt("level", 1))) }
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
    val playerLevel = GameDataManager.playerLevelForXp(xp)
    
    // Estatísticas por operação
    var addStats by remember { mutableStateOf(GameDataManager.loadOperationStats(prefs, "add")) }
    var subStats by remember { mutableStateOf(GameDataManager.loadOperationStats(prefs, "sub")) }
    var mulStats by remember { mutableStateOf(GameDataManager.loadOperationStats(prefs, "mul")) }
    var divStats by remember { mutableStateOf(GameDataManager.loadOperationStats(prefs, "div")) }
    
    // Conquistas
    var achievements by remember { mutableStateOf(GameDataManager.loadAchievements(prefs)) }
    
    // UI States - Navegação entre telas
    // Saveable: girar a tela não pode jogar a criança de volta ao menu nem tirá-la do treino.
    var currentScreen by rememberSaveable { mutableStateOf("MENU") } // MENU, GAME, TIME_ATTACK, SETTINGS
    var showStats by remember { mutableStateOf(false) }
    var showAchievements by remember { mutableStateOf(false) }
    var showTrainingMode by remember { mutableStateOf(false) }
    var trainingOp by rememberSaveable { mutableStateOf<Op?>(null) }
    var isInTrainingMode by rememberSaveable { mutableStateOf(false) }
    var trainingCorrectCount by rememberSaveable { mutableIntStateOf(0) }
    var showFeedbackAnimation by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    var feedbackEmoji by remember { mutableStateOf("") }
    var feedbackIsCorrect by remember { mutableStateOf(true) }
    var questionStartTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var inputsEnabled by remember { mutableStateOf(true) }
    var nextAction by remember { mutableStateOf("NONE") }
    
    // Micro-lições: controla se já introduziu cada operação
    var hasIntroducedSub by rememberSaveable { mutableStateOf(prefs.getBoolean("introduced_sub", false)) }
    var hasIntroducedMul by rememberSaveable { mutableStateOf(prefs.getBoolean("introduced_mul", false)) }
    var hasIntroducedDiv by rememberSaveable { mutableStateOf(prefs.getBoolean("introduced_div", false)) }
    var showMicroLesson by remember { mutableStateOf<Op?>(null) }
    
    // Repetição espaçada
    var questionsAnsweredTotal by rememberSaveable { mutableIntStateOf(prefs.getInt("questions_answered_total", 0)) }

    // Nível do aluno baseado no total de acertos
    val studentLevelLabel = remember(totalCorrect) {
        when {
            totalCorrect < 20 -> "Iniciante"
            totalCorrect < 50 -> "Aprendiz"
            totalCorrect < 100 -> "Esperto"
            totalCorrect < 200 -> "Campeão"
            else -> "Mestre da Matemática"
        }
    }
    
    // Desafio diário
    var dailyChallenge by remember { mutableStateOf(GameDataManager.loadDailyChallenge(prefs)) }

    // Sequência Diária (Streak), Highscore e Avatares
    var dailyStreak by remember { mutableIntStateOf(GameDataManager.updateDailyStreak(prefs)) }
    var timeAttackHighScore by rememberSaveable { mutableIntStateOf(GameDataManager.getTimeAttackHighScore(prefs)) }
    val avatars = remember { GameDataManager.getAvailableAvatars() }
    var selectedAvatarId by rememberSaveable { mutableStateOf(prefs.getString("selected_avatar", "student") ?: "student") }
    val currentAvatar = remember(selectedAvatarId) { avatars.find { it.id == selectedAvatarId } ?: avatars.first() }
    var showAvatarDialog by remember { mutableStateOf(false) }

    // Mecânicas Arcade: Combo, Power-ups, Boss e Roleta
    var comboCount by rememberSaveable { mutableIntStateOf(0) }
    var hasActiveShield by rememberSaveable { mutableStateOf(false) }
    val currentBoss = remember(level) { GameDataManager.getBossForLevel(level) }
    var showLuckyWheel by remember { mutableStateOf(false) }
    var showBossVictory by remember { mutableStateOf(false) }
    var showDailyRewards by remember { mutableStateOf(false) }
    var showWorldMap by remember { mutableStateOf(false) }

    // Retenção (DIAGNOSTICO_RETENCAO.md): onboarding no primeiro uso e lembrete diário
    var showOnboarding by rememberSaveable {
        mutableStateOf(
            !prefs.getBoolean("onboarding_done", false) &&
                prefs.getInt("totalCorrect", 0) == 0 && prefs.getInt("level", 1) == 1
        )
    }
    LaunchedEffect(Unit) {
        // Quem já jogava antes do onboarding existir não precisa vê-lo depois de um reinício.
        if (!showOnboarding) prefs.edit().putBoolean("onboarding_done", true).apply()
    }
    var showReminderPrompt by remember { mutableStateOf(false) }
    var reminderEnabled by remember { mutableStateOf(DailyReminder.isEnabled(prefs)) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        reminderEnabled = granted
        DailyReminder.setEnabled(ctx, granted)
    }

    fun enableReminder() {
        if (DailyReminder.hasNotificationPermission(ctx)) {
            reminderEnabled = true
            DailyReminder.setEnabled(ctx, true)
        } else {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var shieldCount by remember { mutableIntStateOf(GameDataManager.getPowerUpCount(prefs, PowerUpType.SHIELD)) }
    var bombCount by remember { mutableIntStateOf(GameDataManager.getPowerUpCount(prefs, PowerUpType.BOMB_5050)) }
    var freezeCount by remember { mutableIntStateOf(GameDataManager.getPowerUpCount(prefs, PowerUpType.FREEZE)) }

    // Preferência de som: ligado/desligado (mantém migração do legado sound_level)

    val soundEnabledKey = "sound_enabled"
    val legacySoundLevel = remember { prefs.getInt("sound_level", 2) }
    val soundEnabledDefault = remember {
        if (prefs.contains(soundEnabledKey)) prefs.getBoolean(soundEnabledKey, true) else legacySoundLevel != 0
    }
    var soundEnabled by rememberSaveable { mutableStateOf(soundEnabledDefault) }
    LaunchedEffect(soundEnabled) {
        prefs.edit().putBoolean(soundEnabledKey, soundEnabled).apply()
    }

    // Preferência de vibração
    val vibrationEnabledKey = "vibration_enabled"
    var vibrationEnabled by rememberSaveable { mutableStateOf(prefs.getBoolean(vibrationEnabledKey, true)) }
    LaunchedEffect(vibrationEnabled) {
        prefs.edit().putBoolean(vibrationEnabledKey, vibrationEnabled).apply()
    }
    
    // Sons e vibração
    val vibrator = remember { 
        try {
            ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } catch (e: Exception) {
            Log.e("JogoInfantil", "Erro ao obter vibrator: ${e.message}")
            null
        }
    }
    val canVibrate = remember {
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED
    }

    val soundVolume = if (soundEnabled) 0.90f else 0f

    val soundPlayer = remember(soundEnabled) {
        if (soundEnabled) SoundFeedbackPlayer(context = ctx, volume = soundVolume) else null
    }

    DisposableEffect(soundPlayer) {
        onDispose {
            try {
                soundPlayer?.close()
            } catch (_: Exception) {
                // Ignorar falhas de release
            }
        }
    }
    
    fun playSound(isCorrect: Boolean) {
        try {
            if (soundEnabled) {
                if (isCorrect) soundPlayer?.playCorrect() else soundPlayer?.playWrong()
            }

            if (isCorrect && vibrationEnabled && canVibrate && vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(100)
                }
            }
        } catch (e: Exception) {
            Log.e("JogoInfantil", "Erro no feedback: ${e.message}")
        }
    }
    
    // Salvar progresso sempre que mudar
    LaunchedEffect(level) {
        if (level > maxLevel) maxLevel = level
    }

    LaunchedEffect(level, maxLevel, totalCorrect, totalWrong, consecutiveCorrect, wrong) {
        prefs.edit().apply {
            putInt("level", level)
            putInt("max_level", maxLevel)
            putInt("totalCorrect", totalCorrect)
            putInt("totalWrong", totalWrong)
            putInt("consecutiveCorrect", consecutiveCorrect)
            putInt("wrong", wrong)
            apply()
        }
        Log.d("JogoInfantil", "💾 Progresso salvo: Fase $level, Acertos $totalCorrect, Erros $totalWrong")
    }
    
    val config = remember(level, totalCorrect, totalWrong, isInTrainingMode, trainingOp) {
        if (isInTrainingMode) {
            // Modo treino: fica ativo até o usuário sair.
            // trainingOp == null significa "misto" (todas as operações).
            val ops = trainingOp?.let { listOf(it) } ?: listOf(Op.ADD, Op.SUB, Op.MUL, Op.DIV)
            val opName = when (trainingOp) {
                Op.ADD -> "Adição"
                Op.SUB -> "Subtração"
                Op.MUL -> "Multiplicação"
                Op.DIV -> "Divisão"
                null -> "Misto"
            }

            LevelConfig(
                ops = ops,
                min = 0,
                max = if (trainingOp == Op.MUL || trainingOp == Op.DIV || trainingOp == null) 10 else 20,
                targetCorrect = 10,
                description = "Modo Treino: $opName"
            )
        } else {
            generateAdaptiveLevel(level, totalCorrect, totalWrong, consecutiveCorrect)
        }
    }

    // Incrementado para sortear a próxima questão. Antes as chaves do remember refaziam a
    // questão já no acerto (atrás do feedback) e ela era sorteada de novo ao fechar o
    // feedback — o que descartava a questão de revisão da repetição espaçada.
    var questionNonce by remember { mutableIntStateOf(0) }

    var question by remember(level, isInTrainingMode, trainingOp, questionNonce) {
        mutableStateOf(
            // No modo treino, não injeta questões de revisão para não trocar a operação selecionada.
            if (isInTrainingMode) {
                generateQuestion(config)
            } else {
                // Repetição espaçada: a cada 5 acertos revisa uma questão errada, desde que a
                // operação dela faça parte da fase atual.
                val review = if (questionsAnsweredTotal % 5 == 0 && questionsAnsweredTotal > 0) {
                    GameDataManager.getQuestionsForReview(prefs, questionsAnsweredTotal)
                        .mapNotNull { parseQuestionFromText(it) }
                        .filter { it.op in config.ops }
                        .randomOrNull()
                } else {
                    null
                }
                review ?: generateQuestion(config)
            }
        )
    }
    var attemptsOnCurrentQuestion by remember(question) { mutableIntStateOf(0) }
    var disabledOptions by remember(question) { mutableStateOf(setOf<Int>()) }
    val bossHp = currentBoss?.let { GameDataManager.bossHpAfter(it.maxHp, correctThisLevel, config.targetCorrect) } ?: 0

    LaunchedEffect(question) {
        questionStartTime = System.currentTimeMillis()
    }
    var showCompleted by remember { mutableStateOf(false) }
    var showGameOver by remember { mutableStateOf(false) }
    
    // Detectar quando nova operação é introduzida
    LaunchedEffect(config.ops) {
        val newOps = config.ops
        if (Op.SUB in newOps && !hasIntroducedSub) {
            showMicroLesson = Op.SUB
            inputsEnabled = false
        } else if (Op.MUL in newOps && !hasIntroducedMul) {
            showMicroLesson = Op.MUL
            inputsEnabled = false
        } else if (Op.DIV in newOps && !hasIntroducedDiv) {
            showMicroLesson = Op.DIV
            inputsEnabled = false
        }
    }

    // Recomeça a trilha de fases. Moedas, XP, conquistas, estatísticas, recordes, power-ups e
    // configurações continuam salvos: antes um prefs.edit().clear() apagava tudo isso (e ainda
    // liberava de novo o prêmio diário e a roleta), deixando a memória fora de sincronia.
    fun restartProgress() {
        level = 1
        maxLevel = 1
        correctThisLevel = 0
        wrong = 0
        lives = 3
        hintsUsed = 0
        showHint = false
        totalCorrect = 0
        totalWrong = 0
        consecutiveCorrect = 0
        consecutiveWrong = 0
        comboCount = 0
        questionNonce++
    }

    fun exitToMenu() {
        currentScreen = "MENU"
        if (isInTrainingMode) {
            isInTrainingMode = false
            trainingOp = null
            trainingCorrectCount = 0
        }
    }

    // Tela de game over
    if (showGameOver) {
        GameOverDialog(
            level = level,
            correctAnswers = correctThisLevel,
            onRestart = {
                showGameOver = false
                inputsEnabled = true
                restartProgress()
            },
        )
    }

    var showResetConfirm by remember { mutableStateOf(false) }
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("🔄 Recomeçar da fase 1?") },
            text = { Text("Suas moedas, conquistas e estatísticas continuam guardadas.") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirm = false
                        restartProgress()
                        Log.d("JogoInfantil", "🔄 Jogo resetado manualmente")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) { Text("Recomeçar") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancelar") }
            }
        )
    }

    if (showReminderPrompt) {
        ReminderOptInDialog(
            onAccept = {
                showReminderPrompt = false
                prefs.edit().putBoolean(DailyReminder.PREF_PROMPT_SHOWN, true).apply()
                enableReminder()
            },
            onDecline = {
                showReminderPrompt = false
                prefs.edit().putBoolean(DailyReminder.PREF_PROMPT_SHOWN, true).apply()
            }
        )
    }

    // Tela de fase completa. Sem intersticial: anúncio de tela cheia entre fases é
    // exatamente o que a Política para Famílias proíbe em app dirigido a crianças.
    if (showCompleted) {
        LevelCompletedDialog(
            level = level,
            totalLevels = totalLevels,
            onNext = {
                showCompleted = false
                // Fase vencida é um bom momento para convidar para o lembrete diário. Só é
                // preciso no Android 13+; nas versões anteriores ele já vem ligado.
                if (!prefs.getBoolean(DailyReminder.PREF_PROMPT_SHOWN, false) &&
                    !DailyReminder.hasNotificationPermission(ctx)
                ) {
                    showReminderPrompt = true
                }
                inputsEnabled = true
                if (level < totalLevels) {
                    level += 1
                } else {
                    level = 1
                }
                correctThisLevel = 0
                wrong = 0
                lives = 3
                hintsUsed = 0
                showHint = false
            }
        )
    }

    if (showOnboarding) {
        OnboardingScreen(onFinish = { startPlaying ->
            prefs.edit().putBoolean("onboarding_done", true).apply()
            showOnboarding = false
            if (startPlaying) currentScreen = "GAME"
        })
        return@GameApp
    }

    // Verificar primeiro se algum dialog precisa ser mostrado
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
        return@GameApp
    }

    if (showAchievements) {
        AchievementsScreen(
            achievements = achievements,
            onDismiss = { showAchievements = false }
        )
        return@GameApp
    }

    if (showAvatarDialog) {
        AvatarSelectionDialog(
            avatars = avatars,
            playerLevel = playerLevel,
            selectedAvatarId = selectedAvatarId,
            onSelectAvatar = { newId ->
                selectedAvatarId = newId
                prefs.edit().putString("selected_avatar", newId).apply()
                showAvatarDialog = false
            },
            onDismiss = { showAvatarDialog = false }
        )
        return@GameApp
    }

    if (showLuckyWheel) {
        LuckyWheelDialog(
            // Roleta diária: canSpinWheel existia mas nunca era consultado (giros infinitos).
            canSpin = remember { GameDataManager.canSpinWheel(prefs) },
            onReward = { name, amount ->
                if (name.contains("Moedas")) {
                    coins += amount
                    prefs.edit().putInt("coins", coins).apply()
                } else if (name.contains("Escudo")) {
                    GameDataManager.addPowerUp(prefs, PowerUpType.SHIELD, amount)
                    shieldCount = GameDataManager.getPowerUpCount(prefs, PowerUpType.SHIELD)
                } else if (name.contains("Bomba")) {
                    GameDataManager.addPowerUp(prefs, PowerUpType.BOMB_5050, amount)
                    bombCount = GameDataManager.getPowerUpCount(prefs, PowerUpType.BOMB_5050)
                } else if (name.contains("XP")) {
                    xp += amount
                    prefs.edit().putInt("xp", xp).apply()
                }
                GameDataManager.recordWheelSpin(prefs)
            },
            onDismiss = { showLuckyWheel = false }
        )
        return@GameApp
    }

    if (showBossVictory && currentBoss != null) {
        BossVictoryDialog(
            bossName = currentBoss.name,
            rewardCoins = currentBoss.rewardCoins,
            onContinue = {
                showBossVictory = false
                // Momento de vitória: pode pedir avaliação na loja (com limites em ReviewPrompter)
                (ctx as? Activity)?.let { ReviewPrompter.maybeAsk(it, prefs, totalCorrect) }
                coins += currentBoss.rewardCoins
                prefs.edit().putInt("coins", coins).apply()
                level += 1
                correctThisLevel = 0
                wrong = 0
                lives = 3
                hintsUsed = 0
                showHint = false
                currentScreen = "MENU"
            }
        )
        return@GameApp
    }

    if (showDailyRewards) {
        DailyRewardsDialog(
            currentDay = GameDataManager.getDailyRewardStreakDay(prefs),
            canClaim = GameDataManager.canClaimDailyReward(prefs),
            onClaim = { day ->
                val claimedDay = GameDataManager.claimDailyReward(prefs)
                when (claimedDay) {
                    1 -> { coins += 50 }
                    2 -> { GameDataManager.addPowerUp(prefs, PowerUpType.SHIELD, 1) }
                    3 -> { coins += 100 }
                    4 -> { GameDataManager.addPowerUp(prefs, PowerUpType.BOMB_5050, 1) }
                    5 -> { coins += 150 }
                    6 -> { xp += 200 }
                    7 -> { coins += 300; GameDataManager.addPowerUp(prefs, PowerUpType.SHIELD, 2) }
                }
                prefs.edit().putInt("coins", coins).putInt("xp", xp).apply()
                shieldCount = GameDataManager.getPowerUpCount(prefs, PowerUpType.SHIELD)
                bombCount = GameDataManager.getPowerUpCount(prefs, PowerUpType.BOMB_5050)
                showDailyRewards = false
            },
            onDismiss = { showDailyRewards = false }
        )
        return@GameApp
    }

    if (showWorldMap) {
        WorldMapDialog(
            currentLevel = level,
            maxUnlockedLevel = maxLevel,
            onSelectLevel = { selectedLvl ->
                level = selectedLvl
                correctThisLevel = 0
                wrong = 0
                lives = 3
                hintsUsed = 0
                showHint = false
                questionNonce++
                showWorldMap = false
                currentScreen = "GAME"
            },
            onDismiss = { showWorldMap = false }
        )
        return@GameApp
    }

    if (currentScreen == "TIME_ATTACK") {
        TimeAttackGameScreen(
            highScore = timeAttackHighScore,
            soundPlayer = soundPlayer,
            onFinish = { score, isNewHigh ->
                if (isNewHigh) {
                    timeAttackHighScore = score
                    GameDataManager.saveTimeAttackScore(prefs, score)
                    (ctx as? Activity)?.let { ReviewPrompter.maybeAsk(it, prefs, totalCorrect) }
                }
            },
            onBack = { currentScreen = "MENU" }
        )
        return@GameApp
    }

    if (showTrainingMode) {
        TrainingModeSelector(
            onSelectOperation = { op ->
                trainingOp = op
                isInTrainingMode = true
                trainingCorrectCount = 0
                showTrainingMode = false
                currentScreen = "GAME"  // Vai para o jogo em modo treino
            },
            onDismiss = { 
                showTrainingMode = false
            }
        )
        return@GameApp
    }

    // Gerenciar botão de voltar do sistema
    BackHandler(enabled = currentScreen != "MENU") {
        when (currentScreen) {
            "GAME" -> exitToMenu()
            "STATS" -> {
                // Se está nas estatísticas, volta para o menu
                showStats = false
                currentScreen = "MENU"
            }
            "ACHIEVEMENTS" -> {
                // Se está nas conquistas, volta para o menu
                showAchievements = false
                currentScreen = "MENU"
            }
            "TRAINING" -> {
                // Se está no modo treino, volta para o menu
                showTrainingMode = false
                currentScreen = "MENU"
            }
            "SETTINGS" -> {
                currentScreen = "MENU"
            }
        }
    }

    if (currentScreen == "SETTINGS") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackgroundColor)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚙️ Configurações",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Button(
                    onClick = { currentScreen = "MENU" },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    modifier = Modifier.size(48.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("🏠", fontSize = 20.sp)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("🔊 Som", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = if (soundEnabled) "Ligado" else "Desligado",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                            )
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { enabled -> soundEnabled = enabled }
                        )
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("📳 Vibração", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = if (vibrationEnabled) "Ligada" else "Desligada",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                            )
                        }
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { vibrationEnabled = it }
                        )
                    }

                    Divider()

                    // Lembrete diário (retenção). No Android 13+ ligar pede a permissão de notificação.
                    val reminderOn = reminderEnabled && DailyReminder.hasNotificationPermission(ctx)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("🔔 Lembrete diário", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = if (reminderOn) "Às ${DailyReminder.REMINDER_HOUR}h, se ainda não jogou no dia" else "Desligado",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                            )
                        }
                        Switch(
                            checked = reminderOn,
                            onCheckedChange = { on ->
                                if (on) {
                                    enableReminder()
                                } else {
                                    reminderEnabled = false
                                    DailyReminder.setEnabled(ctx, false)
                                }
                            }
                        )
                    }

                    Divider()

                    Button(
                        onClick = {
                            if (soundEnabled) {
                                soundPlayer?.playCorrect()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("▶️ Testar som", color = Color.White)
                    }
                }
            }

            Text(
                text = "Dica: o som depende do volume de MÍDIA do aparelho.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
            )
        }
        return@GameApp
    }

    // Tela de Menu ou Tela de Jogo
    if (currentScreen == "MENU") {
        // Menu reorganizado (P2 do DIAGNOSTICO_RETENCAO.md): ver MainMenuScreen
        MainMenuScreen(
            avatar = currentAvatar,
            dailyStreak = dailyStreak,
            level = level,
            studentLevelLabel = studentLevelLabel,
            coins = coins,
            timeAttackHighScore = timeAttackHighScore,
            canClaimDaily = remember { GameDataManager.canClaimDailyReward(prefs) },
            canSpinWheel = remember { GameDataManager.canSpinWheel(prefs) },
            onPlay = { currentScreen = "GAME" },
            onAvatarClick = { showAvatarDialog = true },
            onDailyReward = { showDailyRewards = true },
            onLuckyWheel = { showLuckyWheel = true },
            onTimeAttack = { currentScreen = "TIME_ATTACK" },
            onWorldMap = { showWorldMap = true },
            onTraining = { showTrainingMode = true },
            onStats = { showStats = true },
            onAchievements = { showAchievements = true },
            onSettings = { currentScreen = "SETTINGS" },
            onChallengeFriend = {
                shareText(
                    ctx,
                    "Compartilhar Matemática Divertida",
                    "🎮 Aprendendo matemática brincando com o app Matemática Divertida! Consegui $totalCorrect acertos até agora. Baixe você também!"
                )
            }
        )
        return@GameApp
    }

    // Se não estiver no menu, mostrar o jogo normal
    Scaffold(
        containerColor = AppBackgroundColor,
        bottomBar = { BannerAdView(modifier = Modifier.fillMaxWidth()) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Modo Treino - Botão de saída
            if (isInTrainingMode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val opName = when (trainingOp) {
                                Op.ADD -> "Adição"
                                Op.SUB -> "Subtração"
                                Op.MUL -> "Multiplicação"
                                Op.DIV -> "Divisão"
                                null -> "Misto"
                            }
                            Text(
                                text = "🎯 Modo Treino: $opName",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "✅ $trainingCorrectCount acertos",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White
                                )
                            )
                        }
                        Button(
                            onClick = { exitToMenu() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                        ) {
                            Text("Sair", color = Color.White)
                        }
                    }
                }
            }

            // Cabeçalho da fase
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEB3B)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Voltar ao menu também sai do treino (antes o JOGAR seguinte abria o
                        // modo treino em vez do jogo normal)
                        Button(
                            onClick = { exitToMenu() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                            modifier = Modifier.size(48.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("🏠", fontSize = 20.sp)
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📚 Fase $level${if (level > 30) " (Infinita)" else " de 30"}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1976D2)
                                )
                            )
                            Text(
                                text = "👶 Nível do aluno: $studentLevelLabel",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp,
                                    color = Color(0xFF424242)
                                )
                            )
                        }
                        Button(
                            // Pede confirmação: um toque sem querer não pode zerar o progresso.
                            onClick = { showResetConfirm = true },
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
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        ),
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { correctThisLevel / config.targetCorrect.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0xFFE0E0E0),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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
                            // Dica no nível das tentativas feitas. Antes mostrava sempre o passo a
                            // passo com a resposta, o que anulava as dicas progressivas.
                            text = getProgressiveHint(question, level = attemptsOnCurrentQuestion.coerceIn(1, 3)),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 15.sp,
                                color = Color(0xFF5D4037)
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Barra de Boss (se for fase de Boss; o treino não enfrenta chefões)
            if (currentBoss != null && !isInTrainingMode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "${currentBoss.emoji} ${currentBoss.name}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { bossHp / currentBoss.maxHp.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp),
                            color = Color(0xFFFFEB3B),
                            trackColor = Color.Black.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "HP: $bossHp / ${currentBoss.maxHp}",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Indicador de Combo / Modo Fever
            if (comboCount >= 3) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (comboCount >= 10) Color(0xFFE91E63) else Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (comboCount >= 10) "🌟 MODO FEVER 🔥 (x3 Bônus!)" else "🔥 Combo x${if (comboCount >= 5) 2 else 1.5} (${comboCount} acertos)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
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
                        modifier = Modifier.testTag("questionText"),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                    )
                }
            }

            // Barra de Power-Ups Usáveis
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Escudo Mágico
                    Button(
                        onClick = {
                            if (!hasActiveShield && GameDataManager.usePowerUp(prefs, PowerUpType.SHIELD)) {
                                hasActiveShield = true
                                shieldCount = GameDataManager.getPowerUpCount(prefs, PowerUpType.SHIELD)
                            }
                        },
                        enabled = shieldCount > 0 && !hasActiveShield,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasActiveShield) Color(0xFF4CAF50) else Color(0xFF2196F3)
                        ),
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                    ) {
                        Text(if (hasActiveShield) "🛡️ Ativo" else "🛡️ Escudo ($shieldCount)", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // Bomba 50/50
                    Button(
                        onClick = {
                            val wrongOpts = question.options.filter { it != question.correct }
                            // Deixa uma alternativa errada: com 3 opções, tirar 2 entregava a resposta.
                            if (disabledOptions.isEmpty() && wrongOpts.size >= 2 && GameDataManager.usePowerUp(prefs, PowerUpType.BOMB_5050)) {
                                disabledOptions = wrongOpts.shuffled().take(wrongOpts.size - 1).toSet()
                                bombCount = GameDataManager.getPowerUpCount(prefs, PowerUpType.BOMB_5050)
                            }
                        },
                        enabled = bombCount > 0 && disabledOptions.isEmpty() && question.options.count { it != question.correct } >= 2,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                    ) {
                        Text("🔮 50/50 ($bombCount)", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
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
                        modifier = Modifier.fillMaxWidth().testTag("hintButton"),
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
                    // Limite de dicas atingido - apenas desabilita botão extra
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Opções de resposta - com proteção contra lista vazia
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val safeOptions = if (question.options.isEmpty()) {
                    Log.e("JogoInfantil", "ERRO: question.options vazia!")
                    listOf(question.correct, question.correct + 1, question.correct + 2)
                } else {
                    question.options
                }
                
                safeOptions.forEach { option ->
                    val isDisabled = option in disabledOptions
                    Button(
                        onClick = {
                            if (showFeedbackAnimation || !inputsEnabled || showGameOver || showCompleted || isDisabled) {
                                return@Button
                            }
                            val responseTime = System.currentTimeMillis() - questionStartTime
                            
                            if (option == question.correct) {
                                // ACERTOU! 🎉
                                attemptsOnCurrentQuestion = 0
                                comboCount += 1
                                
                                // Acertou: sai da fila de revisão espaçada (antes nunca saía)
                                GameDataManager.removeReviewedQuestion(prefs, question.text)

                                
                                // Incrementar contador total de questões respondidas
                                questionsAnsweredTotal += 1
                                prefs.edit().putInt("questions_answered_total", questionsAnsweredTotal).apply()
                                
                                // Se está em modo treino, incrementa contador de treino
                                if (isInTrainingMode) {
                                    trainingCorrectCount += 1
                                } else {
                                    // Modo normal: incrementa progresso da fase e stats gerais
                                    correctThisLevel += 1
                                    totalCorrect += 1
                                }
                                
                                consecutiveCorrect += 1
                                consecutiveWrong = 0
                                
                                // Ganhar XP e Moedas (apenas no modo normal)
                                if (!isInTrainingMode) {
                                    val xpGain = 10 + (consecutiveCorrect * 2)
                                    xp += xpGain
                                    // Aplica o bônus de combo que a tela anuncia (x1.5, x2, x3)
                                    coins += GameDataManager.coinsForCorrectAnswer(comboCount)
                                }
                                
                                // Atualizar estatísticas da operação
                                val opKey = when (question.op) {
                                    Op.ADD -> "add"
                                    Op.SUB -> "sub"
                                    Op.MUL -> "mul"
                                    Op.DIV -> "div"
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
                                
                                // Controla se o desafio diário foi completado *neste* acerto
                                // (evento de uma única vez). Se usarmos apenas `dailyChallenge.completed`,
                                // o estado persistente pode fazer o app repetir o parabéns em acertos futuros.
                                var justCompletedDailyChallengeNow = false

                                // Atualizar desafio diário se a operação corresponder
                                // (funciona tanto no modo normal quanto no modo treino)
                                run {
                                    val challengeOp = dailyChallenge.operation
                                    val currentOp = when (opKey) {
                                        "add" -> Op.ADD
                                        "sub" -> Op.SUB
                                        "mul" -> Op.MUL
                                        "div" -> Op.DIV
                                        else -> Op.ADD
                                    }
                                    
                                    if (currentOp == challengeOp && !dailyChallenge.completed) {
                                        val newProgress = dailyChallenge.progress + 1
                                        val isCompleted = newProgress >= dailyChallenge.targetCorrect
                                        
                                        Log.d("JogoInfantil", "📅 Desafio atualizado: $newProgress/${dailyChallenge.targetCorrect} (Op: $currentOp, Treino: $isInTrainingMode)")
                                        
                                        dailyChallenge = dailyChallenge.copy(
                                            progress = newProgress,
                                            completed = isCompleted
                                        )
                                        
                                        GameDataManager.saveDailyChallengeProgress(prefs, newProgress, isCompleted)
                                        
                                        // Recompensa ao completar desafio
                                        if (isCompleted && newProgress == dailyChallenge.targetCorrect) {
                                            Log.d("JogoInfantil", "🏆 DESAFIO DIÁRIO COMPLETO!")
                                            justCompletedDailyChallengeNow = true
                                            coins += 50
                                            xp += 100
                                            prefs.edit().apply {
                                                putInt("coins", coins)
                                                putInt("xp", xp)
                                                apply()
                                            }
                                        }
                                    }
                                }
                                
                                // Verificar conquistas
                                val newAchievements = GameDataManager.checkAndUnlockAchievements(
                                    prefs, totalCorrect, level, consecutiveCorrect, wrong,
                                    addStats, subStats, mulStats, divStats,
                                    levelCompleted = !isInTrainingMode && correctThisLevel >= config.targetCorrect
                                )
                                if (newAchievements.isNotEmpty()) {
                                    achievements = GameDataManager.loadAchievements(prefs)
                                }
                                
                                // Feedback visual e vibração
                                playSound(true)
                                
                                // Verificar se acabou de completar o desafio diário
                                val justCompletedChallenge = justCompletedDailyChallengeNow
                                
                                // Reforço positivo específico com a conta e resultado
                                val reinforcementMessage = getPositiveReinforcement(
                                    question.text, 
                                    question.correct, 
                                    question.op,
                                    consecutiveCorrect,
                                    responseTime
                                )
                                
                                feedbackMessage = when {
                                    justCompletedChallenge -> "🎊 DESAFIO DIÁRIO COMPLETO!\n+50 moedas +100 XP"
                                    else -> reinforcementMessage
                                }
                                feedbackEmoji = when {
                                    justCompletedChallenge -> "🏆"
                                    consecutiveCorrect >= 10 -> "⚡"
                                    consecutiveCorrect >= 5 -> "🔥"
                                    responseTime < 3000 -> "⚡"
                                    else -> "🎉"
                                }
                                feedbackIsCorrect = true
                                showFeedbackAnimation = true
                                inputsEnabled = false
                                
                                // Salvar XP e moedas (apenas no modo normal)
                                if (!isInTrainingMode) {
                                    prefs.edit().apply {
                                        putInt("xp", xp)
                                        putInt("coins", coins)
                                        apply()
                                    }
                                }
                                
                                showHint = false
                                // Não troca de questão aqui. Apenas marca o que deve acontecer
                                nextAction = if (isInTrainingMode) {
                                    "NEXT_QUESTION"
                                } else if (correctThisLevel >= config.targetCorrect) {
                                    "LEVEL_COMPLETED"
                                } else {
                                    "NEXT_QUESTION"
                                }
                            } else {
                                // ERROU 😢
                                comboCount = 0
                                try {
                                    playSound(false)
                                    attemptsOnCurrentQuestion += 1

                                    // Atualizar estatísticas de erro (somente na primeira vez que erra esta questão)
                                    if (attemptsOnCurrentQuestion == 1) {
                                        // Treino não conta erros da fase nem do jogo principal
                                        if (!isInTrainingMode) {
                                            wrong += 1
                                            totalWrong += 1
                                        }
                                        consecutiveWrong += 1
                                        consecutiveCorrect = 0
                                        
                                        // Salvar questão errada para repetição espaçada
                                        try {
                                            GameDataManager.saveWrongQuestion(prefs, question.text)
                                        } catch (e: Exception) {
                                            Log.e("JogoInfantil", "Erro ao salvar questão errada: ${e.message}")
                                        }

                                        val opKey = when (question.op) {
                                            Op.ADD -> "add"
                                            Op.SUB -> "sub"
                                            Op.MUL -> "mul"
                                            Op.DIV -> "div"
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
                                    }
                                    
                                    // Sistema de dicas progressivas (3 níveis pedagógicos)
                                    when (attemptsOnCurrentQuestion) {
                                        1 -> {
                                            // 1ª TENTATIVA: Dica conceitual - não tira vida
                                            val hint = try { getProgressiveHint(question, level = 1) } catch (_: Exception) { "Pense com calma!" }
                                            feedbackMessage = "Quase! Pense nisso:\n$hint"
                                            feedbackEmoji = "🤔"
                                            feedbackIsCorrect = false
                                            showFeedbackAnimation = true
                                            inputsEnabled = false
                                            
                                            if (!showHint && hintsUsed < 3) {
                                                showHint = true
                                                hintsUsed += 1
                                            }
                                        }
                                        2 -> {
                                            // 2ª TENTATIVA: Estratégia específica - ainda não tira vida
                                            val hint = try { getProgressiveHint(question, level = 2) } catch (_: Exception) { "Vamos tentar de novo!" }
                                            feedbackMessage = "Vou te ajudar mais:\n$hint"
                                            feedbackEmoji = "💡"
                                            feedbackIsCorrect = false
                                            showFeedbackAnimation = true
                                            inputsEnabled = false
                                        }
                                        else -> {
                                            // 3ª TENTATIVA: Passo a passo completo - tira vida
                                            // Treino não tem vidas (antes 3 erros no treino davam game over
                                            // e zeravam o jogo principal). O Escudo Mágico só é gasto aqui,
                                            // quando o erro custaria uma vida; antes era gasto em qualquer
                                            // erro, pulava a questão e o erro nem era registrado.
                                            val shieldSavedLife = !isInTrainingMode && hasActiveShield
                                            if (shieldSavedLife) {
                                                hasActiveShield = false
                                            } else if (!isInTrainingMode) {
                                                lives = maxOf(0, lives - 1)
                                            }
                                            consecutiveWrong += 1
                                            consecutiveCorrect = 0
                                            
                                            val hint = try { getProgressiveHint(question, level = 3) } catch (_: Exception) { "Veja a resposta correta" }
                                            feedbackMessage = (if (shieldSavedLife) "🛡️ O Escudo Mágico protegeu sua vida!\n\n" else "") +
                                                "Veja como resolve:\n$hint\n\n✅ Resposta: ${question.correct}"
                                            feedbackEmoji = if (shieldSavedLife) "🛡️" else "📚"
                                            feedbackIsCorrect = false
                                            showFeedbackAnimation = true
                                            inputsEnabled = false

                                            if (!isInTrainingMode && lives <= 0) {
                                                nextAction = "GAME_OVER"
                                            } else {
                                                nextAction = "NEXT_QUESTION"
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("JogoInfantil", "Erro no fluxo de resposta errada: ${e.message}")
                                    // Fallback: apenas mostra feedback genérico
                                    feedbackMessage = "Ops! Tente novamente!"
                                    feedbackEmoji = "🤔"
                                    feedbackIsCorrect = false
                                    showFeedbackAnimation = true
                                    inputsEnabled = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .testTag("answerButton_$option"),
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
    
    // Animação de feedback
    FeedbackAnimation(
        show = showFeedbackAnimation,
        message = feedbackMessage,
        emoji = feedbackEmoji,
        isCorrect = feedbackIsCorrect,
        onDismiss = {
            showFeedbackAnimation = false
            disabledOptions = setOf()
            when (nextAction) {
                "NEXT_QUESTION" -> {
                    // Sorteia pelo remember de `question`, que inclui a revisão espaçada
                    questionNonce++
                    attemptsOnCurrentQuestion = 0
                    showHint = false
                }
                "LEVEL_COMPLETED" -> {
                    if (currentBoss != null && bossHp <= 0) {
                        showBossVictory = true
                    } else {
                        showCompleted = true
                    }
                }
                "GAME_OVER" -> {
                    showGameOver = true
                }
                else -> Unit
            }

            nextAction = "NONE"

            if (!showGameOver && !showCompleted) {
                inputsEnabled = true
            }
        }
    )
    
    // Micro-lição quando nova operação é introduzida
    showMicroLesson?.let { op ->
        MicroLessonDialog(
            operation = op,
            onDismiss = {
                when (op) {
                    Op.SUB -> {
                        hasIntroducedSub = true
                        prefs.edit().putBoolean("introduced_sub", true).apply()
                    }
                    Op.MUL -> {
                        hasIntroducedMul = true
                        prefs.edit().putBoolean("introduced_mul", true).apply()
                    }
                    Op.DIV -> {
                        hasIntroducedDiv = true
                        prefs.edit().putBoolean("introduced_div", true).apply()
                    }
                    else -> Unit
                }
                showMicroLesson = null
                inputsEnabled = true
            }
        )
    }
}

private fun MainActivity.requestConsent() {
    val params = ConsentRequestParameters.Builder()
        .setTagForUnderAgeOfConsent(true)
        .build()
    val consentInformation = UserMessagingPlatform.getConsentInformation(this)
    Log.d("JogoInfantil", "🔐 Solicitando atualização de consentimento UMP...")
    
    consentInformation.requestConsentInfoUpdate(
        this,
        params,
        {
            Log.d("JogoInfantil", "✅ Informações de consentimento atualizadas")

            if (consentInformation.isConsentFormAvailable) {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    this
                ) { formError ->
                    if (formError != null) {
                        Log.e("JogoInfantil", "❌ Erro ao exibir formulário: ${formError.message}")
                    }
                    // Respeita a decisão da UMP: sem permissão, nenhum anúncio é carregado.
                    MainActivity.canShowAds = consentInformation.canRequestAds()
                    if (MainActivity.canShowAds) initializeMobileAds()
                    Log.d("JogoInfantil", "📊 Anúncios permitidos: ${MainActivity.canShowAds}")
                }
            } else {
                MainActivity.canShowAds = consentInformation.canRequestAds()
                if (MainActivity.canShowAds) initializeMobileAds()
                Log.d("JogoInfantil", "📊 Anúncios permitidos (sem formulário): ${MainActivity.canShowAds}")
            }
        },
        { error ->
            Log.e("JogoInfantil", "❌ Falha ao atualizar consentimento: ${error.message}")
            // Falha no consentimento não pode virar "mostra anúncio mesmo assim".
            MainActivity.canShowAds = false
        }
    )
}

fun shareText(context: Context, title: String, text: String) {
    try {
        val sendIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, "$text\n\nBaixe grátis o Matemática Divertida no Google Play!")
            type = "text/plain"
        }
        val shareIntent = android.content.Intent.createChooser(sendIntent, title)
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        Log.e("JogoInfantil", "Erro ao compartilhar: ${e.message}")
    }
}
