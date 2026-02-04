package com.team.applywise.service

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.text.format.DateFormat.getMediumDateFormat
import android.text.format.DateFormat.getTimeFormat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import com.team.applywise.R
import com.team.applywise.MainActivity
import com.team.applywise.service.AlarmScheduler.Companion.ACTION_INTERVIEW_REMINDER
import com.team.applywise.service.AlarmScheduler.Companion.EXTRA_APPLICATION_ID
import com.team.applywise.service.AlarmScheduler.Companion.EXTRA_COMPANY_NAME
import com.team.applywise.service.AlarmScheduler.Companion.EXTRA_INTERVIEW_AT
import com.team.applywise.service.AlarmScheduler.Companion.EXTRA_JOB_TITLE
import java.util.Date

/**
 * MyAlarmReceiver - Receives alarm from AlarmManager and shows notification
 * This runs when it's time to remind user about interview (30 min before)
 * 
 * How it works:
 * 1. AlarmManager fires at scheduled time
 * 2. This receiver gets the alarm
 * 3. We read company name, job title, interview time from the Intent
 * 4. We create and show a notification
 * 5. When user taps notification, open MainActivity with that application
 */
class MyAlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        // Make sure this is our interview reminder action (not some other broadcast)
        if (intent.action != ACTION_INTERVIEW_REMINDER) return

        // Get interview details from Intent (passed from AlarmScheduler)
        val applicationId = intent.getStringExtra(EXTRA_APPLICATION_ID).orEmpty()
        val companyName = intent.getStringExtra(EXTRA_COMPANY_NAME).orEmpty()
        val jobTitle = intent.getStringExtra(EXTRA_JOB_TITLE).orEmpty()
        val interviewAt = intent.getLongExtra(EXTRA_INTERVIEW_AT, 0L)

        // Format interview date and time nicely
        // Example: "Feb 03, 2026 • 02:00 PM"
        val dateText = if (interviewAt > 0L) {
            val date = Date(interviewAt)
            val datePart = getMediumDateFormat(context).format(date) // "Feb 03, 2026"
            val timePart = getTimeFormat(context).format(date) // "02:00 PM"
            "$datePart • $timePart"
        } else {
            ""
        }

        // Build notification text
        // Example: "Software Engineer at Google • Feb 03, 2026 • 02:00 PM"
        val title = "Interview reminder"
        val body = buildString {
            if (jobTitle.isNotBlank()) append(jobTitle)
            if (companyName.isNotBlank()) {
                if (isNotEmpty()) append(" at ")
                append(companyName)
            }
            if (dateText.isNotBlank()) {
                if (isNotEmpty()) append(" • ")
                append(dateText)
            }
        }.ifBlank { "Upcoming interview" }

        // Create Intent to open MainActivity when user taps notification
        // Passes applicationId so MainActivity can open that specific application detail
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (applicationId.isNotBlank()) {
                putExtra(EXTRA_APPLICATION_ID, applicationId)
            }
        }
        // Wrap in PendingIntent (Android requires this for notifications)
        val pendingIntent = PendingIntent.getActivity(
            context,
            applicationId.hashCode(), // Unique ID per application
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.outline_track_changes) // Icon in status bar
            .setContentTitle(title) // "Interview reminder"
            .setContentText(body) // "Software Engineer at Google • Feb 03..."
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Show at top of notifications
            .setContentIntent(pendingIntent) // What happens when user taps
            .setAutoCancel(true) // Dismiss notification when tapped
            .build()

        // Show the notification
        val notificationId = if (applicationId.isNotBlank()) applicationId.hashCode() else intent.hashCode()
        // Check notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = checkSelfPermission(
                context,
                POST_NOTIFICATIONS
            ) == PERMISSION_GRANTED
            if (!granted) return // Can't show notification without permission
        }
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "interview_reminders"
    }
}