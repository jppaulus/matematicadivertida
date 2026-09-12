package com.joaop.matematicadivertida

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Monta e exibe o lembrete diário, sempre a partir do estado real do jogo.
 *
 * A mensagem nunca é genérica se houver algo de verdade esperando a criança: sequência
 * em risco, prêmio do dia sem resgatar, roleta sem girar ou desafio do dia pela metade.
 * Se ela já jogou hoje e não deixou nada pendente, nenhuma notificação é enviada —
 * lembrete que chega sem motivo é o caminho mais curto para o app ser silenciado.
 */
class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    private data class Lembrete(val titulo: String, val texto: String)

    override fun doWork(): ListenableWorker.Result {
        try {
            val prefs = applicationContext.getSharedPreferences(
                ReminderScheduler.PREFS_NAME,
                Context.MODE_PRIVATE
            )

            if (!ReminderScheduler.isEnabled(prefs)) {
                Log.d(TAG, "Lembrete desligado nas configurações — nada a fazer")
                return ListenableWorker.Result.success()
            }

            val lembrete = montarLembrete(prefs)
            if (lembrete == null) {
                Log.d(TAG, "Nada pendente hoje — sem notificação")
                return ListenableWorker.Result.success()
            }

            if (!podeNotificar()) {
                Log.d(TAG, "Sem permissão de notificação — nada enviado")
                return ListenableWorker.Result.success()
            }

            exibirNotificacao(lembrete.titulo, lembrete.texto)
            Log.d(TAG, "🔔 Lembrete enviado: ${lembrete.titulo}")
        } catch (e: Exception) {
            // Falhar aqui não pode virar erro visível para a criança.
            Log.e(TAG, "⚠️ Erro ao enviar lembrete: ${e.message}")
        }
        return ListenableWorker.Result.success()
    }

    /** Escolhe a mensagem mais relevante, ou null quando não há motivo para incomodar. */
    private fun montarLembrete(prefs: SharedPreferences): Lembrete? {
        val hoje = chaveDoDia(0)
        val ontem = chaveDoDia(-1)

        val ultimoDiaJogado = prefs.getString("streak_last_date", "") ?: ""
        val jogouHoje = ultimoDiaJogado == hoje
        val sequencia = prefs.getInt("streak_count", 0)
        // Só está "em risco" quem jogou exatamente ontem. Se faltou mais de um dia a
        // sequência já foi perdida, e prometer o contrário seria mentira.
        val sequenciaEmRisco = sequencia > 0 && ultimoDiaJogado == ontem

        val premioDisponivel = (prefs.getString("last_daily_reward_date", "") ?: "") != hoje
        val roletaDisponivel = (prefs.getString("last_wheel_spin", "") ?: "") != hoje

        val desafioEhDeHoje = (prefs.getString("challenge_date", "") ?: "") == hoje
        val desafioConcluido = desafioEhDeHoje && prefs.getBoolean("challenge_completed", false)

        // Já jogou hoje e não deixou nada para trás: fica quieto.
        if (jogouHoje && !premioDisponivel && !roletaDisponivel && desafioConcluido) {
            return null
        }

        return when {
            sequenciaEmRisco -> Lembrete(
                "🔥 Sua sequência de $sequencia ${if (sequencia == 1) "dia" else "dias"} está em risco!",
                "Responda algumas continhas hoje para não perder a sequência."
            )
            premioDisponivel -> Lembrete(
                "🎁 Seu prêmio de hoje está esperando",
                "Abra o Matemática Divertida e pegue o prêmio diário."
            )
            roletaDisponivel -> Lembrete(
                "🎡 Você ainda não girou a Roleta da Sorte",
                "Tem moedas e power-ups te esperando. Vem tentar a sorte!"
            )
            !desafioConcluido -> Lembrete(
                "🎯 O desafio de hoje ainda não terminou",
                "Faltam poucas questões para completar o desafio diário."
            )
            else -> Lembrete(
                "🧮 Bora treinar um pouquinho?",
                "Cinco minutinhos de matemática já contam para a sua sequência."
            )
        }
    }

    /**
     * A partir do Android 13 a permissão POST_NOTIFICATIONS é obrigatória.
     * Em qualquer versão a pessoa ainda pode ter desligado as notificações do app
     * nas configurações do sistema — [NotificationManagerCompat] cobre esse caso.
     */
    private fun podeNotificar(): Boolean {
        if (!ReminderScheduler.permissaoConcedida(applicationContext)) return false
        return NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
    }

    // A permissão é verificada em podeNotificar(), chamado antes deste método.
    @SuppressLint("MissingPermission")
    private fun exibirNotificacao(titulo: String, texto: String) {
        val gerenciador =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                "Lembrete diário",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Um aviso por dia para manter a sequência de estudos."
            }
            gerenciador.createNotificationChannel(canal)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificacao = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notificacao)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Id fixo: o lembrete de hoje substitui o de ontem em vez de empilhar.
        gerenciador.notify(NOTIFICATION_ID, notificacao)
    }

    /** Data no mesmo formato usado pelo GameDataManager ("yyyy-MM-dd"). */
    private fun chaveDoDia(diasDeDiferenca: Int): String {
        val calendario = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, diasDeDiferenca)
        }
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(calendario.timeInMillis))
    }

    companion object {
        private const val TAG = "Lembrete"
        private const val CHANNEL_ID = "lembrete_diario"
        private const val NOTIFICATION_ID = 1001
    }
}
