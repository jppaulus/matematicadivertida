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

    // Sem isto o botão voltar do sistema fechava o app durante o Desafio Relâmpago
    BackHandler(onBack = onBack)

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
