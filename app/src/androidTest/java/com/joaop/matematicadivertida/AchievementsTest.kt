package com.joaop.matematicadivertida

import android.content.Context
import android.content.SharedPreferences
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Test

/**
 * Testes para o sistema de conquistas (achievements).
 * Validam que as conquistas são desbloqueadas corretamente e exibidas ao usuário.
 * Importante para motivação de crianças: conquistas são gamificação.
 */
class AchievementsTest {
    private lateinit var prefs: SharedPreferences
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        prefs = context.getSharedPreferences("JogoInfantil", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    /**
     * Teste 1: Todas as conquistas estão disponíveis no sistema
     */
    @Test
    fun testAllAchievementsExist() {
        val achievements = GameDataManager.loadAchievements(prefs)
        
        // Deve haver múltiplas conquistas
        assert(achievements.isNotEmpty()) { "Nenhuma conquista foi carregada" }
        assert(achievements.size >= 10) { "Deve haver pelo menos 10 conquistas" }
    }

    /**
     * Teste 2: Conquista "Primeiro Acerto" inicialmente desbloqueada?
     * (Validar estado inicial)
     */
    @Test
    fun testFirstCorrectAchievementInitialStatus() {
        val achievements = GameDataManager.loadAchievements(prefs)
        val firstCorrect = achievements.find { it.id == "first_correct" }
        
        assert(firstCorrect != null) { "Conquista 'first_correct' não existe" }
        assert(!firstCorrect!!.unlocked) { "Conquista 'first_correct' deveria começar bloqueada" }
    }

    /**
     * Teste 3: Desbloquear uma conquista
     */
    @Test
    fun testUnlockAchievement() {
        // Desbloquear conquista
        GameDataManager.saveAchievement(prefs, "first_correct")
        
        // Carregar e validar
        val achievements = GameDataManager.loadAchievements(prefs)
        val firstCorrect = achievements.find { it.id == "first_correct" }
        
        assert(firstCorrect!!.unlocked) { "Conquista não foi desbloqueada" }
    }

    /**
     * Teste 4: Diferentes conquistas têm diferentes IDs e descrições
     */
    @Test
    fun testAchievementUniqueness() {
        val achievements = GameDataManager.loadAchievements(prefs)
        
        // Todos IDs devem ser únicos
        val ids = achievements.map { it.id }
        assert(ids.size == ids.distinct().size) { "Há conquistas com IDs duplicados" }
        
        // Todos títulos devem ser únicos
        val titles = achievements.map { it.title }
        assert(titles.size == titles.distinct().size) { "Há conquistas com títulos duplicados" }
    }

    /**
     * Teste 5: Conquistas têm descrição e emoji
     */
    @Test
    fun testAchievementHasDescription() {
        val achievements = GameDataManager.loadAchievements(prefs)
        
        achievements.forEach { achievement ->
            assert(achievement.description.isNotEmpty()) { 
                "Conquista ${achievement.id} não tem descrição" 
            }
            assert(achievement.emoji.isNotEmpty()) { 
                "Conquista ${achievement.id} não tem emoji" 
            }
        }
    }

    /**
     * Teste 6: Desbloquear múltiplas conquistas
     */
    @Test
    fun testUnlockMultipleAchievements() {
        val achievementsToUnlock = listOf("first_correct", "ten_correct", "fifty_correct")
        
        achievementsToUnlock.forEach { id ->
            GameDataManager.saveAchievement(prefs, id)
        }
        
        val achievements = GameDataManager.loadAchievements(prefs)
        achievementsToUnlock.forEach { id ->
            val achievement = achievements.find { it.id == id }
            assert(achievement!!.unlocked) { "Conquista $id deveria estar desbloqueada" }
        }
    }

    /**
     * Teste 7: Desbloquear mesma conquista duas vezes (idempotência)
     */
    @Test
    fun testUnlockAchievementTwiceIsIdempotent() {
        // Desbloquear duas vezes
        GameDataManager.saveAchievement(prefs, "first_correct")
        GameDataManager.saveAchievement(prefs, "first_correct")
        
        // Deve estar desbloqueada uma vez
        val achievements = GameDataManager.loadAchievements(prefs)
        val firstCorrect = achievements.find { it.id == "first_correct" }
        
        assert(firstCorrect!!.unlocked) { "Conquista deveria estar desbloqueada" }
    }

    /**
     * Teste 8: Conquistas relacionadas a operações (Mestre)
     */
    @Test
    fun testMasterAchievementsExist() {
        val achievements = GameDataManager.loadAchievements(prefs)
        
        val masterAchievements = listOf("master_add", "master_sub", "master_mul", "master_div")
        masterAchievements.forEach { masterId ->
            val achievement = achievements.find { it.id == masterId }
            assert(achievement != null) { "Conquista de Mestre '$masterId' não existe" }
        }
    }

    /**
     * Teste 9: Conquistas de progresso de nível
     */
    @Test
    fun testProgressAchievementsExist() {
        val achievements = GameDataManager.loadAchievements(prefs)
        
        val progressAchievements = listOf("level_10", "level_20", "level_30")
        progressAchievements.forEach { levelId ->
            val achievement = achievements.find { it.id == levelId }
            assert(achievement != null) { "Conquista de progresso '$levelId' não existe" }
        }
    }

    /**
     * Teste 10: Conquistas de consecutivos (Em Chama, Imparável)
     */
    @Test
    fun testStreakAchievementsExist() {
        val achievements = GameDataManager.loadAchievements(prefs)
        
        val streakAchievements = listOf("five_consecutive", "ten_consecutive")
        streakAchievements.forEach { streakId ->
            val achievement = achievements.find { it.id == streakId }
            assert(achievement != null) { "Conquista de consecutivo '$streakId' não existe" }
        }
    }

    /**
     * Teste 11: Carregar após desbloquear persiste corretamente
     */
    @Test
    fun testAchievementPersistenceAfterUnlock() {
        // Desbloquear
        GameDataManager.saveAchievement(prefs, "hundred_correct")
        
        // "Carregar novamente" simulando app reinício
        val achievements1 = GameDataManager.loadAchievements(prefs)
        val found1 = achievements1.find { it.id == "hundred_correct" }?.unlocked
        
        // Carregar ainda novamente
        val achievements2 = GameDataManager.loadAchievements(prefs)
        val found2 = achievements2.find { it.id == "hundred_correct" }?.unlocked
        
        assert(found1 == true && found2 == true) { 
            "Conquista não persistiu após recarregar" 
        }
    }

    /**
     * Teste 12: Conquistas bloqueadas mantêm status de bloqueadas
     */
    @Test
    fun testLockedAchievementsRemainLocked() {
        // Desbloquear apenas uma
        GameDataManager.saveAchievement(prefs, "first_correct")
        
        val achievements = GameDataManager.loadAchievements(prefs)
        
        // Outras devem estar bloqueadas
        val otherAchievements = achievements.filter { it.id != "first_correct" }
        otherAchievements.forEach { achievement ->
            assert(!achievement.unlocked) { 
                "Conquista ${achievement.id} deveria estar bloqueada mas não está" 
            }
        }
    }
}
