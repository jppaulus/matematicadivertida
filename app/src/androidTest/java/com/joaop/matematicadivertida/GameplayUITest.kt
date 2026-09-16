package com.joaop.matematicadivertida

import android.content.Context
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testes de UI do fluxo principal: menu → jogo → responder → dica.
 * Antes estes testes procuravam tags inexistentes dentro de try/catch e nunca falhavam.
 */
@RunWith(AndroidJUnit4::class)
class GameplayUITest {
    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        // Perfil limpo gravado antes de a Activity ler as preferências.
        InstrumentationRegistry.getInstrumentation().targetContext
            .getSharedPreferences("JogoInfantil", Context.MODE_PRIVATE)
            .edit().clear().putBoolean("onboarding_done", true).commit()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    private fun openGame() {
        composeTestRule.onNodeWithText("JOGAR", substring = true).performClick()
        composeTestRule.onNodeWithTag("questionText").assertIsDisplayed()
    }

    private fun currentQuestion(): Question {
        val text = composeTestRule.onNodeWithTag("questionText").fetchSemanticsNode()
            .config[SemanticsProperties.Text].joinToString("") { it.text }
        val question = parseQuestionFromText(text)
        assertNotNull("Questão ilegível na tela: $text", question)
        return question!!
    }

    @Test
    fun menu_showsPlayButton() {
        composeTestRule.onNodeWithText("JOGAR", substring = true).assertIsDisplayed()
    }

    @Test
    fun playButton_opensGameWithReadableQuestion() {
        openGame()
        currentQuestion()
    }

    @Test
    fun correctAnswer_showsFeedbackAndCountsProgress() {
        openGame()
        val question = currentQuestion()
        composeTestRule.onNodeWithTag("answerButton_${question.correct}").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("feedbackOverlay").assertIsDisplayed()
        composeTestRule.onNodeWithText("✅ 1/", substring = true).assertExists()
    }

    @Test
    fun hintButton_showsHintCard() {
        openGame()
        composeTestRule.onNodeWithTag("hintButton").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Dica:").assertIsDisplayed()
    }
}
