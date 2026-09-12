package com.joaop.matematicadivertida

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * Pedido de avaliação na Play, usando a API oficial de avaliação no app.
 *
 * Duas regras guiam tudo aqui:
 *
 * 1. **Só em momento de vitória.** O pedido dispara ao derrotar um chefão ou ao bater
 *    recorde no Desafio Relâmpago — nunca no meio de uma partida, nunca depois de um
 *    game over.
 * 2. **Raramente.** A própria Play limita quantas vezes o formulário aparece por
 *    usuário; as guardas abaixo existem para não gastar essa cota num usuário novo,
 *    que ainda não tem opinião formada sobre o app.
 *
 * A API nunca informa se a pessoa avaliou ou não — o app só sabe que o pedido foi feito.
 * Por isso é proibido (pela política da Play) condicionar qualquer recompensa a ele.
 */
object ReviewPrompt {

    private const val TAG = "Avaliacao"

    private const val KEY_FIRST_LAUNCH = "review_first_launch_millis"
    private const val KEY_LAST_ASK = "review_last_ask_millis"
    private const val KEY_ASK_COUNT = "review_ask_count"

    /** Quem ainda não acertou isto não conhece o app o bastante para avaliá-lo. */
    private const val MIN_ACERTOS = 30

    /** Dias desde a primeira abertura. Evita pedir avaliação no dia da instalação. */
    private const val MIN_DIAS_DE_USO = 2

    /** Intervalo mínimo entre dois pedidos. */
    private const val MIN_DIAS_ENTRE_PEDIDOS = 60

    /** Teto absoluto de pedidos na vida do app naquele aparelho. */
    private const val MAX_PEDIDOS = 3

    private const val UM_DIA_EM_MILLIS = 24L * 60L * 60L * 1000L

    /** Registra a data da primeira abertura. Chamado no onCreate da MainActivity. */
    fun registrarPrimeiraAbertura(prefs: SharedPreferences) {
        if (!prefs.contains(KEY_FIRST_LAUNCH)) {
            prefs.edit().putLong(KEY_FIRST_LAUNCH, System.currentTimeMillis()).apply()
        }
    }

    /**
     * Pede a avaliação se — e só se — todas as guardas permitirem.
     * Silencioso por natureza: se algo falhar, o jogo segue como se nada tivesse acontecido.
     */
    fun pedirSePuder(context: Context, prefs: SharedPreferences) {
        if (!devePedir(prefs)) return

        val activity = context.acharActivity()
        if (activity == null) {
            Log.d(TAG, "Sem Activity disponível — pedido cancelado")
            return
        }

        try {
            val manager = ReviewManagerFactory.create(activity)
            manager.requestReviewFlow().addOnCompleteListener { pedido ->
                if (!pedido.isSuccessful) {
                    Log.d(TAG, "Play não liberou o formulário agora: ${pedido.exception?.message}")
                    return@addOnCompleteListener
                }
                try {
                    manager.launchReviewFlow(activity, pedido.result)
                        .addOnCompleteListener {
                            // A API não diz se a pessoa avaliou; só registramos a tentativa.
                            registrarPedido(prefs)
                            Log.d(TAG, "⭐ Fluxo de avaliação concluído")
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "⚠️ Erro ao abrir o formulário: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "⚠️ Erro ao preparar avaliação: ${e.message}")
        }
    }

    private fun devePedir(prefs: SharedPreferences): Boolean {
        val agora = System.currentTimeMillis()

        val acertos = prefs.getInt("totalCorrect", 0)
        if (acertos < MIN_ACERTOS) return false

        val pedidosFeitos = prefs.getInt(KEY_ASK_COUNT, 0)
        if (pedidosFeitos >= MAX_PEDIDOS) return false

        val primeiraAbertura = prefs.getLong(KEY_FIRST_LAUNCH, 0L)
        if (primeiraAbertura <= 0L) return false
        if (agora - primeiraAbertura < MIN_DIAS_DE_USO * UM_DIA_EM_MILLIS) return false

        val ultimoPedido = prefs.getLong(KEY_LAST_ASK, 0L)
        if (ultimoPedido > 0L && agora - ultimoPedido < MIN_DIAS_ENTRE_PEDIDOS * UM_DIA_EM_MILLIS) {
            return false
        }

        return true
    }

    private fun registrarPedido(prefs: SharedPreferences) {
        prefs.edit()
            .putLong(KEY_LAST_ASK, System.currentTimeMillis())
            .putInt(KEY_ASK_COUNT, prefs.getInt(KEY_ASK_COUNT, 0) + 1)
            .apply()
    }
}

/**
 * Sobe a cadeia de contextos até achar a Activity.
 *
 * Dentro do Compose o `LocalContext.current` costuma ser um ContextWrapper do tema, e
 * não a Activity em si — mas o fluxo de avaliação da Play exige uma Activity de verdade.
 */
fun Context.acharActivity(): Activity? {
    var contexto: Context = this
    while (contexto is ContextWrapper) {
        if (contexto is Activity) return contexto
        contexto = contexto.baseContext
    }
    return null
}
