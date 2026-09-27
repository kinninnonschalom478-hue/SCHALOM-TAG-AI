package com.schalom.tagai

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.Calendar

class MorningEncouragementReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        val quotes = listOf(
            "« Dieu t'a donné un plan, et je suis là pour t'aider à le réaliser. »",
            "« Champion de la 2nde F3, chaque schéma électrique maîtrisé aujourd'hui construit ton avenir ! »",
            "« La persévérance est la clé de la réussite en sciences industrielles. Bonne journée ! »",
            "« Confie tes efforts au Seigneur et tes projets réussiront. Bon travail ! »"
        )
        val todayQuote = quotes.random()

        val schedule = when (dayOfWeek) {
            Calendar.MONDAY -> "Aujourd'hui : Électrotechnique & Schémas Électriques (08h-12h), Mathématiques (15h-17h)."
            Calendar.TUESDAY -> "Aujourd'hui : Physique-Chimie (08h-11h), Français (11h-13h)."
            Calendar.WEDNESDAY -> "Aujourd'hui : Construction Mécanique & Dessin Technique (08h-12h)."
            Calendar.THURSDAY -> "Aujourd'hui : Électronique pratique & Mesures (08h-12h), Anglais (15h-17h)."
            Calendar.FRIDAY -> "Aujourd'hui : Travaux Pratiques F3 en laboratoire (08h-12h), Histoire-Géo (15h-17h)."
            else -> "Week-end : Temps de révision des schémas et repos !"
        }

        showNotification(context, "Glory IA - Encouragement du Matin ⏰", "$todayQuote\n\n📚 $schedule")
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val channelId = "glory_ia_f3_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Rappels & Encouragements 2nde F3",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
