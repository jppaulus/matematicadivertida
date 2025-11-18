package com.joaop.matematicadivertida

import android.content.Context
import android.content.SharedPreferences
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Test

/**
 * Testes unitários para o sistema de persistência de dados.
 * Validam que progresso, estatísticas e conquistas são salvos/restaurados corretamente.
 */
class GameDataPersistenceTest {
    private lateinit var prefs: SharedPreferences
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        // Limpar dados antes de cada teste
        prefs = context.getSharedPreferences("JogoInfantil", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    /**
     * Teste 1: Salvar e recuperar nível do jogador
     */
    @Test
    fun testLevelPersistence() {
        val testLevel = 15
        
        // Salvar nível
        prefs.edit().putInt("level", testLevel).apply()
        
        // Recuperar e validar
        val savedLevel = prefs.getInt("level", 1)
        assert(savedLevel == testLevel) { "Nível não foi persistido corretamente" }
    }

    /**
     * Teste 2: Salvar e recuperar estatísticas de operação
     */
    @Test
    fun testOperationStatsPersistence() {
        val op = "add"
        
        // Salvar stats de adição
        prefs.edit().apply {
            putInt("${op}_correct", 25)
            putInt("${op}_wrong", 5)
            putLong("${op}_time", 45000) // 45 segundos
            putInt("${op}_count", 30)
            apply()
        }
        
        // Recuperar e validar
        val stats = GameDataManager.loadOperationStats(prefs, op)
        assert(stats.correct == 25) { "Acertos não foram salvos" }
        assert(stats.wrong == 5) { "Erros não foram salvos" }
        assert(stats.totalTime == 45000L) { "Tempo não foi salvo" }
        assert(stats.count == 30) { "Contagem não foi salva" }
    }

    /**
     * Teste 3: Cálculo de acurácia está correto
     */
    @Test
    fun testAccuracyCalculation() {
        // Stats: 8 acertos em 10 tentativas = 80%
        val stats = GameDataManager.loadOperationStats(prefs, "test")
        val withCorrectAnswers = stats.copy(correct = 8, wrong = 2)
        
        val accuracy = withCorrectAnswers.accuracy
        assert(accuracy == 0.8f) { "Acurácia calculada incorretamente: $accuracy" }
    }

    /**
     * Teste 4: Salvar e recuperar conquistas desbloqueadas
     */
    @Test
    fun testAchievementPersistence() {
        val achievementIds = setOf("first_correct", "ten_correct", "fifty_correct")
        
        // Salvar conquistas desbloqueadas
        prefs.edit().putStringSet("achievements", achievementIds).apply()
        
        // Recuperar e validar
        val unlocked = prefs.getStringSet("achievements", emptySet()) ?: emptySet()
        assert(unlocked.size == 3) { "Número de conquistas incorreto" }
        assert("first_correct" in unlocked) { "Conquista 'first_correct' não foi salva" }
        assert("ten_correct" in unlocked) { "Conquista 'ten_correct' não foi salva" }
    }

    /**
     * Teste 5: Carregar conquistas mostra corretamente quais estão desbloqueadas
     */
    @Test
    fun testLoadAchievementsStatus() {
        // Desbloquear uma conquista
        val unlocked = mutableSetOf("first_correct", "ten_correct")
        prefs.edit().putStringSet("achievements", unlocked).apply()
        
        // Carregar todas as conquistas
        val allAchievements = GameDataManager.loadAchievements(prefs)
        
        // Validar que status está correto
        val firstCorrect = allAchievements.find { it.id == "first_correct" }
        assert(firstCorrect != null) { "Conquista 'first_correct' não encontrada" }
        assert(firstCorrect!!.unlocked) { "Status 'unlocked' não está correto" }
        
        val notUnlocked = allAchievements.find { it.id == "hundred_correct" }
        assert(notUnlocked != null) { "Conquista 'hundred_correct' não encontrada" }
        assert(!notUnlocked!!.unlocked) { "Conquista deveria estar bloqueada" }
    }

    /**
     * Teste 6: Estatísticas múltiplas para diferentes operações
     */
    @Test
    fun testMultipleOperationStats() {
        val operations = listOf("add", "sub", "mul", "div")
        
        // Salvar stats para cada operação
        operations.forEachIndexed { index, op ->
            prefs.edit().apply {
                putInt("${op}_correct", index * 10)
                putInt("${op}_wrong", index)
                apply()
            }
        }
        
        // Carregar e validar cada uma
        operations.forEachIndexed { index, op ->
            val stats = GameDataManager.loadOperationStats(prefs, op)
            assert(stats.correct == index * 10) { "Stats incorretos para $op" }
            assert(stats.wrong == index) { "Erros incorretos para $op" }
        }
    }

    /**
     * Teste 7: Valores padrão quando dados não existem
     */
    @Test
    fun testDefaultValuesWhenNoData() {
        // Sem dados salvos, deve retornar padrões
        val stats = GameDataManager.loadOperationStats(prefs, "empty")
        
        assert(stats.correct == 0) { "Default de acertos deveria ser 0" }
        assert(stats.wrong == 0) { "Default de erros deveria ser 0" }
        assert(stats.totalTime == 0L) { "Default de tempo deveria ser 0" }
        assert(stats.count == 0) { "Default de contagem deveria ser 0" }
    }

    /**
     * Teste 8: Salvar XP e moedas do jogador
     */
    @Test
    fun testPlayerProgressPersistence() {
        val xp = 1500
        val coins = 42
        val playerLevel = 5
        
        prefs.edit().apply {
            putInt("xp", xp)
            putInt("coins", coins)
            putInt("playerLevel", playerLevel)
            apply()
        }
        
        assert(prefs.getInt("xp", 0) == xp) { "XP não foi persistido" }
        assert(prefs.getInt("coins", 0) == coins) { "Moedas não foram persistidas" }
        assert(prefs.getInt("playerLevel", 1) == playerLevel) { "Nível do jogador não foi persistido" }
    }

    /**
     * Teste 9: Incrementar estatísticas corretamente
     */
    @Test
    fun testIncrementStats() {
        val op = "add"
        
        // Estado inicial
        prefs.edit().apply {
            putInt("${op}_correct", 5)
            putInt("${op}_wrong", 2)
            apply()
        }
        
        // Simular acerto
        val current = GameDataManager.loadOperationStats(prefs, op)
        val updated = current.copy(
            correct = current.correct + 1,
            count = current.count + 1
        )
        GameDataManager.saveOperationStats(prefs, op, updated)
        
        // Validar incremento
        val saved = GameDataManager.loadOperationStats(prefs, op)
        assert(saved.correct == 6) { "Incremento de acertos falhou" }
        assert(saved.count == 1) { "Contagem não foi incrementada" }
    }

    /**
     * Teste 10: Limpar dados após conclusão
     */
    @Test
    fun testClearData() {
        // Salvar alguns dados
        prefs.edit().apply {
            putInt("level", 20)
            putInt("xp", 5000)
            apply()
        }
        
        // Verificar que estão lá
        assert(prefs.getInt("level", 0) == 20) { "Dados não foram salvos" }
        
        // Limpar
        prefs.edit().clear().apply()
        
        // Validar limpeza
        assert(prefs.getInt("level", 0) == 0) { "Dados não foram limpos" }
        assert(prefs.getInt("xp", 0) == 0) { "XP não foi limpo" }
    }
}
