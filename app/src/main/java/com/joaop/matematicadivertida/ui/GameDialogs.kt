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
