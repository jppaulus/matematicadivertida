package com.joaop.matematicadivertida

import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

object GameDataManager {
    
    fun loadOperationStats(prefs: SharedPreferences, op: String): OperationStats {
        return OperationStats(
            correct = prefs.getInt("${op}_correct", 0),
            wrong = prefs.getInt("${op}_wrong", 0),
            totalTime = prefs.getLong("${op}_time", 0),
            count = prefs.getInt("${op}_count", 0)
        )
    }

    fun saveOperationStats(prefs: SharedPreferences, op: String, stats: OperationStats) {
        prefs.edit().apply {
            putInt("${op}_correct", stats.correct)
            putInt("${op}_wrong", stats.wrong)
            putLong("${op}_time", stats.totalTime)
            putInt("${op}_count", stats.count)
            apply()
        }
    }

    fun loadAchievements(prefs: SharedPreferences): List<Achievement> {
        val unlocked = prefs.getStringSet("achievements", emptySet()) ?: emptySet()
        return listOf(
            Achievement("first_correct", "Primeiro Acerto", "Acertou sua primeira questão!", "🎯", "first_correct" in unlocked),
            Achievement("ten_correct", "Iniciante", "10 questões corretas!", "⭐", "ten_correct" in unlocked),
            Achievement("fifty_correct", "Aprendiz", "50 questões corretas!", "🌟", "fifty_correct" in unlocked),
            Achievement("hundred_correct", "Mestre", "100 questões corretas!", "🏆", "hundred_correct" in unlocked),
            Achievement("perfect_level", "Perfeito!", "Completou uma fase sem erros!", "💯", "perfect_level" in unlocked),
            Achievement("five_consecutive", "Em Chama!", "5 acertos seguidos!", "🔥", "five_consecutive" in unlocked),
            Achievement("ten_consecutive", "Imparável!", "10 acertos seguidos!", "⚡", "ten_consecutive" in unlocked),
            Achievement("level_10", "Progresso", "Alcançou a fase 10!", "📚", "level_10" in unlocked),
            Achievement("level_20", "Dedicado", "Alcançou a fase 20!", "📖", "level_20" in unlocked),
            Achievement("level_30", "Infinito!", "Alcançou a fase 30!", "♾️", "level_30" in unlocked),
            Achievement("master_add", "Mestre da Adição", "100 adições corretas!", "➕", "master_add" in unlocked),
            Achievement("master_sub", "Mestre da Subtração", "100 subtrações corretas!", "➖", "master_sub" in unlocked),
            Achievement("master_mul", "Mestre da Multiplicação", "100 multiplicações corretas!", "✖️", "master_mul" in unlocked),
            Achievement("master_div", "Mestre da Divisão", "100 divisões corretas!", "➗", "master_div" in unlocked),
        )
    }

    fun saveAchievement(prefs: SharedPreferences, id: String) {
        val unlocked = prefs.getStringSet("achievements", emptySet())?.toMutableSet() ?: mutableSetOf()
        unlocked.add(id)
        prefs.edit().putStringSet("achievements", unlocked).apply()
    }

    fun checkAndUnlockAchievements(
        prefs: SharedPreferences,
        totalCorrect: Int,
        level: Int,
        consecutiveCorrect: Int,
        wrongInLevel: Int,
        addStats: OperationStats,
        subStats: OperationStats,
        mulStats: OperationStats,
        divStats: OperationStats
    ): List<String> {
        val newUnlocks = mutableListOf<String>()
        val unlocked = prefs.getStringSet("achievements", emptySet()) ?: emptySet()
        
        fun unlock(id: String, title: String) {
            if (id !in unlocked) {
                saveAchievement(prefs, id)
                newUnlocks.add(title)
            }
        }
        
        if (totalCorrect >= 1) unlock("first_correct", "🎯 Primeiro Acerto!")
        if (totalCorrect >= 10) unlock("ten_correct", "⭐ Iniciante!")
        if (totalCorrect >= 50) unlock("fifty_correct", "🌟 Aprendiz!")
        if (totalCorrect >= 100) unlock("hundred_correct", "🏆 Mestre!")
        if (wrongInLevel == 0 && totalCorrect > 0) unlock("perfect_level", "💯 Perfeito!")
        if (consecutiveCorrect >= 5) unlock("five_consecutive", "🔥 Em Chama!")
        if (consecutiveCorrect >= 10) unlock("ten_consecutive", "⚡ Imparável!")
        if (level >= 10) unlock("level_10", "📚 Fase 10!")
        if (level >= 20) unlock("level_20", "📖 Fase 20!")
        if (level >= 30) unlock("level_30", "♾️ Infinito!")
        if (addStats.correct >= 100) unlock("master_add", "➕ Mestre da Adição!")
        if (subStats.correct >= 100) unlock("master_sub", "➖ Mestre da Subtração!")
        if (mulStats.correct >= 100) unlock("master_mul", "✖️ Mestre da Multiplicação!")
        if (divStats.correct >= 100) unlock("master_div", "➗ Mestre da Divisão!")
        
        return newUnlocks
    }

    fun loadDailyChallenge(prefs: SharedPreferences): DailyChallenge {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val savedDate = prefs.getString("challenge_date", "") ?: ""
        
        return if (savedDate == today) {
            DailyChallenge(
                date = today,
                description = prefs.getString("challenge_desc", "Responda 20 questões") ?: "Responda 20 questões",
                targetCorrect = prefs.getInt("challenge_target", 20),
                operation = Op.values()[prefs.getInt("challenge_op", 0)],
                completed = prefs.getBoolean("challenge_completed", false),
                progress = prefs.getInt("challenge_progress", 0)
            )
        } else {
            // Novo desafio
            val op = Op.values().random()
            val challenge = DailyChallenge(
                date = today,
                description = "Responda 20 questões de ${op.toPortuguese()}",
                targetCorrect = 20,
                operation = op,
                completed = false,
                progress = 0
            )
            prefs.edit().apply {
                putString("challenge_date", today)
                putString("challenge_desc", challenge.description)
                putInt("challenge_target", challenge.targetCorrect)
                putInt("challenge_op", op.ordinal)
                putBoolean("challenge_completed", false)
                putInt("challenge_progress", 0)
                apply()
            }
            challenge
        }
    }
    
    fun saveDailyChallengeProgress(prefs: SharedPreferences, progress: Int, completed: Boolean) {
        prefs.edit().apply {
            putInt("challenge_progress", progress)
            putBoolean("challenge_completed", completed)
            apply()
        }
    }
    
    // Sistema de Repetição Espaçada
    fun saveWrongQuestion(prefs: SharedPreferences, questionText: String) {
        val wrongQuestions = prefs.getStringSet("wrong_questions", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        val timestamp = System.currentTimeMillis()
        wrongQuestions.add("$questionText|$timestamp")
        
        // Limitar a 50 questões erradas salvas
        if (wrongQuestions.size > 50) {
            val sorted = wrongQuestions.sortedBy { it.split("|").getOrNull(1)?.toLongOrNull() ?: 0L }
            wrongQuestions.clear()
            wrongQuestions.addAll(sorted.takeLast(50))
        }
        
        prefs.edit().putStringSet("wrong_questions", wrongQuestions).apply()
    }
    
    fun getQuestionsForReview(prefs: SharedPreferences, questionsAnswered: Int): List<String> {
        val wrongQuestions = prefs.getStringSet("wrong_questions", emptySet()) ?: emptySet()
        val now = System.currentTimeMillis()
        val reviewQueue = mutableListOf<String>()
        
        wrongQuestions.forEach { entry ->
            val parts = entry.split("|")
            val questionText = parts.getOrNull(0) ?: return@forEach
            val timestamp = parts.getOrNull(1)?.toLongOrNull() ?: return@forEach
            
            val timeSinceError = now - timestamp
            val dayInMillis = 24 * 60 * 60 * 1000L
            
            // Intervalo 1: após 5 questões (imediato)
            // Intervalo 2: após 10 questões  
            // Intervalo 3: após 1 dia
            val shouldReview = when {
                questionsAnswered % 5 == 0 && timeSinceError < 5 * 60 * 1000L -> true // Últimos 5 min
                questionsAnswered % 10 == 0 && timeSinceError < 30 * 60 * 1000L -> true // Últimos 30 min
                timeSinceError >= dayInMillis -> true // Após 1 dia
                else -> false
            }
            
            if (shouldReview) {
                reviewQueue.add(questionText)
            }
        }
        
        return reviewQueue.take(3) // Máximo 3 revisões por vez
    }
    
    fun removeReviewedQuestion(prefs: SharedPreferences, questionText: String) {
        val wrongQuestions = prefs.getStringSet("wrong_questions", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        wrongQuestions.removeAll { it.startsWith(questionText) }
        prefs.edit().putStringSet("wrong_questions", wrongQuestions).apply()
    }

    // Sistema de Sequência Diária (Daily Streak 🔥)
    fun updateDailyStreak(prefs: SharedPreferences): Int {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastDate = prefs.getString("streak_last_date", "") ?: ""
        var currentStreak = prefs.getInt("streak_count", 0)

        if (lastDate.isEmpty()) {
            currentStreak = 1
            prefs.edit().putString("streak_last_date", today).putInt("streak_count", 1).apply()
        } else if (lastDate != today) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            try {
                val last = sdf.parse(lastDate)
                val curr = sdf.parse(today)
                if (last != null && curr != null) {
                    val diffInMillis = curr.time - last.time
                    val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)
                    if (diffInDays == 1L) {
                        currentStreak += 1
                    } else if (diffInDays > 1L) {
                        currentStreak = 1 // Reseta se pulou 1 dia
                    }
                    prefs.edit().putString("streak_last_date", today).putInt("streak_count", currentStreak).apply()
                }
            } catch (e: Exception) {
                // Fallback
            }
        }
        return currentStreak
    }

    fun getTimeAttackHighScore(prefs: SharedPreferences): Int {
        return prefs.getInt("time_attack_highscore", 0)
    }

    fun saveTimeAttackScore(prefs: SharedPreferences, score: Int): TimeAttackResult {
        val currentHigh = getTimeAttackHighScore(prefs)
        val isNewHigh = score > currentHigh
        if (isNewHigh) {
            prefs.edit().putInt("time_attack_highscore", score).apply()
        }
        return TimeAttackResult(score, isNewHigh)
    }

    fun getAvailableAvatars(): List<Avatar> {
        return listOf(
            Avatar("student", "Estudante", "🧑‍🎓", 1),
            Avatar("star", "Estrela", "⭐", 2),
            Avatar("wizard", "Mago dos Números", "🧙‍♂️", 5),
            Avatar("scientist", "Cientista", "👨‍🔬", 10),
            Avatar("superhero", "Super Herói", "🦸‍♂️", 15),
            Avatar("king", "Rei da Matemática", "👑", 20),
            Avatar("dragon", "Mestre Dragão", "🐉", 30)
        )
    }

    // Boss Battle Generator
    fun getBossForLevel(level: Int): BossInfo? {
        return when (level) {
            5 -> BossInfo("Monstro da Adição", "👹", 100, 50, "Derrote o Monstro com acertos rápidos de soma!")
            10 -> BossInfo("Gigante da Subtração", "🗿", 120, 75, "Acerte as subtrações para derrubar o Gigante!")
            15 -> BossInfo("Mago da Tabuada", "🧙‍♂️", 150, 100, "Vença o Mago multiplicando corretamente!")
            20 -> BossInfo("Dragão da Divisão", "🐉", 180, 150, "Divida e conquiste o Dragão Matemático!")
            25 -> BossInfo("Rei Sem Lógica", "👑", 200, 200, "O supremo vilão das 4 operações!")
            30 -> BossInfo("Imperador do Caos", "👾", 250, 300, "O desafio final supremo da matemática!")
            else -> if (level > 30 && level % 5 == 0) {
                BossInfo("Guardião Supremo Nível $level", "🔥", 200 + (level * 5), 100 + (level * 5), "Desafio lendário!")
            } else null
        }
    }

    // Gerenciamento de Power-Ups
    fun getPowerUpCount(prefs: SharedPreferences, type: PowerUpType): Int {
        return prefs.getInt("powerup_${type.name}", 2) // Inicialmente dá 2 de presente!
    }

    fun usePowerUp(prefs: SharedPreferences, type: PowerUpType): Boolean {
        val count = getPowerUpCount(prefs, type)
        if (count > 0) {
            prefs.edit().putInt("powerup_${type.name}", count - 1).apply()
            return true
        }
        return false
    }

    fun addPowerUp(prefs: SharedPreferences, type: PowerUpType, amount: Int = 1) {
        val current = getPowerUpCount(prefs, type)
        prefs.edit().putInt("powerup_${type.name}", current + amount).apply()
    }

    fun getPowerUpList(): List<PowerUp> {
        return listOf(
            PowerUp(PowerUpType.SHIELD, "Escudo Mágico", "🛡️", 30, "Protege contra 1 erro sem perder vida"),
            PowerUp(PowerUpType.BOMB_5050, "Bomba 50/50", "🔮", 20, "Elimina 2 alternativas incorretas"),
            PowerUp(PowerUpType.FREEZE, "Congelar Tempo", "⏱️", 25, "Congela o tempo da questão por 10s")
        )
    }

    // Roleta Diária da Sorte
    fun canSpinWheel(prefs: SharedPreferences): Boolean {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastSpin = prefs.getString("last_wheel_spin", "") ?: ""
        return lastSpin != today
    }

    fun recordWheelSpin(prefs: SharedPreferences) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        prefs.edit().putString("last_wheel_spin", today).apply()
    }

    // Recompensas Diárias (7 Dias)
    fun getDailyRewardStreakDay(prefs: SharedPreferences): Int {
        return prefs.getInt("daily_reward_streak_day", 1)
    }

    fun canClaimDailyReward(prefs: SharedPreferences): Boolean {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastClaim = prefs.getString("last_daily_reward_date", "") ?: ""
        return lastClaim != today
    }

    fun claimDailyReward(prefs: SharedPreferences): Int {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var currentDay = getDailyRewardStreakDay(prefs)
        prefs.edit().putString("last_daily_reward_date", today).apply()
        
        val nextDay = if (currentDay >= 7) 1 else currentDay + 1
        prefs.edit().putInt("daily_reward_streak_day", nextDay).apply()
        return currentDay
    }
}



