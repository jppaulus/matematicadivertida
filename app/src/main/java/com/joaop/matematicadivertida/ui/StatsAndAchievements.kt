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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.clickable

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
            progress = stats.accuracy,
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
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { onDismiss() },
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
    }
}

@Composable
fun DailyChallengeCard(
    challenge: DailyChallenge,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (challenge.completed) Color(0xFF4CAF50) else Color(0xFFFFEB3B)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
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
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                challenge.description,
                fontSize = 11.sp,
                color = if (challenge.completed) Color.White.copy(alpha = 0.9f) else Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(2.dp))
            LinearProgressIndicator(
                progress = { challenge.progress.toFloat() / challenge.targetCorrect },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = if (challenge.completed) Color.White else Color(0xFF4CAF50),
                trackColor = if (challenge.completed) Color.White.copy(alpha = 0.3f) else Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "${challenge.progress}/${challenge.targetCorrect}",
                fontSize = 11.sp,
                color = if (challenge.completed) Color.White.copy(alpha = 0.8f) else Color(0xFF757575)
            )
        }
    }
}

@Composable
fun AvatarSelectionDialog(
    avatars: List<Avatar>,
    playerLevel: Int,
    selectedAvatarId: String,
    onSelectAvatar: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🎭 Escolha seu Avatar",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                    )
                    TextButton(onClick = onDismiss) {
                        Text("✖", fontSize = 20.sp, color = Color(0xFF757575))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(avatars) { avatar ->
                        val isUnlocked = playerLevel >= avatar.minLevel
                        val isSelected = avatar.id == selectedAvatarId
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isUnlocked) { onSelectAvatar(avatar.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isSelected -> Color(0xFFBBDEFB)
                                    isUnlocked -> Color.White
                                    else -> Color(0xFFE0E0E0)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(avatar.emoji, fontSize = 32.sp, modifier = Modifier.padding(end = 12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        avatar.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (isUnlocked) Color(0xFF212121) else Color(0xFF9E9E9E)
                                    )
                                    if (!isUnlocked) {
                                        Text("Requer Nível ${avatar.minLevel}", fontSize = 12.sp, color = Color(0xFFD32F2F))
                                    }
                                }
                                if (isSelected) {
                                    Text("✅", fontSize = 20.sp)
                                } else if (!isUnlocked) {
                                    Text("🔒", fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LuckyWheelDialog(
    onReward: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var isSpinning by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var rewardResult by remember { mutableStateOf<String?>(null) }

    val rewards = remember {
        listOf(
            "💰 50 Moedas" to 50,
            "🛡️ Escudo Mágico" to 1,
            "💰 100 Moedas" to 100,
            "🔮 Bomba 50/50" to 1,
            "⭐ Bônus de XP" to 30,
            "💰 200 Moedas!" to 200
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🎡 Roleta da Sorte!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(140.dp)
                ) {
                    Text(
                        "🎰",
                        fontSize = 72.sp,
                        modifier = Modifier.rotate(
                            animateFloatAsState(
                                targetValue = if (isSpinning) rotationAngle + 1080f else rotationAngle,
                                animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing),
                                label = ""
                            ).value
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (rewardResult != null) {
                    Text(
                        "🎉 Você Ganhou: $rewardResult!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("✅ Resgatar Prêmio", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = {
                            if (!isSpinning) {
                                isSpinning = true
                                rotationAngle += 720f + (0..360).random()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        enabled = !isSpinning
                    ) {
                        Text(if (isSpinning) "Girando..." else "🎲 GIRAR ROLETA!", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                LaunchedEffect(isSpinning) {
                    if (isSpinning) {
                        kotlinx.coroutines.delay(2000L)
                        isSpinning = false
                        val won = rewards.random()
                        rewardResult = won.first
                        onReward(won.first, won.second)
                    }
                }
            }
        }
    }
}

@Composable
fun BossVictoryDialog(
    bossName: String,
    rewardCoins: Int,
    onContinue: () -> Unit
) {
    Dialog(onDismissRequest = onContinue) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🏆 CHEFÃO DERROTADO!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.height(12.dp))
                Text("⚔️ Você venceu o $bossName!", fontSize = 16.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Text("💰 +$rewardCoins Moedas!", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🚀 CONTINUAR JORNADA", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DailyRewardsDialog(
    currentDay: Int,
    canClaim: Boolean,
    onClaim: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val daysRewards = remember {
        listOf(
            1 to ("💰 50 Moedas" to "50 Moedas"),
            2 to ("🛡️ Escudo" to "1 Escudo"),
            3 to ("💰 100 Moedas" to "100 Moedas"),
            4 to ("🔮 Bomba" to "1 Bomba 50/50"),
            5 to ("💰 150 Moedas" to "150 Moedas"),
            6 to ("⭐ 200 XP" to "200 XP"),
            7 to ("🎁 SUPER BAÚ" to "300 Moedas + 2 Escudos")
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🎁 Recompensa Diária (7 Dias)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.height(12.dp))

                daysRewards.chunked(4).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { (day, reward) ->
                            val isClaimed = day < currentDay || (day == currentDay && !canClaim)
                            val isCurrent = day == currentDay && canClaim

                            Card(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        isClaimed -> Color(0xFFC8E6C9)
                                        isCurrent -> Color(0xFFFFD54F)
                                        else -> Color.White
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Dia $day", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(reward.first.take(2), fontSize = 20.sp)
                                    Text(reward.second, fontSize = 9.sp, textAlign = TextAlign.Center, maxLines = 1)
                                    if (isClaimed) Text("✅", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (canClaim) {
                    Button(
                        onClick = { onClaim(currentDay) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🎁 RESGATAR DIA $currentDay", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Volte amanhã para mais prêmios! ✅", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun WorldMapDialog(
    currentLevel: Int,
    onSelectLevel: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val worlds = remember {
        listOf(
            Triple(1, "🌲 Floresta da Soma", 1..5),
            Triple(2, "🏜️ Deserto da Subtração", 6..10),
            Triple(3, "🏰 Castelo da Tabuada", 11..15),
            Triple(4, "🌋 Vulcão da Divisão", 16..20),
            Triple(5, "🌌 Dimensão do Caos", 21..30)
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🗺️ Trilha de Mundos",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0288D1)
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(worlds) { (worldNum, worldName, range) ->
                        val isUnlocked = currentLevel >= range.first
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUnlocked) Color.White else Color(0xFFEEEEEE)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    worldName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isUnlocked) Color(0xFF0288D1) else Color.Gray
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    range.forEach { lvl ->
                                        val lvlUnlocked = lvl <= currentLevel
                                        val isBossLvl = lvl % 5 == 0
                                        Button(
                                            onClick = { if (lvlUnlocked) onSelectLevel(lvl) },
                                            enabled = lvlUnlocked,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = when {
                                                    lvl == currentLevel -> Color(0xFFFF9800)
                                                    isBossLvl -> Color(0xFFD32F2F)
                                                    lvlUnlocked -> Color(0xFF4CAF50)
                                                    else -> Color.Gray
                                                }
                                            ),
                                            modifier = Modifier.size(36.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(
                                                text = if (isBossLvl) "👾" else "$lvl",
                                                fontSize = 12.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fechar Mapa", color = Color.White)
                }
            }
        }
    }
}



