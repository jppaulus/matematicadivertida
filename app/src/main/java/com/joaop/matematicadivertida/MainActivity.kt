package com.joaop.matematicadivertida

import android.Manifest
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
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


private val AppBackgroundColor = Color(0xFFD6E9FC) // Slightly deeper blue for better contrast

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "JogoInfantil"
        private val canShowAdsState = mutableStateOf(false)

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

        // Inicializar AdMob. Nenhum anúncio de tela cheia é pré-carregado: o único
        // formato do app é o banner ancorado, carregado sob demanda por BannerAdView.
        MobileAds.initialize(this) {
            Log.d(TAG, "✅ AdMob inicializado com política para famílias (COPPA & Rating G)")
        }

        // Solicitar consentimento (UMP)
        requestConsent()
        
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
    
    // UI States - Navegação entre telas
    var currentScreen by remember { mutableStateOf("MENU") } // MENU, GAME, STATS, ACHIEVEMENTS, TRAINING, SETTINGS
    var showStats by remember { mutableStateOf(false) }
    var showAchievements by remember { mutableStateOf(false) }
    var showTrainingMode by remember { mutableStateOf(false) }
    var trainingOp by remember { mutableStateOf<Op?>(null) }
    var isInTrainingMode by remember { mutableStateOf(false) }
    var trainingCorrectCount by remember { mutableIntStateOf(0) }
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
    var disabledOptions by remember { mutableStateOf(setOf<Int>()) }
    val currentBoss = remember(level) { GameDataManager.getBossForLevel(level) }
    var bossHp by remember(level) { mutableIntStateOf(currentBoss?.maxHp ?: 100) }
    var showLuckyWheel by remember { mutableStateOf(false) }
    var showBossVictory by remember { mutableStateOf(false) }
    var showDailyRewards by remember { mutableStateOf(false) }
    var showWorldMap by remember { mutableStateOf(false) }

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

    var question by remember(level, correctThisLevel, isInTrainingMode, trainingOp, trainingCorrectCount, questionsAnsweredTotal) {
        mutableStateOf(
            // No modo treino, não injeta questões de revisão para não trocar a operação selecionada.
            if (isInTrainingMode) {
                generateQuestion(config)
            } else {
                // Verificar se há questões para revisão (repetição espaçada)
                if (questionsAnsweredTotal % 5 == 0 && questionsAnsweredTotal > 0) {
                    val reviewQuestions = GameDataManager.getQuestionsForReview(prefs, questionsAnsweredTotal)
                    if (reviewQuestions.isNotEmpty()) {
                        val reviewText = reviewQuestions.random()
                        parseQuestionFromText(reviewText) ?: generateQuestion(config)
                    } else {
                        generateQuestion(config)
                    }
                } else {
                    generateQuestion(config)
                }
            }
        )
    }
    var attemptsOnCurrentQuestion by remember(level, correctThisLevel) { mutableStateOf(0) }
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

    // Tela de game over com Rewarded Ad (Reviver)
    val currentActivity = LocalContext.current as? Activity
    if (showGameOver) {
        GameOverDialog(
            level = level,
            correctAnswers = correctThisLevel,
            onRestart = {
                showGameOver = false
                inputsEnabled = true
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
            },
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
            onSelectLevel = { selectedLvl ->
                level = selectedLvl
                correctThisLevel = 0
                wrong = 0
                lives = 3
                hintsUsed = 0
                showHint = false
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
            "GAME" -> {
                // Se está no jogo, volta para o menu
                currentScreen = "MENU"
                // Se estava no modo treino, limpar o estado
                if (isInTrainingMode) {
                    isInTrainingMode = false
                    trainingOp = null
                    trainingCorrectCount = 0
                }
            }
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
        // TELA DE MENU PRINCIPAL
        Scaffold(
            containerColor = AppBackgroundColor,
            bottomBar = { BannerAdView(modifier = Modifier.fillMaxWidth()) }
        ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo/Título
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎮",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Matemática Divertida",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Aprenda brincando!",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.Gray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Informações do jogador com Avatar e Sequência Diária
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEB3B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar com acionador de troca
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { showAvatarDialog = true }
                                .padding(4.dp)
                        ) {
                            Text(currentAvatar.emoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                currentAvatar.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1976D2)
                            )
                            Text(" ✏️", fontSize = 12.sp)
                        }

                        // Indicador de Sequência Diária (Streak 🔥)
                        Surface(
                            color = Color(0xFFFF9800),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "🔥 $dailyStreak ${if (dailyStreak == 1) "Dia" else "Dias"}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⭐", fontSize = 20.sp)
                            Text(
                                text = "$totalCorrect",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text("Acertos", fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏆", fontSize = 20.sp)
                            Text(
                                text = "Fase $level",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(studentLevelLabel, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💰", fontSize = 20.sp)
                            Text(
                                text = "$coins",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text("Moedas", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botões do menu
            Button(
                onClick = { currentScreen = "GAME" },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "▶️  JOGAR",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Novo Modo: Desafio Relâmpago (Time Attack 60s)
            Button(
                onClick = { currentScreen = "TIME_ATTACK" },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "⚡  DESAFIO RELÂMPAGO (60s)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Roleta Diária da Sorte
            Button(
                onClick = { showLuckyWheel = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "🎡  ROLETA DA SORTE",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Trilha de Mundos (Mapa)
            Button(
                onClick = { showWorldMap = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "🗺️  TRILHA DE MUNDOS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Recompensa Diária (7 Dias)
            val canClaimDaily = remember { GameDataManager.canClaimDailyReward(prefs) }
            Button(
                onClick = { showDailyRewards = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canClaimDaily) Color(0xFFFF9800) else Color(0xFF757575)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (canClaimDaily) "🎁  PRÊMIO DIÁRIO (DISPONÍVEL!)" else "🎁  PRÊMIO DIÁRIO",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))



            Button(
                onClick = { showTrainingMode = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "🎯  MODO TREINO",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { currentScreen = "SETTINGS" },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "⚙️  CONFIGURAÇÕES",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showStats = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📊 STATS", style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    }
                }

                Button(
                    onClick = { showAchievements = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏅 CONQUISTAS", style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    }
                }

                Button(
                    onClick = {
                        shareText(
                            ctx,
                            "Compartilhar Matemática Divertida",
                            "🎮 Aprendendo matemática brincando com o app Matemática Divertida! Consegui $totalCorrect acertos até agora. Baixe você também!"
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📲 DESAFIAR", style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    }
                }
            }

        }
        }
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
                            onClick = {
                                isInTrainingMode = false
                                trainingOp = null
                                trainingCorrectCount = 0
                                currentScreen = "MENU"
                            },
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
                        // Botão Voltar ao Menu
                        Button(
                            onClick = { currentScreen = "MENU" },
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

            // Barra de Boss (se for fase de Boss)
            if (currentBoss != null) {
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
                            if (disabledOptions.isEmpty() && GameDataManager.usePowerUp(prefs, PowerUpType.BOMB_5050)) {
                                val wrongOpts = question.options.filter { it != question.correct }
                                disabledOptions = wrongOpts.shuffled().take(2).toSet()
                                bombCount = GameDataManager.getPowerUpCount(prefs, PowerUpType.BOMB_5050)
                            }
                        },
                        enabled = bombCount > 0 && disabledOptions.isEmpty(),
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
                                
                                if (currentBoss != null) {
                                    bossHp = maxOf(0, bossHp - 35)
                                }

                                
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
                                    coins += 5
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
                                    addStats, subStats, mulStats, divStats
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
                                if (hasActiveShield) {
                                    hasActiveShield = false
                                    playSound(false)
                                    feedbackMessage = "🛡️ Escudo Mágico protegeu você!"
                                    feedbackEmoji = "🛡️"
                                    feedbackIsCorrect = false
                                    showFeedbackAnimation = true
                                    inputsEnabled = false
                                    nextAction = "NEXT_QUESTION"
                                    return@Button
                                }
                                try {
                                    playSound(false)
                                    attemptsOnCurrentQuestion += 1

                                    // Atualizar estatísticas de erro (somente na primeira vez que erra esta questão)
                                    if (attemptsOnCurrentQuestion == 1) {
                                        wrong += 1
                                        totalWrong += 1
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
                                            lives = maxOf(0, lives - 1) // Garantir que lives não fique negativo
                                            consecutiveWrong += 1
                                            consecutiveCorrect = 0
                                            
                                            val hint = try { getProgressiveHint(question, level = 3) } catch (_: Exception) { "Veja a resposta correta" }
                                            feedbackMessage = "Veja como resolve:\n$hint\n\n✅ Resposta: ${question.correct}"
                                            feedbackEmoji = "📚"
                                            feedbackIsCorrect = false
                                            showFeedbackAnimation = true
                                            inputsEnabled = false

                                            if (lives <= 0) {
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
                    try {
                        question = generateQuestion(config)
                        attemptsOnCurrentQuestion = 0
                        showHint = false
                        questionStartTime = System.currentTimeMillis()
                    } catch (e: Exception) {
                        Log.e("JogoInfantil", "Erro ao gerar questão: ${e.message}")
                        // Questão de fallback
                        question = Question("2 + 2 = ?", 4, listOf(4, 3, 5), Op.ADD)
                    }
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
fun MicroLessonDialog(operation: Op, onDismiss: () -> Unit) {
    val (title, emoji, examples, explanation) = when (operation) {
        Op.SUB -> MicroLessonContent(
            "Subtração",
            "➖",
            listOf("5 - 2 = 3", "8 - 3 = 5", "10 - 4 = 6"),
            "Subtrair é TIRAR. Se você tem 5 balas e come 2, sobram 3!"
        )
        Op.MUL -> MicroLessonContent(
            "Multiplicação",
            "✖️",
            listOf("2 × 3 = 6", "3 × 4 = 12", "5 × 2 = 10"),
            "Multiplicar é somar o mesmo número várias vezes. 2 × 3 = 2 + 2 + 2!"
        )
        Op.DIV -> MicroLessonContent(
            "Divisão",
            "➗",
            listOf("6 ÷ 2 = 3", "12 ÷ 3 = 4", "10 ÷ 5 = 2"),
            "Dividir é REPARTIR em partes iguais. 6 balas para 2 amigos = 3 cada!"
        )
        else -> MicroLessonContent("", "", emptyList(), "")
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = emoji,
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Novidade: $title!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        color = Color(0xFF424242)
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "📝 Exemplos:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    examples.forEach { example ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = example,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1976D2)
                                )
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "💪 Vamos praticar agora!",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "✅ Entendi, vamos começar!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = Color(0xFFFFFDE7),
        shape = RoundedCornerShape(20.dp)
    )
}

data class MicroLessonContent(
    val title: String,
    val emoji: String,
    val examples: List<String>,
    val explanation: String
)

@Composable
fun GameOverDialog(
    level: Int,
    correctAnswers: Int,
    onRestart: () -> Unit
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
                Text(
                    text = "Continue praticando e você vai melhorar! 💪",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
    val totalAnswers = totalCorrect + totalWrong
    val accuracy = if (totalAnswers > 0) {
        totalCorrect.toFloat() / totalAnswers
    } else {
        0.5f
    }

    // Configuração base da fase (por nível)
    var cfg = levelConfig(level, totalCorrect)

    // Ajuste fino pela performance RECENTE
    // Se a criança está indo muito bem (>=80% e 5 acertos seguidos), aumenta um pouco o intervalo
    if (accuracy >= 0.8f && consecutiveCorrect >= 5) {
        cfg = cfg.copy(
            min = maxOf(0, cfg.min - 1),
            max = cfg.max + 3,
            description = cfg.description + " ⚡ (ficou um pouquinho mais difícil)"
        )
    }

    // Se está com dificuldade (<50% e já respondeu bastante), reduz o intervalo
    if (accuracy < 0.5f && totalAnswers >= 10) {
        cfg = cfg.copy(
            min = 0,
            max = maxOf(cfg.min + 5, (cfg.max * 0.7f).toInt()),
            targetCorrect = maxOf(3, cfg.targetCorrect - 1),
            description = "🌟 Fase de ajuda: vamos praticar devagar" 
        )
    }

    return cfg
}

fun parseQuestionFromText(text: String): Question? {
    // Tenta reconstruir uma questão do texto salvo (ex: "5 + 3 = ?")
    try {
        val parts = text.replace("=", "").replace("?", "").trim().split(Regex("[+\\-×÷]"))
        if (parts.size != 2) return null
        
        val a = parts[0].trim().toIntOrNull() ?: return null
        val b = parts[1].trim().toIntOrNull() ?: return null
        
        val op = when {
            text.contains("+") -> Op.ADD
            text.contains("-") -> Op.SUB
            text.contains("×") -> Op.MUL
            text.contains("÷") -> Op.DIV
            else -> return null
        }
        
        val correct = when (op) {
            Op.ADD -> a + b
            Op.SUB -> a - b
            Op.MUL -> a * b
            Op.DIV -> if (b != 0 && a % b == 0) a / b else return null
        }
        
        // Gerar opções incorretas
        val options = buildList {
            add(correct)
            var tries = 0
            while (size < 3 && tries < 20) {
                tries++
                val delta = Random.nextInt(1, maxOf(3, correct / 2 + 1))
                val sign = if (Random.nextBoolean()) 1 else -1
                val cand = (correct + sign * delta).coerceAtLeast(0)
                if (cand != correct && cand !in this) add(cand)
            }
        }.shuffled()
        
        return Question(text, correct, options, op)
    } catch (e: Exception) {
        return null
    }
}

fun levelConfig(level: Int, totalCorrect: Int): LevelConfig = when {
    // INÍCIO ABSOLUTO: sempre adição até 10, independente da fase
    totalCorrect < 10 -> LevelConfig(
        ops = listOf(Op.ADD),
        min = 0,
        max = 10,
        targetCorrect = 5,
        description = "Adição bem simples até 10"
    )

    // Depois de 10 acertos: adição até 20
    totalCorrect < 20 -> LevelConfig(
        ops = listOf(Op.ADD),
        min = 0,
        max = 20,
        targetCorrect = 6,
        description = "Adição até 20"
    )

    // 20–39 acertos: adição e subtração até 20
    totalCorrect < 40 -> LevelConfig(
        ops = listOf(Op.ADD, Op.SUB),
        min = 0,
        max = 20,
        targetCorrect = 6,
        description = "Somar e subtrair até 20"
    )

    // 40–59 acertos: adição e subtração até 50
    totalCorrect < 60 -> LevelConfig(
        ops = listOf(Op.ADD, Op.SUB),
        min = 0,
        max = 50,
        targetCorrect = 7,
        description = "Somar e subtrair até 50"
    )

    // 60–89 acertos: introduz multiplicação simples
    totalCorrect < 90 -> LevelConfig(
        ops = listOf(Op.ADD, Op.SUB, Op.MUL),
        min = 0,
        max = 10,
        targetCorrect = 7,
        description = "Adição, subtração e início da multiplicação"
    )

    // 90–119 acertos: tabuada e divisão exata simples
    totalCorrect < 120 -> LevelConfig(
        ops = listOf(Op.ADD, Op.SUB, Op.MUL, Op.DIV),
        min = 0,
        max = 10,
        targetCorrect = 8,
        description = "Quatro operações com números pequenos"
    )

    // 120+ acertos: modo avançado, sobe lentamente com o nível
    else -> {
        val phase = (level - 1).coerceAtLeast(0) / 5
        val minRange = 5 + phase * 5
        val maxRange = 20 + phase * 10
        val target = minOf(10 + phase, 15)
        LevelConfig(
            ops = listOf(Op.ADD, Op.SUB, Op.MUL, Op.DIV),
            min = minRange,
            max = maxRange,
            targetCorrect = target,
            description = "⭐ Desafio progressivo (fase ${level})"
        )
    }
}

fun generateQuestion(cfg: LevelConfig): Question {
    return try {
        // Proteção contra lista de operações vazia
        val ops = if (cfg.ops.isEmpty()) listOf(Op.ADD) else cfg.ops
        val minVal = maxOf(0, minOf(cfg.min, cfg.max))
        val maxVal = maxOf(minVal + 1, maxOf(cfg.min, cfg.max))
        
        val op = ops.random()
        val a = Random.nextInt(minVal, maxVal + 1)
        val b = Random.nextInt(minVal, maxVal + 1)

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
                val result = Random.nextInt(minVal, maxVal + 1)
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

        Question(text, correct, options, op)
    } catch (e: Exception) {
        Log.e("JogoInfantil", "Erro na geração de questão: ${e.message}")
        Question("2 + 2 = ?", 4, listOf(4, 3, 5), Op.ADD)
    }
}

fun getPositiveReinforcement(
    questionText: String, 
    correctAnswer: Int, 
    operation: Op,
    consecutive: Int,
    responseTime: Long
): String {
    // Extrair os números da questão
    val numbers = questionText.replace("=", "").replace("?", "").trim()
    val parts = numbers.split(Regex("[+\\-×÷]")).map { it.trim() }
    val a = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val b = parts.getOrNull(1)?.toIntOrNull() ?: 0
    
    // Mensagens base por operação
    val baseMessages = when (operation) {
        Op.ADD -> listOf(
            "Perfeito! $a + $b = $correctAnswer mesmo! 🎉",
            "Isso aí! Você somou direitinho!",
            "Muito bem! $correctAnswer está certo!",
            "Parabéns! Você é bom em somar!"
        )
        Op.SUB -> listOf(
            "Excelente! $a - $b = $correctAnswer! 👏",
            "Muito bem! Você subtraiu certinho!",
            "Perfeito! $correctAnswer é a resposta!",
            "Ótimo! Você manda bem em subtração!"
        )
        Op.MUL -> listOf(
            "Sensacional! $a × $b = $correctAnswer! ⭐",
            "Isso! Você multiplicou perfeitamente!",
            "Show! $correctAnswer está certinho!",
            "Parabéns! Você domina a multiplicação!"
        )
        Op.DIV -> listOf(
            "Incrível! $a ÷ $b = $correctAnswer! 🌟",
            "Muito bem! Você dividiu como um mestre!",
            "Perfeito! $correctAnswer é isso mesmo!",
            "Excelente! Você arrasa na divisão!"
        )
    }
    
    // Adicionar mensagem de streak ou velocidade
    val prefix = when {
        consecutive >= 10 -> "IMPARÁVEL! "
        consecutive >= 5 -> "EM CHAMA! 🔥 "
        responseTime < 3000 -> "QUE RÁPIDO! ⚡ "
        else -> ""
    }
    
    return prefix + baseMessages.random()
}

fun getProgressiveHint(question: Question, level: Int): String {
    // Extrai os números da pergunta
    val numbers = question.text.replace("=", "").replace("?", "").trim()
    val parts = numbers.split(Regex("[+\\-×÷]")).map { it.trim() }
    val a = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val b = parts.getOrNull(1)?.toIntOrNull() ?: 0
    
    return when (question.op) {
        Op.ADD -> {
            when (level) {
                1 -> // Dica conceitual
                    when {
                        a <= 5 && b <= 5 -> "Use seus dedos para contar!"
                        b <= 5 -> "Comece no $a e conte mais $b"
                        else -> "Que tal separar em partes menores?"
                    }
                2 -> // Estratégia específica
                    when {
                        a <= 5 && b <= 5 -> "Conte nos dedos: $a em uma mão e $b na outra."
                        a <= 10 -> "Comece em $a e conte: ${(a+1)}, ${(a+2)}..."
                        b == 10 -> "Somar 10 é fácil: coloque 1 na frente!"
                        else -> "Some primeiro $a + ${b/2}, depois some mais ${b - b/2}"
                    }
                3 -> // Passo a passo completo
                    when {
                        a <= 5 && b <= 5 -> "Passo 1: Levante $a dedos\nPasso 2: Levante mais $b dedos\nPasso 3: Conte todos: ${question.correct}!"
                        else -> "$a + $b = ?\nPasso 1: Comece em $a\nPasso 2: Some +1 cada vez, $b vezes\nResultado: ${question.correct}"
                    }
                else -> "Tente de novo!"
            }
        }
        Op.SUB -> {
            when (level) {
                1 -> // Dica conceitual
                    when {
                        b <= 5 -> "Conte para trás!"
                        else -> "Quanto falta para $b chegar em $a?"
                    }
                2 -> // Estratégia específica
                    when {
                        b <= 5 -> "Comece em $a e volte $b números."
                        a <= 20 -> "Pense: quanto falta para $b chegar em $a?"
                        b == 10 -> "Tirar 10: diminua 1 da esquerda!"
                        else -> "Tire um pouco de cada vez: primeiro ${b/2}, depois mais ${b - b/2}"
                    }
                3 -> // Passo a passo completo
                    "$a - $b = ?\nPasso 1: Tenho $a\nPasso 2: Tiro $b\nPasso 3: Sobram ${question.correct}!"
                else -> "Tente de novo!"
            }
        }
        Op.MUL -> {
            val smaller = minOf(a, b)
            val bigger = maxOf(a, b)
            when (level) {
                1 -> // Dica conceitual
                    when {
                        smaller == 2 -> "Multiplicar por 2 é dobrar!"
                        smaller <= 5 -> "Some o mesmo número várias vezes"
                        else -> "Use a tabuada!"
                    }
                2 -> // Estratégia específica
                    when {
                        smaller == 2 -> "$bigger × 2 = $bigger + $bigger"
                        smaller == 5 -> "Multiplique por 10 e divida por 2"
                        smaller == 10 -> "Coloque um zero no final!"
                        else -> "Some $bigger, $smaller vezes"
                    }
                3 -> // Passo a passo completo
                    when {
                        smaller <= 3 -> "$bigger × $smaller = $bigger + " + List(smaller - 1) { "$bigger" }.joinToString(" + ") + " = ${question.correct}"
                        else -> "Tabuada do $smaller:\n$bigger × $smaller = ${question.correct}"
                    }
                else -> "Tente de novo!"
            }
        }
        Op.DIV -> {
            when (level) {
                1 -> // Dica conceitual
                    when {
                        b == 2 -> "Dividir por 2 é achar a metade!"
                        b <= 5 -> "Quantos grupos de $b cabem em $a?"
                        else -> "Use a tabuada ao contrário!"
                    }
                2 -> // Estratégia específica
                    when {
                        b == 2 -> "Metade de $a é quanto?"
                        b == 10 -> "Tire o último zero de $a"
                        else -> "Pense: $b vezes o quê dá $a?"
                    }
                3 -> // Passo a passo completo
                    "$a ÷ $b = ?\nPasso 1: Quantos grupos de $b em $a?\nPasso 2: $b × ${question.correct} = $a\nResposta: ${question.correct}!"
                else -> "Tente de novo!"
            }
        }
    }
}

fun getSmartHint(question: Question): String {
    // Função legada - agora usa getProgressiveHint nível 3
    return getProgressiveHint(question, level = 3)
}

fun getHint(question: Question, config: LevelConfig): String {
    val op = config.ops.firstOrNull() ?: Op.ADD
    return when (question.op) {
        Op.ADD -> "Dica: Conte nos dedos ou some os números!"
        Op.SUB -> "Dica: Comece do número maior e conte para trás!"
        Op.MUL -> "Dica: Lembre da tabuada ou some várias vezes!"
        Op.DIV -> "Dica: Quantas vezes cabe? Pense na multiplicação!"
    }
}

private fun ComponentActivity.requestConsent() {
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
                    Log.d("JogoInfantil", "📊 Anúncios permitidos: ${MainActivity.canShowAds}")
                }
            } else {
                MainActivity.canShowAds = consentInformation.canRequestAds()
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

@Composable
fun TimeAttackGameScreen(
    highScore: Int,
    soundPlayer: SoundFeedbackPlayer?,
    onFinish: (Int, Boolean) -> Unit,
    onBack: () -> Unit
) {
    var timeLeft by remember { mutableIntStateOf(60) }
    var score by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }

    val config = remember {
        LevelConfig(
            ops = listOf(Op.ADD, Op.SUB, Op.MUL),
            min = 1,
            max = 12,
            targetCorrect = 999,
            description = "Desafio Relâmpago 60s"
        )
    }

    var question by remember { mutableStateOf(generateQuestion(config)) }

    // Timer countdown
    LaunchedEffect(isGameOver) {
        if (!isGameOver) {
            while (timeLeft > 0) {
                kotlinx.coroutines.delay(1000L)
                timeLeft--
            }
            isGameOver = true
        }
    }

    if (isGameOver) {
        val isNewHigh = score > highScore
        val ctx = LocalContext.current
        Dialog(onDismissRequest = {}) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isNewHigh) "🏆 NOVO RECORDE!" else "⌛ TEMPO ESGOTADO!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("⚡ $score Pontos", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    if (isNewHigh) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Parabéns! Você superou seu recorde anterior ($highScore)! 🔥",
                            fontSize = 13.sp,
                            color = Color(0xFFFF9800),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Seu recorde atual: $highScore", fontSize = 13.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                shareText(
                                    ctx,
                                    "Desafie um Amigo",
                                    "⚡ Fiz $score pontos no Desafio Relâmpago (60s) do Matemática Divertida! Consegue superar meu recorde?"
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                        ) {
                            Text("📲 Desafiar", color = Color.White)
                        }
                        
                        Button(
                            onClick = {
                                onFinish(score, isNewHigh)
                                onBack()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("🏠 Menu", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575))
            ) {
                Text("⬅ Sair", color = Color.White)
            }
            Text(
                "⏱️ ${timeLeft}s",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = if (timeLeft <= 10) Color.Red else Color(0xFF1976D2)
            )
            Text("⚡ $score", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Question card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    question.text,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Options grid
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            question.options.chunked(2).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowOptions.forEach { option ->
                        Button(
                            onClick = {
                                if (option == question.correct) {
                                    score += 10
                                    try { soundPlayer?.playCorrect() } catch(_: Exception) {}
                                } else {
                                    try { soundPlayer?.playWrong() } catch(_: Exception) {}
                                }
                                question = generateQuestion(config)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("$option", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

