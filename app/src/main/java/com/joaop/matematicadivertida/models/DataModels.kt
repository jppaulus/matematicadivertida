package com.joaop.matematicadivertida

// Conquistas
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val unlocked: Boolean = false
)

// Estatísticas por operação
data class OperationStats(
    val correct: Int = 0,
    val wrong: Int = 0,
    val totalTime: Long = 0,
    val count: Int = 0
) {
    val accuracy: Float get() = if (correct + wrong > 0) correct.toFloat() / (correct + wrong) else 0f
    val avgTime: Float get() = if (count > 0) totalTime.toFloat() / count else 0f
}

// Desafio diário
data class DailyChallenge(
    val date: String,
    val description: String,
    val targetCorrect: Int,
    val operation: Op,
    val completed: Boolean = false,
    val progress: Int = 0
)

data class Question(val text: String, val correct: Int, val options: List<Int>)

enum class Op { ADD, SUB, MUL, DIV }

data class LevelConfig(
    val ops: List<Op>,
    val min: Int,
    val max: Int,
    val targetCorrect: Int = 5,
    val description: String = ""
)

fun Op.toPortuguese(): String = when(this) {
    Op.ADD -> "Adição"
    Op.SUB -> "Subtração"
    Op.MUL -> "Multiplicação"
    Op.DIV -> "Divisão"
}

fun Op.toSymbol(): String = when(this) {
    Op.ADD -> "+"
    Op.SUB -> "-"
    Op.MUL -> "×"
    Op.DIV -> "÷"
}
