package com.pathgeniusacademy.lifemate

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class LifeMateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "LifeMate task and reminder notifications"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        const val REMINDER_CHANNEL_ID = "lifemate_reminders"
    }
}
