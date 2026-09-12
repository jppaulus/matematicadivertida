package com.joaop.matematicadivertida

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Um dos três atalhos diários do menu (roleta, prêmio, desafio relâmpago).
 *
 * A criança lê o menu pela cor, não pelo texto — é isso que faz o botão certo saltar
 * aos olhos sem precisar ler nada.
 *
 * @param ativo cartão colorido (tem o que fazer) ou cinza (já usado hoje).
 * @param temNovidade desenha o selo vermelho de "tem coisa nova aqui".
 * @param modifier o chamador passa `Modifier.weight(1f)` para os três dividirem a linha.
 */
@Composable
fun DailyActionCard(
    emoji: String,
    label: String,
    color: Color,
    ativo: Boolean,
    temNovidade: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = if (ativo) color else Color(0xFFCFD8DC)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (ativo) 6.dp else 1.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = emoji, fontSize = 30.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (ativo) Color.White else Color(0xFF546E7A)
                    ),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp
                )
            }
        }

        if (temNovidade) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(20.dp)
                    .background(color = Color(0xFFF44336), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * Botão do bloco secundário do menu ("Mais opções").
 *
 * Tudo que não é o caminho principal — treino, mapa, estatísticas, conquistas,
 * configurações — vive aqui, para o menu inicial não competir com o botão JOGAR.
 */
@Composable
fun SecondaryMenuButton(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF37474F)
                )
            )
        }
    }
}
