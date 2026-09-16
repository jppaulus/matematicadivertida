package com.joaop.matematicadivertida

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class OnboardingPage(val emoji: String, val title: String, val body: String)

/**
 * Onboarding curto do primeiro uso (P2 do DIAGNOSTICO_RETENCAO.md). São três telas, e o botão
 * final já abre a primeira partida em vez de largar a criança num menu cheio de opções.
 */
@Composable
fun OnboardingScreen(onFinish: (startPlaying: Boolean) -> Unit) {
    val pages = remember {
        listOf(
            OnboardingPage("👋", "Olá! Vamos aprender brincando?", "Resolva continhas, ganhe moedas e desbloqueie avatares."),
            OnboardingPage("💡", "Errou? Tudo bem!", "O jogo dá dicas passo a passo para você descobrir a resposta."),
            OnboardingPage("🔥", "Volte todo dia", "Mantenha sua sequência, gire a roleta e pegue seu prêmio diário.")
        )
    }
    var index by rememberSaveable { mutableIntStateOf(0) }
    val page = pages[index]
    val isLast = index == pages.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(48.dp), horizontalArrangement = Arrangement.End) {
            if (!isLast) {
                TextButton(onClick = { onFinish(false) }) { Text("Pular") }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(page.emoji, fontSize = 96.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            page.title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1976D2)),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(page.body, fontSize = 18.sp, color = Color(0xFF424242), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            pages.indices.forEach { i ->
                Box(
                    modifier = Modifier
                        .size(if (i == index) 12.dp else 8.dp)
                        .background(if (i == index) Color(0xFF2196F3) else Color(0xFFBDBDBD), CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { if (isLast) onFinish(true) else index++ },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                if (isLast) "▶️  Começar a jogar" else "Próximo",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/** Convite para o lembrete diário, mostrado antes do pedido de permissão do Android 13+. */
@Composable
fun ReminderOptInDialog(onAccept: () -> Unit, onDecline: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDecline,
        title = { Text("🔔 Quer um lembrete diário?", fontWeight = FontWeight.Bold) },
        text = {
            Text("A gente avisa no fim da tarde para você não perder sua sequência 🔥 e pegar o prêmio do dia.")
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) { Text("Sim, quero!") }
        },
        dismissButton = {
            TextButton(onClick = onDecline) { Text("Agora não") }
        }
    )
}
