package com.team.applywise

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import dagger.hilt.android.HiltAndroidApp
import com.team.applywise.service.MyAlarmReceiver.Companion.NOTIFICATION_CHANNEL_ID

@HiltAndroidApp
class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Interview reminders",
            IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for upcoming interviews"
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}