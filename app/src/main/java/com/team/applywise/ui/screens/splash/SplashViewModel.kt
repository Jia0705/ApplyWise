package com.team.applywise.ui.screens.splash

import androidx.lifecycle.ViewModel
import com.team.applywise.service.AuthService
import com.team.applywise.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authService: AuthService,
) : ViewModel() {
    suspend fun determineStartDestination(): Screen {
        val user = authService.getCurrentUser()
        return if (user == null) {
            Screen.Login
        } else {
            Screen.Dashboard
        }
    }
}