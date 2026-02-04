package com.team.applywise

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.compose.foundation.layout.padding
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.team.applywise.ui.navigation.AppNav
import com.team.applywise.ui.navigation.BottomNavItem
import com.team.applywise.ui.navigation.BottomNavigationBar
import com.team.applywise.ui.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.content.edit
import com.team.applywise.service.AlarmScheduler.Companion.EXTRA_APPLICATION_ID

/**
 * Main Activity - The entry point of our app
 * This handles:
 * 1. Requesting notification permission (Android 13+)
 * 2. Requesting exact alarm permission (Android 12+) for interview reminders
 * 3. Setting up navigation with bottom navigation bar
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Launcher for requesting notification permission
    // When user clicks Allow/Deny, this callback runs
    private val requestNotificationsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, "Notifications permission denied", Toast.LENGTH_LONG).show()
        }
    }

    // Launcher for requesting exact alarm permission
    // Opens system settings where user can enable exact alarms
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
        // Request permissions when app starts
        ensureNotificationPermission()
        ensureExactAlarmPermission()
        // Enable edge-to-edge display (use full screen including status bar area)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            // Define bottom navigation items (Dashboard, Applications, Profile)
            val bottomNavItems = remember {
                listOf(
                    BottomNavItem("Dashboard", Screen.Dashboard, Icons.Default.Dashboard),
                    BottomNavItem("Applications", Screen.ApplicationList(), Icons.Default.Work),
                    BottomNavItem("Profile", Screen.Profile, Icons.Default.AccountCircle)
                )
            }
            // Get current navigation route to determine if we should show bottom bar
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            // Routes where bottom navigation should be visible
            val bottomNavRoutes = listOfNotNull(
                Screen.Dashboard::class.qualifiedName,
                Screen.ApplicationList::class.qualifiedName,
                Screen.Profile::class.qualifiedName
            )
            // Only show bottom bar on Dashboard, Applications, Profile screens
            // Hide it on Login, Register, Detail screens, etc.
            val showBottomBar = bottomNavRoutes.any { routeKey ->
                currentRoute?.startsWith(routeKey) == true
            }

            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        BottomNavigationBar(
                            navController = navController,
                            items = bottomNavItems
                        )
                    }
                }
            ) { padding ->
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Main navigation setup - pass application ID if opened from notification
                    AppNav(
                        navController = navController,
                        pendingApplicationId = intent.getStringExtra(EXTRA_APPLICATION_ID),
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }

    /**
     * Check and request notification permission
     * Only needed on Android 13 (TIRAMISU) and above
     * Older Android versions automatically allow notifications
     */
    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            POST_NOTIFICATIONS
        ) == PERMISSION_GRANTED
        if (!granted) {
            // Show permission dialog
            requestNotificationsLauncher.launch(POST_NOTIFICATIONS)
        }
    }

    /**
     * Check and request exact alarm permission
     * Only needed on Android 12 (S) and above
     * Exact alarms ensure interview reminders fire at EXACTLY the scheduled time (30 min before)
     * Without this, Android might delay the notification by several minutes
     */
    private fun ensureExactAlarmPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        // If already granted, do nothing
        if (alarmManager.canScheduleExactAlarms()) return

        // Check if we already asked before (don't annoy user repeatedly)
        val prefs = getSharedPreferences("applywise_prefs", MODE_PRIVATE)
        val alreadyAsked = prefs.getBoolean("asked_exact_alarm", false)
        if (alreadyAsked) {
            // Already asked, just show warning
            Toast.makeText(
                this,
                "Exact alarms not allowed. Interview reminders may not fire on time.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        // Mark as asked so we don't ask again
        prefs.edit { putBoolean("asked_exact_alarm", true) }

        // Open system settings for exact alarm permission
        val intent = Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        exactAlarmSettingsLauncher.launch(intent)
    }
}