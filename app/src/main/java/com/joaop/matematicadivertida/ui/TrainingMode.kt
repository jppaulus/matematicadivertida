package com.joaop.matematicadivertida

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.joaop.matematicadivertida.Op
import com.joaop.matematicadivertida.Question
import kotlin.random.Random

@Composable
fun TrainingModeSelector(
    onSelectOperation: (Op?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🎓 Modo Treino",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Escolha uma operação para praticar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TrainingOperationButton(
                    emoji = "➕",
                    title = "Adição",
                    description = "Praticar somas",
                    color = Color(0xFF4CAF50)
                ) { onSelectOperation(Op.ADD) }

                TrainingOperationButton(
                    emoji = "➖",
                    title = "Subtração",
                    description = "Praticar subtrações",
                    color = Color(0xFFFF9800)
                ) { onSelectOperation(Op.SUB) }

                TrainingOperationButton(
                    emoji = "✖️",
                    title = "Multiplicação",
                    description = "Praticar multiplicações",
                    color = Color(0xFF9C27B0)
                ) { onSelectOperation(Op.MUL) }

                TrainingOperationButton(
                    emoji = "➗",
                    title = "Divisão",
                    description = "Praticar divisões",
                    color = Color(0xFF2196F3)
                ) { onSelectOperation(Op.DIV) }

                OutlinedButton(
                    onClick = { onSelectOperation(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🔀 Modo Misto", fontSize = 16.sp)
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun TrainingOperationButton(
    emoji: String,
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 32.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

// Gera uma pergunta para modo treino
fun generateTrainingQuestion(op: Op, level: Int): Question {
    val range = when {
        level < 5 -> 1..10
        level < 10 -> 1..20
        level < 15 -> 10..50
        else -> 10..100
    }

    return when (op) {
        Op.ADD -> {
            val a = range.random()
            val b = range.random()
            val correct = a + b
            Question("$a + $b = ?", correct, generateOptions(correct), Op.ADD)
        }
        Op.SUB -> {
            val result = range.random()
            val b = (1..result).random()
            val a = result + b
            Question("$a - $b = ?", result, generateOptions(result), Op.SUB)
        }
        Op.MUL -> {
            val a = (1..12).random()
            val b = (1..12).random()
            val correct = a * b
            Question("$a × $b = ?", correct, generateOptions(correct), Op.MUL)
        }
        Op.DIV -> {
            val b = (2..10).random()
            val result = (1..12).random()
            val a = b * result
            Question("$a ÷ $b = ?", result, generateOptions(result), Op.DIV)
        }
    }
}

private fun generateOptions(correct: Int): List<Int> {
    val options = mutableSetOf(correct)
    while (options.size < 4) {
        val offset = Random.nextInt(-5, 6)
        val option = (correct + offset).coerceAtLeast(0)
        if (option != correct) {
            options.add(option)
        }
    }
    return options.shuffled()
}
