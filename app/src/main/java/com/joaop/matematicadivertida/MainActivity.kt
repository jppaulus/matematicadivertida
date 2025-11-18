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
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.Build
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.gms.ads.*
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "🎮 Iniciando aplicativo...")
        
        // Splash API (Android 12+)
        installSplashScreen()
        enableEdgeToEdge()

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
    
    val config = remember(level, totalCorrect, totalWrong, isInTrainingMode, trainingOp) { 
        if (isInTrainingMode && trainingOp != null) {
            // Modo treino: config simples com apenas uma operação
            val op = trainingOp!!
            val opName = when(op) {
                Op.ADD -> "Adição"
                Op.SUB -> "Subtração"
                Op.MUL -> "Multiplicação"
                Op.DIV -> "Divisão"
            }
            LevelConfig(
                ops = listOf(op),
                min = 0,
                max = if (op == Op.MUL || op == Op.DIV) 10 else 20,
                targetCorrect = 10,
                description = "Modo Treino: $opName"
            )
        } else {
            generateAdaptiveLevel(level, totalCorrect, totalWrong, consecutiveCorrect)
        }
    }

    var question by remember(level, correctThisLevel, isInTrainingMode, trainingCorrectCount, questionsAnsweredTotal) { 
        mutableStateOf(
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

    // Tela de game over sem anúncios
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
                Log.d("JogoInfantil", "🔄 Progresso resetado - começando do zero")
            },
            onWatchAd = {
                // Sem anúncio: apenas dá mais vidas
                showGameOver = false
                inputsEnabled = true
                lives = 3
                wrong = 0
            },
            hasRewardedAd = false
        )
    }

    // Tela de fase completa sem interstitial
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

    Scaffold(
        containerColor = AppBackgroundColor,
        bottomBar = {}
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
            if (isInTrainingMode && trainingOp != null) {
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
                            val opName = when(trainingOp) {
                                Op.ADD -> "Adição"
                                Op.SUB -> "Subtração"
                                Op.MUL -> "Multiplicação"
                                Op.DIV -> "Divisão"
                                else -> "Treino"
                            }
                            Text(
                                text = "🎯 Modo Treino: $opName",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "✅ $trainingCorrectCount/10 acertos",
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
                        Column(
                            horizontalAlignment = Alignment.Start
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
                    // Limite de dicas atingido - apenas desabilita botão extra
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
                            if (showFeedbackAnimation || !inputsEnabled || showGameOver || showCompleted) {
                                return@Button
                            }
                            val responseTime = System.currentTimeMillis() - questionStartTime
                            
                            if (option == question.correct) {
                                // ACERTOU! 🎉
                                attemptsOnCurrentQuestion = 0
                                
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
                                
                                // Atualizar desafio diário se a operação corresponder
                                if (!isInTrainingMode) {
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
                                        
                                        Log.d("JogoInfantil", "📅 Desafio atualizado: $newProgress/${dailyChallenge.targetCorrect} (Op: $currentOp)")
                                        
                                        dailyChallenge = dailyChallenge.copy(
                                            progress = newProgress,
                                            completed = isCompleted
                                        )
                                        
                                        GameDataManager.saveDailyChallengeProgress(prefs, newProgress, isCompleted)
                                        
                                        // Recompensa ao completar desafio
                                        if (isCompleted && newProgress == dailyChallenge.targetCorrect) {
                                            Log.d("JogoInfantil", "🏆 DESAFIO DIÁRIO COMPLETO!")
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
                                val justCompletedChallenge = dailyChallenge.completed && 
                                    dailyChallenge.progress == dailyChallenge.targetCorrect
                                
                                // Reforço positivo específico com a conta e resultado
                                val operationSymbol = when {
                                    question.text.contains("+") -> "+"
                                    question.text.contains("-") -> "-"
                                    question.text.contains("×") -> "×"
                                    question.text.contains("÷") -> "÷"
                                    else -> ""
                                }
                                
                                val reinforcementMessage = getPositiveReinforcement(
                                    question.text, 
                                    question.correct, 
                                    operationSymbol,
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
                                    if (trainingCorrectCount >= 10) {
                                        "TRAINING_COMPLETED"
                                    } else {
                                        "NEXT_QUESTION"
                                    }
                                } else if (correctThisLevel >= config.targetCorrect) {
                                    "LEVEL_COMPLETED"
                                } else {
                                    "NEXT_QUESTION"
                                }
                            } else {
                                // ERROU 😢
                                attemptsOnCurrentQuestion += 1

                                // Atualizar estatísticas de erro (somente na primeira vez que erra esta questão)
                                if (attemptsOnCurrentQuestion == 1) {
                                    wrong += 1
                                    totalWrong += 1
                                    consecutiveWrong += 1
                                    consecutiveCorrect = 0
                                    
                                    // Salvar questão errada para repetição espaçada
                                    GameDataManager.saveWrongQuestion(prefs, question.text)

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
                                }
                                
                                // Sistema de dicas progressivas (3 níveis pedagógicos)
                                when (attemptsOnCurrentQuestion) {
                                    1 -> {
                                        // 1ª TENTATIVA: Dica conceitual - não tira vida
                                        val hint = getProgressiveHint(question, level = 1)
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
                                        val hint = getProgressiveHint(question, level = 2)
                                        feedbackMessage = "Vou te ajudar mais:\n$hint"
                                        feedbackEmoji = "💡"
                                        feedbackIsCorrect = false
                                        showFeedbackAnimation = true
                                        inputsEnabled = false
                                    }
                                    else -> {
                                        // 3ª TENTATIVA: Passo a passo completo - tira vida
                                        lives -= 1
                                        consecutiveWrong += 1
                                        consecutiveCorrect = 0
                                        
                                        val hint = getProgressiveHint(question, level = 3)
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
                isInTrainingMode = true
                trainingCorrectCount = 0
                showTrainingMode = false
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
        onDismiss = {
            showFeedbackAnimation = false
            when (nextAction) {
                "NEXT_QUESTION" -> {
                    question = generateQuestion(config)
                    attemptsOnCurrentQuestion = 0
                    showHint = false
                    questionStartTime = System.currentTimeMillis()
                }
                "LEVEL_COMPLETED" -> {
                    showCompleted = true
                }
                "GAME_OVER" -> {
                    showGameOver = true
                }
                "TRAINING_COMPLETED" -> {
                    // Completou 10 acertos no treino - reseta modo treino
                    isInTrainingMode = false
                    trainingOp = null
                    trainingCorrectCount = 0
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
        
        return Question(text, correct, options)
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

fun getPositiveReinforcement(
    questionText: String, 
    correctAnswer: Int, 
    operation: String,
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
        "+" -> listOf(
            "Perfeito! $a + $b = $correctAnswer mesmo! 🎉",
            "Isso aí! Você somou direitinho!",
            "Muito bem! $correctAnswer está certo!",
            "Parabéns! Você é bom em somar!"
        )
        "-" -> listOf(
            "Excelente! $a - $b = $correctAnswer! 👏",
            "Muito bem! Você subtraiu certinho!",
            "Perfeito! $correctAnswer é a resposta!",
            "Ótimo! Você manda bem em subtração!"
        )
        "×" -> listOf(
            "Sensacional! $a × $b = $correctAnswer! ⭐",
            "Isso! Você multiplicou perfeitamente!",
            "Show! $correctAnswer está certinho!",
            "Parabéns! Você domina a multiplicação!"
        )
        "÷" -> listOf(
            "Incrível! $a ÷ $b = $correctAnswer! 🌟",
            "Muito bem! Você dividiu como um mestre!",
            "Perfeito! $correctAnswer é isso mesmo!",
            "Excelente! Você arrasa na divisão!"
        )
        else -> listOf("Muito bem!", "Parabéns!", "Perfeito!", "Excelente!")
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
    
    return when {
        question.text.contains("+") -> {
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
        question.text.contains("-") -> {
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
        question.text.contains("×") -> {
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
        question.text.contains("÷") -> {
            val a = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val b = parts.getOrNull(1)?.toIntOrNull() ?: 0
            when {
                a <= 5 && b <= 5 -> "Conte nos dedos: $a em uma mão e $b na outra."
                a <= 10 -> "Comece em $a e conte mais $b número(s)."
                b == 10 -> "Somar 10: coloque um zero a mais em $a."
                b <= 5 -> "Pense: $a e mais $b. Conte para frente."
                else -> "Quebre em partes: some um pedaço de cada vez."
            }
        }
        question.text.contains("-") -> {
            val a = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val b = parts.getOrNull(1)?.toIntOrNull() ?: 0
            when {
                b <= 5 -> "Comece em $a e volte $b número(s) para trás."
                a <= 20 -> "Veja quanto falta para $b chegar em $a."
                b == 10 -> "Tirar 10: diminua uma dezena de $a."
                else -> "Tire um pouco de cada vez até chegar."
            }
        }
        question.text.contains("×") -> {
            val a = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val b = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val smaller = minOf(a, b)
            val bigger = maxOf(a, b)
            when {
                smaller == 2 -> "Multiplicar por 2 é dobrar: $bigger + $bigger."
                smaller == 3 -> "Some o mesmo número 3 vezes: $bigger + $bigger + $bigger."
                smaller == 5 -> "Multiplicar por 5 é metade de vezes 10."
                smaller == 10 -> "Multiplicar por 10: coloque um zero no final."
                bigger <= 5 -> "Some $smaller vezes o número $bigger."
                else -> "Use a tabuada do menor número."
            }
        }
        question.text.contains("÷") -> {
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
        else -> when (level) {
            1 -> "Leia com calma!"
            2 -> "Pense bem na operação"
            else -> "Faça passo a passo"
        }
    }
}

fun getSmartHint(question: Question): String {
    // Função legada - agora usa getProgressiveHint nível 3
    return getProgressiveHint(question, level = 3)
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
