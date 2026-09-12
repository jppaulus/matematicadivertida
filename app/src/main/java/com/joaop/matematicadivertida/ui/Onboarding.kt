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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Um passo do onboarding. */
private data class PassoOnboarding(
    val emoji: String,
    val titulo: String,
    val texto: String
)

private val PASSOS = listOf(
    PassoOnboarding(
        emoji = "👋",
        titulo = "Oi! Vamos brincar de matemática?",
        texto = "Aqui você responde continhas e sobe de fase. Cada fase vem com questões novas e um pouquinho mais difíceis."
    ),
    PassoOnboarding(
        emoji = "🔥",
        titulo = "Volte um pouquinho todo dia",
        texto = "Jogando todo dia você mantém a sequência, gira a Roleta da Sorte e pega o prêmio diário."
    ),
    PassoOnboarding(
        emoji = "🔔",
        titulo = "Posso te lembrar?",
        texto = "Um aviso por dia, no fim da tarde, para a sequência não se perder. Dá para desligar quando quiser em Configurações."
    )
)

/**
 * Boas-vindas da primeira execução.
 *
 * Existe por dois motivos: a criança cai direto num menu cheio sem saber o que o app é,
 * e a permissão de notificação precisa ser pedida num momento em que a pergunta faça
 * sentido — não na primeira tela, do nada.
 *
 * @param onFinish recebe `true` quando a pessoa aceitou receber o lembrete diário.
 */
@Composable
fun OnboardingScreen(onFinish: (Boolean) -> Unit) {
    var passoAtual by rememberSaveable { mutableIntStateOf(0) }
    val passo = PASSOS[passoAtual]
    val ehUltimoPasso = passoAtual == PASSOS.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = passo.emoji, fontSize = 72.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = passo.titulo,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = passo.texto,
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF424242)),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bolinhas de progresso
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PASSOS.indices.forEach { indice ->
                Box(
                    modifier = Modifier
                        .size(if (indice == passoAtual) 14.dp else 10.dp)
                        .background(
                            color = if (indice == passoAtual) Color(0xFF1976D2) else Color(0xFFB0BEC5),
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (ehUltimoPasso) {
            Button(
                onClick = { onFinish(true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = "🔔  PODE ME LEMBRAR",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { onFinish(false) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Agora não",
                    style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF616161))
                )
            }
        } else {
            Button(
                onClick = { passoAtual += 1 },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = "CONTINUAR  ▶️",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { onFinish(false) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Pular",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF616161))
                )
            }
        }
    }
}
