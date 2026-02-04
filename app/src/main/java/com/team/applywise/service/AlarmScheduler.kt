package com.team.applywise.service

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AlarmScheduler - Sets up interview reminder notifications
 * Reminds users 30 min before their scheduled interview
 */
@Singleton
class AlarmScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    // Android's system service that manages alarms/notifications
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedule a reminder for an interview
     * Will trigger 30 min before the interview time
     * 
     * How it works:
     * 1. Calculate reminder time (interview time - 30 min)
     * 2. Create an intent (message) for the notification
     * 3. Schedule it with Android's AlarmManager
     */
    fun scheduleInterviewReminder(
        applicationId: String,
        companyName: String,
        jobTitle: String,
        interviewAtMillis: Long // Interview time as timestamp
    ) {
        // Calculate when to show reminder (30 min before interview)
        val triggerAt = interviewAtMillis - REMINDER_OFFSET_MILLIS
        
        // If interview is in the past or too soon, don't schedule
        if (triggerAt <= System.currentTimeMillis()) {
            cancelInterviewReminder(applicationId)
            return
        }

        // Create the message that will trigger our notification
        val intent = Intent(context, MyAlarmReceiver::class.java).apply {
            action = ACTION_INTERVIEW_REMINDER
            // Add all the info needed to show the notification
            putExtra(EXTRA_APPLICATION_ID, applicationId)
            putExtra(EXTRA_COMPANY_NAME, companyName)
            putExtra(EXTRA_JOB_TITLE, jobTitle)
            putExtra(EXTRA_INTERVIEW_AT, interviewAtMillis)
        }

        // Wrap the intent in a PendingIntent (required for alarms)
        // hashCode() creates a unique ID for each application
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            applicationId.hashCode(), // Unique ID for this alarm
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the alarm with Android
        // Different methods for different Android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            // For Android 12+, check if we have permission for exact alarms
            alarmManager.setAndAllowWhileIdle(
                RTC_WAKEUP, // Wake up device if sleeping
                triggerAt,
                pendingIntent
            )
        } else {
            // For older Android or if we have permission
            alarmManager.setExactAndAllowWhileIdle(
                RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        }
    }

    /**
     * Cancel a scheduled reminder
     * Called when interview is deleted or rescheduled
     */
    fun cancelInterviewReminder(applicationId: String) {
        // Create same intent as when scheduling (Android uses this to find the alarm)
        val intent = Intent(context, MyAlarmReceiver::class.java).apply {
            action = ACTION_INTERVIEW_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            applicationId.hashCode(), // Same unique ID as when scheduling
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Tell Android to cancel this alarm
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        // Constants used to identify and configure alarms
        const val ACTION_INTERVIEW_REMINDER = "com.team.applywise.ACTION_INTERVIEW_REMINDER"
        const val EXTRA_APPLICATION_ID = "extra_application_id"
        const val EXTRA_COMPANY_NAME = "extra_company_name"
        const val EXTRA_JOB_TITLE = "extra_job_title"
        const val EXTRA_INTERVIEW_AT = "extra_interview_at"
        private const val REMINDER_OFFSET_MILLIS = 30 * 60 * 1000L
    }
}