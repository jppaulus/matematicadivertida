package com.joaop.matematicadivertida

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameAppInstrumentedTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun spamButton_doesNotFreezeApp() {
        val spam = composeTestRule.onNodeWithTag("spamButton")
        // Click fast many times
        repeat(12) { spam.performClick() }
        // verify app still shows a question or a hint button (app isn't frozen)
        composeTestRule.onNodeWithTag("hintButton").assertExists()
    }

    @Test
    fun viewSolution_showsSolutionDialog_withSteps() {
        // Open hint
        composeTestRule.onNodeWithTag("hintButton").performClick()
        // Click view solution
        composeTestRule.onNodeWithTag("viewSolution").performClick()
        // Dialog should show text '🔎 Como resolver' or at least step text
        composeTestRule.onNodeWithText("🔎 Como resolver").assertExists()
    }
}
