package com.joaop.matematicadivertida

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Teste de fumaça do caminho principal: menu → JOGAR → responder uma questão.
 *
 * Antes, cada teste daqui envolvia tudo num `try { } catch (e: Exception) { }` e
 * procurava tags que nunca existiram no app — ou seja, passavam sem testar nada.
 * Agora as tags existem (`playButton`, `questionText`, `answerButton_N`,
 * `hintButton`) e as asserções falham de verdade quando o fluxo quebra.
 */
@RunWith(AndroidJUnit4::class)
class GameplayUITest {
    companion object {
        /**
         * Precisa ser @BeforeClass: a regra do Compose sobe a MainActivity antes de
         * qualquer @Before, então tanto a flag quanto as preferências têm de estar
         * prontas antes disso.
         */
        @JvmStatic
        @BeforeClass
        fun prepararAmbiente() {
            MainActivity.DISABLE_HEAVY_FEATURES = true

            // Estado conhecido: sem onboarding na frente (senão o app abre na tela de
            // boas-vindas) e progresso zerado, que garante fase de adição com 3 opções.
            // commit() e não apply(): a Activity sobe logo em seguida.
            val contexto = InstrumentationRegistry.getInstrumentation().targetContext
            contexto.getSharedPreferences("JogoInfantil", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .putBoolean("onboarding_done", true)
                .putBoolean("reminder_enabled", false)
                .commit()
        }
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        composeTestRule.waitForIdle()
    }

    /** Leva o app do menu até a tela de questão. */
    private fun entrarNoJogo() {
        composeTestRule.onNodeWithTag("playButton").performScrollTo().performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Teste 1: o menu abre com o botão JOGAR visível.
     * É o botão que importa no primeiro uso — se ele sumir, o app perdeu a entrada.
     */
    @Test
    fun menu_mostraBotaoJogarVisivel() {
        composeTestRule.onNodeWithTag("playButton").performScrollTo().assertIsDisplayed()
    }

    /**
     * Teste 2: tocar em JOGAR abre a questão.
     * Crianças precisam VER a conta.
     */
    @Test
    fun jogar_abreATelaDeQuestao() {
        entrarNoJogo()
        composeTestRule.onNodeWithTag("questionText").performScrollTo().assertIsDisplayed()
    }

    /**
     * Teste 3: a questão vem com alternativas para tocar.
     *
     * O gerador tenta sempre montar 3 alternativas, mas o teste cobra só as duas
     * garantidas — exigir a terceira dependeria do sorteio e deixaria o teste instável.
     */
    @Test
    fun telaDeJogo_mostraAlternativas() {
        entrarNoJogo()
        composeTestRule.onNodeWithTag("answerButton_0").assertExists()
        composeTestRule.onNodeWithTag("answerButton_1").assertExists()
    }

    /**
     * Teste 4: o botão de dica está disponível no começo da questão.
     * É o que segura a criança que travou, em vez de ela fechar o app.
     */
    @Test
    fun botaoDeDica_estaDisponivelNoComecoDaQuestao() {
        entrarNoJogo()
        composeTestRule.onNodeWithTag("hintButton").performScrollTo().assertIsEnabled()
    }

    /**
     * Teste 5: responder não derruba o app.
     *
     * Não importa se a alternativa 0 é a certa ou a errada — nos dois casos o jogo
     * segue na mesma fase (são precisos 5 acertos para completar), então a questão
     * continua na tela.
     */
    @Test
    fun responderUmaQuestao_naoDerrubaOApp() {
        entrarNoJogo()
        composeTestRule.onNodeWithTag("answerButton_0").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("questionText").assertExists()
    }

    /**
     * Teste 6: o modo de teste está mesmo ligado.
     * Se falhar, os testes acima estariam subindo AdMob e UMP de verdade no emulador.
     */
    @Test
    fun modoDeTeste_estaLigado() {
        assert(MainActivity.DISABLE_HEAVY_FEATURES) {
            "DISABLE_HEAVY_FEATURES deveria estar ligado durante os testes"
        }
    }
}
