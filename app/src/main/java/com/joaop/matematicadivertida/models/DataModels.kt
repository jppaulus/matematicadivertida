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

data class Question(
    val text: String,
    val correct: Int,
    val options: List<Int>,
    val op: Op
)

enum class Op { 
    ADD, SUB, MUL, DIV;
    
    val symbol: String get() = when(this) {
        ADD -> "+"
        SUB -> "-"
        MUL -> "×"
        DIV -> "÷"
    }
}

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

// Avatares desbloqueáveis
data class Avatar(
    val id: String,
    val name: String,
    val emoji: String,
    val minLevel: Int
)

// Desafio Relâmpago (Time Attack)
data class TimeAttackResult(
    val score: Int,
    val isNewHighScore: Boolean
)

// Boss Battle
data class BossInfo(
    val name: String,
    val emoji: String,
    val maxHp: Int,
    val rewardCoins: Int,
    val description: String
)

enum class PowerUpType { SHIELD, BOMB_5050, FREEZE }

data class PowerUp(
    val type: PowerUpType,
    val name: String,
    val icon: String,
    val cost: Int,
    val description: String
)

