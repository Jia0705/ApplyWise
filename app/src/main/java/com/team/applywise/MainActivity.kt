package com.team.applywise

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.navigation.compose.rememberNavController
import com.team.applywise.ui.navigation.AppNav
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.content.edit
import com.team.applywise.service.AlarmScheduler.Companion.EXTRA_APPLICATION_ID

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestNotificationsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, "Notifications permission denied", Toast.LENGTH_LONG).show()
        }
    }

    private val exactAlarmSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(
                    this,
                    "Exact alarms not allowed. Interview reminders may not fire on time.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensureNotificationPermission()
        ensureExactAlarmPermission()
        enableEdgeToEdge()
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val navController = rememberNavController()
                AppNav(
                    navController = navController,
                    pendingApplicationId = intent.getStringExtra(EXTRA_APPLICATION_ID)
                )
            }
        }
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            POST_NOTIFICATIONS
        ) == PERMISSION_GRANTED
        if (!granted) {
            requestNotificationsLauncher.launch(POST_NOTIFICATIONS)
        }
    }

    private fun ensureExactAlarmPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        if (alarmManager.canScheduleExactAlarms()) return

        val prefs = getSharedPreferences("applywise_prefs", MODE_PRIVATE)
        val alreadyAsked = prefs.getBoolean("asked_exact_alarm", false)
        if (alreadyAsked) {
            Toast.makeText(
                this,
                "Exact alarms not allowed. Interview reminders may not fire on time.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        prefs.edit { putBoolean("asked_exact_alarm", true) }

        val intent = Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        exactAlarmSettingsLauncher.launch(intent)
    }
}