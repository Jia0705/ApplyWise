package com.team.applywise.ui.screens.splash

import androidx.lifecycle.ViewModel
import com.team.applywise.service.AuthService
import com.team.applywise.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for Splash screen (the first screen user sees)
 * Checks if user is logged in and decides where to go next
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authService: AuthService,
) : ViewModel() {
    /**
     * Determine which screen to show after splash
     * If user is logged in: go to Dashboard
     * If user is not logged in: go to Login screen
     */
    suspend fun determineStartDestination(): Screen {
        val user = authService.getCurrentUser()
        return if (user == null) {
            Screen.Login // Not logged in, show login screen
        } else {
            Screen.Dashboard // Logged in, show dashboard
        }
    }
}