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

class MyAlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        if (intent.action != ACTION_INTERVIEW_REMINDER) return

        val applicationId = intent.getStringExtra(EXTRA_APPLICATION_ID).orEmpty()
        val companyName = intent.getStringExtra(EXTRA_COMPANY_NAME).orEmpty()
        val jobTitle = intent.getStringExtra(EXTRA_JOB_TITLE).orEmpty()
        val interviewAt = intent.getLongExtra(EXTRA_INTERVIEW_AT, 0L)

        val dateText = if (interviewAt > 0L) {
            val date = Date(interviewAt)
            val datePart = getMediumDateFormat(context).format(date)
            val timePart = getTimeFormat(context).format(date)
            "$datePart • $timePart"
        } else {
            ""
        }

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

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (applicationId.isNotBlank()) {
                putExtra(EXTRA_APPLICATION_ID, applicationId)
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            applicationId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.outline_track_changes)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationId = if (applicationId.isNotBlank()) applicationId.hashCode() else intent.hashCode()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = checkSelfPermission(
                context,
                POST_NOTIFICATIONS
            ) == PERMISSION_GRANTED
            if (!granted) return
        }
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "interview_reminders"
    }
}