package com.joaop.matematicadivertida

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private class MenuEntry(val emoji: String, val label: String, val onClick: () -> Unit)

/**
 * Menu principal (P2 do DIAGNOSTICO_RETENCAO.md).
 *
 * Antes eram oito botões empilhados, com rolagem, e o JOGAR disputava atenção com todos.
 * Agora o JOGAR fica grande no topo, as atividades diárias viram uma linha de cartões que
 * avisam o que dá para pegar hoje, e o restante fica num menu secundário recolhido.
 */
@Composable
fun MainMenuScreen(
    avatar: Avatar,
    dailyStreak: Int,
    level: Int,
    studentLevelLabel: String,
    coins: Int,
    timeAttackHighScore: Int,
    canClaimDaily: Boolean,
    canSpinWheel: Boolean,
    onPlay: () -> Unit,
    onAvatarClick: () -> Unit,
    onDailyReward: () -> Unit,
    onLuckyWheel: () -> Unit,
    onTimeAttack: () -> Unit,
    onWorldMap: () -> Unit,
    onTraining: () -> Unit,
    onStats: () -> Unit,
    onAchievements: () -> Unit,
    onSettings: () -> Unit,
    onChallengeFriend: () -> Unit
) {
    var showMore by rememberSaveable { mutableStateOf(false) }

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cabeçalho compacto: avatar, fase, sequência e moedas
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.clickable(onClick = onAvatarClick).padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(avatar.emoji, fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("${avatar.name} ✏️", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1976D2))
                            Text("Fase $level · $studentLevelLabel", fontSize = 12.sp, color = Color(0xFF616161))
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "🔥 $dailyStreak ${if (dailyStreak == 1) "dia" else "dias"}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                        Text("💰 $coins", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                "Matemática Divertida",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF2196F3)),
                textAlign = TextAlign.Center
            )

            // A ação principal, grande e isolada
            Button(
                onClick = onPlay,
                modifier = Modifier.fillMaxWidth().height(96.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(24.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "▶️  JOGAR",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Text("Fase $level", fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
                }
            }

            Text(
                "Todo dia tem novidade",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF424242))
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DailyTile(
                    emoji = "🎁", title = "Prêmio",
                    status = if (canClaimDaily) "Disponível!" else "Volte amanhã",
                    highlight = canClaimDaily, color = Color(0xFFFF9800),
                    onClick = onDailyReward, modifier = Modifier.weight(1f)
                )
                DailyTile(
                    emoji = "🎡", title = "Roleta",
                    status = if (canSpinWheel) "Gire hoje!" else "Volte amanhã",
                    highlight = canSpinWheel, color = Color(0xFF9C27B0),
                    onClick = onLuckyWheel, modifier = Modifier.weight(1f)
                )
                DailyTile(
                    emoji = "⚡", title = "Relâmpago",
                    status = if (timeAttackHighScore > 0) "Recorde: $timeAttackHighScore" else "60 segundos",
                    highlight = false, color = Color(0xFFE91E63),
                    onClick = onTimeAttack, modifier = Modifier.weight(1f)
                )
            }

            OutlinedButton(
                onClick = { showMore = !showMore },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (showMore) "Menos opções ▲" else "Mais opções ▼", fontWeight = FontWeight.Bold)
            }

            if (showMore) {
                val entries = listOf(
                    MenuEntry("🗺️", "Trilha de Mundos", onWorldMap),
                    MenuEntry("🎯", "Modo Treino", onTraining),
                    MenuEntry("📊", "Estatísticas", onStats),
                    MenuEntry("🏅", "Conquistas", onAchievements),
                    MenuEntry("⚙️", "Configurações", onSettings),
                    MenuEntry("📲", "Desafiar amigo", onChallengeFriend)
                )
                entries.chunked(2).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { entry ->
                            OutlinedButton(
                                onClick = entry.onClick,
                                modifier = Modifier.weight(1f).height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("${entry.emoji} ${entry.label}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyTile(
    emoji: String,
    title: String,
    status: String,
    highlight: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(112.dp),
        colors = CardDefaults.cardColors(containerColor = if (highlight) color else Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (highlight) 6.dp else 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(emoji, fontSize = 30.sp)
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (highlight) Color.White else Color(0xFF212121),
                    maxLines = 1
                )
                Text(
                    status,
                    fontSize = 11.sp,
                    color = if (highlight) Color.White else Color(0xFF757575),
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
            if (highlight) {
                // Bolinha de aviso: tem algo para pegar hoje
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(12.dp)
                        .background(Color(0xFFFF1744), CircleShape)
                )
            }
        }
    }
}
