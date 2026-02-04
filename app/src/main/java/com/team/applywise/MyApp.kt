package com.team.applywise

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import dagger.hilt.android.HiltAndroidApp
import com.team.applywise.service.MyAlarmReceiver.Companion.NOTIFICATION_CHANNEL_ID

/**
 * MyApp - The main Application class for ApplyWise
 * This runs BEFORE any activity starts
 * 
 * @HiltAndroidApp tells Hilt (dependency injection) to set up everything it needs
 * 
 * What it does:
 * 1. Creates notification channel for interview reminders
 * 2. Sets up Hilt dependency injection for the entire app
 * 
 * Note: Notification channels are REQUIRED on Android 8.0+ (Oreo)
 * Without a channel, notifications won't show!
 */
@HiltAndroidApp
class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        // Create notification channel when app starts
        createNotificationChannel()
    }

    /**
     * Create notification channel for interview reminders
     * Channel controls how notifications look and sound
     * IMPORTANCE_HIGH = notifications appear at top, make sound
     */
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