package com.team.applywise.ui.screens.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.team.applywise.ui.navigation.Screen
import kotlinx.coroutines.delay

/**
 * SplashScreen - First screen shown when app opens
 * Shows app logo for 1.2 seconds while checking if user is logged in
 * 
 * Flow:
 * 1. Show "ApplyWise" logo and loading spinner
 * 2. Wait 1200ms (1.2 seconds)
 * 3. Check if user is logged in (via ViewModel)
 * 4. If logged in -> go to Dashboard
 * 5. If not logged in -> go to Login screen
 * 6. Special case: If opened from notification, go directly to that application detail
 */
@Composable
fun SplashScreen(
    navController: NavController,
    pendingApplicationId: String? = null // Non-null if opened from notification
) {
    val viewModel: SplashViewModel = hiltViewModel()

    // This runs once when screen is created
    LaunchedEffect(Unit) {
        delay(1200) // Show splash for 1.2 seconds
        val destination = viewModel.determineStartDestination() // Check if logged in
        
        // If user is logged in AND opened from notification, go to that application
        if (destination == Screen.Dashboard && !pendingApplicationId.isNullOrBlank()) {
            navigate(navController, Screen.ApplicationDetail(pendingApplicationId))
        } else {
            // Otherwise, go to Dashboard or Login depending on login status
            navigate(navController, destination)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.TrackChanges,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "ApplyWise",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Job Application Tracker",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            CircularProgressIndicator()
        }
    }
}

private fun navigate(
    navController: NavController,
    screen: Screen
) {
    navController.navigate(screen) {
        popUpTo<Screen.Splash> { inclusive = true }
        launchSingleTop = true
    }
}