package com.joaop.matematicadivertida

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class GameLogicTest {

    private fun mul(a: Int, b: Int) = Question("$a × $b = ?", a * b, listOf(a * b), Op.MUL)

    @Test
    fun hintForMultiplicationByZeroOrOne_doesNotCrash() {
        for (level in 1..3) {
            for ((a, b) in listOf(0 to 7, 7 to 0, 0 to 0, 1 to 9, 9 to 1)) {
                assertTrue(getProgressiveHint(mul(a, b), level).isNotBlank())
            }
        }
    }

    @Test
    fun stepByStepMultiplicationHint_isMathematicallyCorrect() {
        assertEquals("7 × 1 = 7", getProgressiveHint(mul(7, 1), 3))
        assertEquals("7 × 2 = 7 + 7 = 14", getProgressiveHint(mul(7, 2), 3))
        assertEquals("7 × 3 = 7 + 7 + 7 = 21", getProgressiveHint(mul(3, 7), 3))
    }

    @Test
    fun generatedQuestions_areConsistentAndHintsNeverThrow() {
        val configs = listOf(
            levelConfig(level = 1, totalCorrect = 0),
            levelConfig(level = 3, totalCorrect = 70),
            levelConfig(level = 5, totalCorrect = 100),
            levelConfig(level = 45, totalCorrect = 500),
            generateAdaptiveLevel(level = 4, totalCorrect = 3, totalWrong = 20, consecutiveCorrect = 0),
            LevelConfig(ops = listOf(Op.MUL, Op.DIV), min = 0, max = 10)
        )
        repeat(3000) { i ->
            val cfg = configs[i % configs.size]
            val q = generateQuestion(cfg)
            assertTrue("${q.text} fora das operações da fase", q.op in cfg.ops)
            assertTrue("${q.text} sem a resposta nas opções", q.correct in q.options)
            assertEquals("${q.text} com opções repetidas", q.options.size, q.options.toSet().size)
            assertTrue("${q.text} com opção negativa", q.options.all { it >= 0 })
            val parsed = parseQuestionFromText(q.text)
            assertNotNull("Não reconstrói ${q.text}", parsed)
            assertEquals(q.text, q.correct, parsed!!.correct)
            for (level in 1..3) getProgressiveHint(q, level)
        }
    }

    @Test
    fun playerLevel_growsWithXp() {
        val step = GameDataManager.XP_PER_PLAYER_LEVEL
        assertEquals(1, GameDataManager.playerLevelForXp(-10))
        assertEquals(1, GameDataManager.playerLevelForXp(0))
        assertEquals(1, GameDataManager.playerLevelForXp(step - 1))
        assertEquals(2, GameDataManager.playerLevelForXp(step))
        assertEquals(30, GameDataManager.playerLevelForXp(29 * step))
    }

    @Test
    fun boss_isDefeatedExactlyOnTheLastCorrectAnswer() {
        for (level in listOf(5, 10, 25, 30, 70, 100, 500)) {
            val boss = GameDataManager.getBossForLevel(level)!!
            for (target in 3..15) {
                assertEquals(boss.maxHp, GameDataManager.bossHpAfter(boss.maxHp, 0, target))
                assertTrue("fase $level, alvo $target", GameDataManager.bossHpAfter(boss.maxHp, target - 1, target) > 0)
                assertEquals(0, GameDataManager.bossHpAfter(boss.maxHp, target, target))
                assertEquals(0, GameDataManager.bossHpAfter(boss.maxHp, target + 2, target))
            }
        }
    }

    @Test
    fun comboBonus_matchesTheMultiplierShownOnScreen() {
        assertEquals(5, GameDataManager.coinsForCorrectAnswer(1))
        assertEquals(8, GameDataManager.coinsForCorrectAnswer(3))
        assertEquals(10, GameDataManager.coinsForCorrectAnswer(5))
        assertEquals(15, GameDataManager.coinsForCorrectAnswer(10))
    }

    @Test
    fun daysBetween_countsCalendarDaysAcrossDaylightSavingTime() {
        val lisbon = TimeZone.getTimeZone("Europe/Lisbon")
        // 29/03/2026 começa o horário de verão em Portugal: esse dia tem só 23 horas.
        assertEquals(1L, GameDataManager.daysBetween("2026-03-29", "2026-03-30", lisbon))
        assertEquals(1L, GameDataManager.daysBetween("2026-10-25", "2026-10-26", lisbon))
        assertEquals(0L, GameDataManager.daysBetween("2026-09-15", "2026-09-15", lisbon))
        assertEquals(2L, GameDataManager.daysBetween("2026-09-13", "2026-09-15", lisbon))
        assertNull(GameDataManager.daysBetween("data-invalida", "2026-09-15", lisbon))
    }
}
