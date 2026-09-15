package com.joaop.matematicadivertida

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testes básicos de inicialização da MainActivity.
 * Validam que o app abre sem erros e sobrevive à recriação (por exemplo, ao girar a tela).
 */
@RunWith(AndroidJUnit4::class)
class AppStartupTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun appStartup_activityReachesResumed() {
        assertEquals(Lifecycle.State.RESUMED, activityRule.scenario.state)
    }

    @Test
    fun appStartup_survivesRecreation() {
        activityRule.scenario.recreate()
        assertEquals(Lifecycle.State.RESUMED, activityRule.scenario.state)
    }

    @Test
    fun appStartup_canAccessSharedPreferences() {
        activityRule.scenario.onActivity { activity ->
            assertNotNull(activity.getSharedPreferences("JogoInfantil", Context.MODE_PRIVATE))
        }
    }
}
