package com.joaop.matematicadivertida

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun StatsScreen(
    addStats: OperationStats,
    subStats: OperationStats,
    mulStats: OperationStats,
    divStats: OperationStats,
    totalCorrect: Int,
    totalWrong: Int,
    level: Int,
    xp: Int,
    playerLevel: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📊 Estatísticas",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                    )
                    TextButton(onClick = onDismiss) {
                        Text("✖", fontSize = 24.sp, color = Color(0xFF757575))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Resumo geral
                    item {
                        StatsCard(
                            title = "📈 Resumo Geral",
                            content = {
                                StatRow("Fase Atual", "$level")
                                StatRow("Nível do Jogador", "$playerLevel")
                                StatRow("XP", "$xp")
                                StatRow("Total de Acertos", "$totalCorrect")
                                StatRow("Total de Erros", "$totalWrong")
                                val accuracy = if (totalCorrect + totalWrong > 0) 
                                    (totalCorrect * 100f / (totalCorrect + totalWrong)).toInt() 
                                else 0
                                StatRow("Taxa de Acerto", "$accuracy%")
                            }
                        )
                    }
                    
                    // Estatísticas por operação
                    item {
                        OperationStatsCard("➕ Adição", addStats)
                    }
                    item {
                        OperationStatsCard("➖ Subtração", subStats)
                    }
                    item {
                        OperationStatsCard("✖️ Multiplicação", mulStats)
                    }
                    item {
                        OperationStatsCard("➗ Divisão", divStats)
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color(0xFF757575))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
    }
}

@Composable
fun OperationStatsCard(title: String, stats: OperationStats) {
    StatsCard(title) {
        StatRow("Acertos", "${stats.correct}")
        StatRow("Erros", "${stats.wrong}")
        StatRow("Taxa de Acerto", "${(stats.accuracy * 100).toInt()}%")
        StatRow("Tempo Médio", "${(stats.avgTime / 1000).toInt()}s")
        
        // Barra de progresso
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { stats.accuracy },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = when {
                stats.accuracy >= 0.8f -> Color(0xFF4CAF50)
                stats.accuracy >= 0.6f -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            },
            trackColor = Color(0xFFE0E0E0)
        )
    }
}

@Composable
fun AchievementsScreen(
    achievements: List<Achievement>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🏆 Conquistas",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                    )
                    TextButton(onClick = onDismiss) {
                        Text("✖", fontSize = 24.sp, color = Color(0xFF757575))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val unlockedCount = achievements.count { it.unlocked }
                Text(
                    "$unlockedCount de ${achievements.size} conquistadas",
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(achievements) { achievement ->
                        AchievementCard(achievement)
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.unlocked) Color.White else Color(0xFFE0E0E0)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (achievement.unlocked) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                achievement.emoji,
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    achievement.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (achievement.unlocked) Color(0xFF212121) else Color(0xFF9E9E9E)
                )
                Text(
                    achievement.description,
                    fontSize = 12.sp,
                    color = if (achievement.unlocked) Color(0xFF757575) else Color(0xFFBDBDBD)
                )
            }
            if (achievement.unlocked) {
                Text("✓", fontSize = 24.sp, color = Color(0xFF4CAF50))
            } else {
                Text("🔒", fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun FeedbackAnimation(
    show: Boolean,
    message: String,
    emoji: String,
    isCorrect: Boolean,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = show,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var scale by remember { mutableFloatStateOf(0.5f) }
                    LaunchedEffect(Unit) {
                        scale = 1.5f
                    }
                    
                    Text(
                        emoji,
                        fontSize = 72.sp,
                        modifier = Modifier.scale(
                            animateFloatAsState(
                                targetValue = scale,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy
                                ), label = ""
                            ).value
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        message,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1500)
            onDismiss()
        }
    }
}

@Composable
fun DailyChallengeCard(
    challenge: DailyChallenge,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (challenge.completed) Color(0xFF4CAF50) else Color(0xFFFFEB3B)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🎯 Desafio Diário",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (challenge.completed) Color.White else Color(0xFF212121)
                )
                if (challenge.completed) {
                    Text("✓ Completo!", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                challenge.description,
                fontSize = 14.sp,
                color = if (challenge.completed) Color.White.copy(alpha = 0.9f) else Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { challenge.progress.toFloat() / challenge.targetCorrect },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (challenge.completed) Color.White else Color(0xFF4CAF50),
                trackColor = if (challenge.completed) Color.White.copy(alpha = 0.3f) else Color(0xFFE0E0E0)
            )
            Text(
                "${challenge.progress}/${challenge.targetCorrect}",
                fontSize = 12.sp,
                color = if (challenge.completed) Color.White.copy(alpha = 0.8f) else Color(0xFF757575),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
