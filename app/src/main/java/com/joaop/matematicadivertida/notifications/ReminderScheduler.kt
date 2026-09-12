package com.joaop.matematicadivertida

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Agendamento do lembrete local diário.
 *
 * Por que isto existe: o app tem quatro mecânicas diárias (sequência 🔥, prêmio diário,
 * roleta e desafio do dia) que só funcionam se a criança lembrar de voltar sozinha.
 * Antes disto nada a chamava de volta — era um programa de fidelidade que nunca avisa
 * o cliente que ele tem pontos para resgatar.
 *
 * O agendamento usa WorkManager (e não AlarmManager) porque o lembrete pode atrasar
 * alguns minutos sem problema nenhum, e o WorkManager sobrevive a reinício do aparelho
 * sem precisar de BroadcastReceiver de BOOT_COMPLETED nem de permissão de alarme exato.
 */
object ReminderScheduler {

    /** Mesmo arquivo de preferências usado pelo jogo inteiro. */
    const val PREFS_NAME = "JogoInfantil"

    /** Chave da preferência "quero receber lembrete" (ligada por padrão). */
    const val KEY_REMINDER_ENABLED = "reminder_enabled"

    private const val TAG = "Lembrete"
    private const val UNIQUE_WORK_NAME = "lembrete_diario"

    /** Fim de tarde: depois da escola e antes do jantar. */
    private const val REMINDER_HOUR = 18
    private const val REMINDER_MINUTE = 0

    fun isEnabled(prefs: SharedPreferences): Boolean =
        prefs.getBoolean(KEY_REMINDER_ENABLED, true)

    /**
     * O aparelho permite notificar?
     *
     * Até o Android 12 qualquer app podia notificar; do 13 em diante é preciso a
     * permissão POST_NOTIFICATIONS, concedida pela pessoa.
     */
    fun permissaoConcedida(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    /**
     * Liga ou desliga o lembrete e já aplica a decisão no agendamento.
     * Chamado pelo onboarding e pelo botão da tela de Configurações.
     */
    fun setEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
        if (enabled) schedule(context) else cancel(context)
    }

    /**
     * Agenda (ou reagenda) o lembrete para as 18h.
     *
     * É seguro chamar a cada abertura do app: a política UPDATE substitui o agendamento
     * anterior em vez de criar um segundo. Reagendar toda vez também corrige o atraso
     * que um trabalho periódico acumula ao longo das semanas.
     */
    fun schedule(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            if (!isEnabled(prefs)) {
                cancel(context)
                return
            }

            val delayMillis = millisUntilNextReminder()
            val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .addTag(UNIQUE_WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
            Log.d(TAG, "⏰ Lembrete agendado para daqui a ${delayMillis / 60000} minutos")
        } catch (e: Exception) {
            // Um lembrete que falha nunca pode derrubar o app.
            Log.e(TAG, "⚠️ Falha ao agendar lembrete: ${e.message}")
        }
    }

    fun cancel(context: Context) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
            Log.d(TAG, "🔕 Lembrete cancelado")
        } catch (e: Exception) {
            Log.e(TAG, "⚠️ Falha ao cancelar lembrete: ${e.message}")
        }
    }

    /** Quanto falta, em milissegundos, para as 18h de hoje — ou de amanhã, se já passou. */
    private fun millisUntilNextReminder(): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, REMINDER_HOUR)
            set(Calendar.MINUTE, REMINDER_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!next.after(now)) {
            next.add(Calendar.DAY_OF_YEAR, 1)
        }
        return next.timeInMillis - now.timeInMillis
    }
}
