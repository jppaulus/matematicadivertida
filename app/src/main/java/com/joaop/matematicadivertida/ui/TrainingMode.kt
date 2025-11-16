package com.joaop.matematicadivertida

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun TrainingModeSelector(
    onSelectOperation: (Op?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🎓 Modo Treino",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                )
                Text(
                    "Escolha uma operação para praticar",
                    fontSize = 14.sp,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )
                
                TrainingOperationButton(
                    emoji = "➕",
                    title = "Adição",
                    description = "Praticar somas",
                    color = Color(0xFF4CAF50),
                    onClick = { onSelectOperation(Op.ADD) }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TrainingOperationButton(
                    emoji = "➖",
                    title = "Subtração",
                    description = "Praticar subtrações",
                    color = Color(0xFFFF9800),
                    onClick = { onSelectOperation(Op.SUB) }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TrainingOperationButton(
                    emoji = "✖️",
                    title = "Multiplicação",
                    description = "Praticar multiplicações",
                    color = Color(0xFF9C27B0),
                    onClick = { onSelectOperation(Op.MUL) }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TrainingOperationButton(
                    emoji = "➗",
                    title = "Divisão",
                    description = "Praticar divisões",
                    color = Color(0xFF2196F3),
                    onClick = { onSelectOperation(Op.DIV) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = { onSelectOperation(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🔀 Modo Misto", fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
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
                Text(
                    title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    description,
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
            val options = generateOptions(correct)
            Question("$a + $b = ?", correct, options)
        }
        Op.SUB -> {
            val result = range.random()
            val b = (1..result).random()
            val a = result + b
            val options = generateOptions(result)
            Question("$a - $b = ?", result, options)
        }
        Op.MUL -> {
            val a = (1..12).random()
            val b = (1..12).random()
            val correct = a * b
            val options = generateOptions(correct)
            Question("$a × $b = ?", correct, options)
        }
        Op.DIV -> {
            val b = (2..10).random()
            val result = (1..12).random()
            val a = b * result
            val options = generateOptions(result)
            Question("$a ÷ $b = ?", result, options)
        }
    }
}

private fun generateOptions(correct: Int): List<Int> {
    val options = mutableSetOf(correct)
    while (options.size < 4) {
        val offset = (-5..5).random()
        val option = (correct + offset).coerceAtLeast(0)
        if (option != correct) {
            options.add(option)
        }
    }
    return options.shuffled()
}
