package com.joaop.matematicadivertida

import android.app.Activity
import android.content.SharedPreferences
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory
import java.util.concurrent.TimeUnit

/**
 * Pedido de avaliação dentro do app (P3 do DIAGNOSTICO_RETENCAO.md).
 *
 * Só em momento de vitória (chefão derrotado ou recorde no Desafio Relâmpago), nunca no meio
 * de uma partida, depois de uso real e no máximo uma vez a cada 60 dias. O Google Play ainda
 * aplica a própria cota e pode não mostrar a janela.
 */
object ReviewPrompter {
    const val MIN_TOTAL_CORRECT = 30
    private const val PREF_LAST_PROMPT = "review_last_prompt_ms"
    private val MIN_INTERVAL_MS = TimeUnit.DAYS.toMillis(60)

    fun shouldAsk(totalCorrect: Int, lastPromptMs: Long, nowMs: Long): Boolean =
        totalCorrect >= MIN_TOTAL_CORRECT && (lastPromptMs <= 0L || nowMs - lastPromptMs >= MIN_INTERVAL_MS)

    fun maybeAsk(activity: Activity, prefs: SharedPreferences, totalCorrect: Int) {
        val now = System.currentTimeMillis()
        if (!shouldAsk(totalCorrect, prefs.getLong(PREF_LAST_PROMPT, 0L), now)) return
        prefs.edit().putLong(PREF_LAST_PROMPT, now).apply()

        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow().addOnCompleteListener { request ->
            if (request.isSuccessful) {
                manager.launchReviewFlow(activity, request.result)
            } else {
                Log.w("JogoInfantil", "In-App Review indisponível: ${request.exception?.message}")
            }
        }
    }
}
