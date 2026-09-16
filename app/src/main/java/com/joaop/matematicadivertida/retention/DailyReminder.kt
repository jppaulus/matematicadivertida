package com.joaop.matematicadivertida

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Lembrete diário local (P1 do DIAGNOSTICO_RETENCAO.md).
 *
 * Sequência, prêmio diário, roleta e desafio diário só funcionam se a criança voltar sozinha.
 * Este lembrete chama de volta no fim da tarde com uma mensagem ligada ao estado do jogo.
 * Tudo é calculado no aparelho a partir das preferências locais: nenhum dado sai do app.
 */
object DailyReminder {
    const val PREF_ENABLED = "reminder_enabled"
    const val PREF_PROMPT_SHOWN = "reminder_prompt_shown"
    const val REMINDER_HOUR = 18

    private const val WORK_NAME = "daily_reminder"
    private const val CHANNEL_ID = "lembrete_diario"
    private const val NOTIFICATION_ID = 1001

    // Quem sumiu há mais de 3 dias só é lembrado nestes marcos, para não virar insistência.
    private val RE_ENGAGEMENT_DAYS = setOf(7L, 14L, 30L)

    data class Message(val title: String, val body: String)

    fun isEnabled(prefs: SharedPreferences): Boolean = prefs.getBoolean(PREF_ENABLED, true)

    /** No Android 13+ a notificação só aparece com a permissão POST_NOTIFICATIONS concedida. */
    fun hasNotificationPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    /** Chamado ao abrir o app: agenda sem reiniciar o horário, ou cancela se estiver desligado. */
    fun ensureScheduled(context: Context) {
        if (isEnabled(prefs(context))) enqueue(context, ExistingPeriodicWorkPolicy.KEEP) else cancel(context)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(PREF_ENABLED, enabled).apply()
        if (enabled) enqueue(context, ExistingPeriodicWorkPolicy.UPDATE) else cancel(context)
    }

    /**
     * Mensagem do dia, ou null quando não há o que lembrar (já jogou hoje, nunca jogou, ou
     * está longe demais de um marco de reengajamento). Sequência em risco tem prioridade.
     */
    fun messageFor(prefs: SharedPreferences, today: String = GameDataManager.todayKey()): Message? {
        val lastPlayed = prefs.getString("streak_last_date", "").orEmpty()
        if (lastPlayed.isEmpty()) return null
        val daysAway = GameDataManager.daysBetween(lastPlayed, today) ?: return null
        if (daysAway <= 0 || (daysAway > 3 && daysAway !in RE_ENGAGEMENT_DAYS)) return null

        val streak = prefs.getInt("streak_count", 0)
        if (daysAway == 1L && streak >= 2) {
            return Message("🔥 Não perca sua sequência!", "São $streak dias seguidos. Jogue hoje para continuar!")
        }
        val options = listOf(
            Message("🎁 Seu prêmio diário chegou!", "Entre para resgatar a recompensa de hoje."),
            Message("🎡 A Roleta da Sorte está liberada!", "Gire hoje e ganhe moedas ou power-ups."),
            Message("🎯 Tem desafio novo hoje!", "Um desafio de continhas está esperando por você.")
        )
        return options[Math.floorMod(today.hashCode(), options.size)]
    }

    fun millisUntilNextReminder(now: Calendar, hour: Int = REMINDER_HOUR): Long {
        val next = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_MONTH, 1)
        }
        return next.timeInMillis - now.timeInMillis
    }

    internal fun show(context: Context, message: Message) {
        if (!hasNotificationPermission(context)) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Lembrete diário", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Lembra de voltar para manter a sequência e pegar o prêmio diário"
                }
            )
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setColor(ContextCompat.getColor(context, R.color.ic_launcher_background))
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun enqueue(context: Context, policy: ExistingPeriodicWorkPolicy) {
        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(millisUntilNextReminder(Calendar.getInstance()), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, policy, request)
    }

    private fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences("JogoInfantil", Context.MODE_PRIVATE)
}

class DailyReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("JogoInfantil", Context.MODE_PRIVATE)
        if (DailyReminder.isEnabled(prefs)) {
            DailyReminder.messageFor(prefs)?.let { DailyReminder.show(applicationContext, it) }
        }
        return Result.success()
    }
}
