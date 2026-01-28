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

@Singleton
class AlarmScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleInterviewReminder(
        applicationId: String,
        companyName: String,
        jobTitle: String,
        interviewAtMillis: Long
    ) {
        val triggerAt = interviewAtMillis - REMINDER_OFFSET_MILLIS
        if (triggerAt <= System.currentTimeMillis()) {
            cancelInterviewReminder(applicationId)
            return
        }

        val intent = Intent(context, MyAlarmReceiver::class.java).apply {
            action = ACTION_INTERVIEW_REMINDER
            putExtra(EXTRA_APPLICATION_ID, applicationId)
            putExtra(EXTRA_COMPANY_NAME, companyName)
            putExtra(EXTRA_JOB_TITLE, jobTitle)
            putExtra(EXTRA_INTERVIEW_AT, interviewAtMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            applicationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            alarmManager.setAndAllowWhileIdle(
                RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        }
    }

    fun cancelInterviewReminder(applicationId: String) {
        val intent = Intent(context, MyAlarmReceiver::class.java).apply {
            action = ACTION_INTERVIEW_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            applicationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        const val ACTION_INTERVIEW_REMINDER = "com.team.applywise.ACTION_INTERVIEW_REMINDER"
        const val EXTRA_APPLICATION_ID = "extra_application_id"
        const val EXTRA_COMPANY_NAME = "extra_company_name"
        const val EXTRA_JOB_TITLE = "extra_job_title"
        const val EXTRA_INTERVIEW_AT = "extra_interview_at"
        private const val REMINDER_OFFSET_MILLIS = 30 * 60 * 1000L
    }
}