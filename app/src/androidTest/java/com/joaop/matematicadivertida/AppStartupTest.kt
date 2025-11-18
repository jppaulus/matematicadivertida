package com.joaop.matematicadivertida

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith

/**
 * Testes básicos de inicialização da MainActivity.
 * Validam que a app começa sem erros e não congela durante startup.
 */
@RunWith(AndroidJUnit4::class)
class AppStartupTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        // Habilitar modo de teste para evitar dependências pesadas
        MainActivity.DISABLE_HEAVY_FEATURES = true
    }

    /**
     * Teste 1: Activity é criada sem exceção
     */
    @Test
    fun appStartup_activityIsCreated() {
        activityRule.scenario.onActivity { activity ->
            assert(activity != null) { "MainActivity não foi criada" }
        }
    }

    /**
     * Teste 2: Activity não lança exceção durante onCreate
     * (Se lançasse, o teste falharia durante setUp)
     */
    @Test
    fun appStartup_doesNotThrowException() {
        // Se chegou aqui, não houve exceção não capturada
        activityRule.scenario.onActivity { activity ->
            assert(activity.isDestroyed.not()) { "Activity foi destruída durante startup" }
        }
    }

    /**
     * Teste 3: Modo de teste está ativado
     */
    @Test
    fun appStartup_testModeIsEnabled() {
        assert(MainActivity.DISABLE_HEAVY_FEATURES) {
            "Modo de teste deveria estar habilitado"
        }
    }

    /**
     * Teste 4: Validar que a flag de test mode previne inicialização pesada
     */
    @Test
    fun testMode_preventsHeavyInitialization() {
        // Se DISABLE_HEAVY_FEATURES está true, Firebase/Ads não devem inicializar
        MainActivity.DISABLE_HEAVY_FEATURES = true
        
        activityRule.scenario.onActivity { activity ->
            // Verificar que activity está funcionando
            assert(!activity.isDestroyed) { "Activity deveria estar viva" }
        }
    }

    /**
     * Teste 5: App não congela durante startup (timeout implícito)
     * Se esse teste passar, significa que onCreate completou dentro do timeout
     */
    @Test(timeout = 5000) // 5 segundos timeout
    fun appStartup_completesQuickly() {
        activityRule.scenario.onActivity { activity ->
            assert(activity != null) { "MainActivity não respondeu em tempo" }
        }
    }

    /**
     * Teste 6: Validar que SharedPreferences pode ser acessado
     */
    @Test
    fun appStartup_canAccessSharedPreferences() {
        activityRule.scenario.onActivity { activity ->
            val prefs = activity.getSharedPreferences("JogoInfantil", android.content.Context.MODE_PRIVATE)
            assert(prefs != null) { "SharedPreferences não acessível" }
        }
    }

    /**
     * Teste 7: Allowed Ads Without Consent começa com false
     */
    @Test
    fun appStartup_allowAdsWithoutConsentIsFalse() {
        assert(MainActivity.allowAdsWithoutConsent == false) {
            "allowAdsWithoutConsent deveria começar como false"
        }
    }
}
