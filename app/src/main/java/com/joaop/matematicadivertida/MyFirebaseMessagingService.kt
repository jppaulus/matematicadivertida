package com.joaop.matematicadivertida

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "🔑 Novo token FCM: $token")
        // Aqui você pode enviar o token para seu servidor backend
        // sendTokenToServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "📨 Mensagem recebida de: ${message.from}")

        // Mensagens do console do Firebase costumam trazer notificação E dados; antes isso
        // gerava duas notificações. Mostra só uma, priorizando o payload de notificação.
        val notification = message.notification
        if (notification != null) {
            Log.d("FCM", "📬 Título: ${notification.title}")
            sendNotification(notification.title ?: "Matemática Divertida", notification.body ?: "")
        } else if (message.data.isNotEmpty()) {
            Log.d("FCM", "📦 Dados da mensagem: ${message.data}")
            handleDataMessage(message.data)
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        // Processar dados personalizados da notificação
        val type = data["type"]
        when (type) {
            "daily_challenge" -> {
                sendNotification(
                    "🎯 Novo Desafio Diário!",
                    "Um novo desafio está esperando por você!"
                )
            }
            "achievement" -> {
                val achievement = data["achievement_name"] ?: "Nova conquista"
                sendNotification(
                    "🏆 Conquista Desbloqueada!",
                    achievement
                )
            }
            else -> {
                sendNotification(
                    data["title"] ?: "Matemática Divertida",
                    data["body"] ?: "Você tem uma nova notificação"
                )
            }
        }
    }

    private fun sendNotification(title: String, body: String) {
        val channelId = "matematica_divertida_channel"
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            // Ícone monocromático: um ícone adaptativo como smallIcon derruba a notificação no
            // Android 8.0 ("Couldn't create icon") e vira um quadrado branco nas outras versões.
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setColor(ContextCompat.getColor(this, R.color.ic_launcher_background))
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Criar canal de notificação (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Matemática Divertida Notificações",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de desafios e conquistas"
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
