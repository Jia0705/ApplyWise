package com.team.applywise.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.applywise.data.repo.UserRepo
import com.team.applywise.service.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Profile screen
 * Shows user's name, email, and avatar color
 * Gets data from both Firebase Auth and Firestore
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authService: AuthService,
    private val userRepo: UserRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserInfo()
    }

    fun refresh() {
        loadUserInfo()
    }

    private fun loadUserInfo() {
        // Step 1: Get user from Firebase Auth (fast, but might not have all data)
        val authUser = authService.getCurrentUser()
        if (authUser == null) {
            _uiState.update { it.copy(name = "Unknown", email = "Unknown") }
            return
        }

        // Show auth data immediately
        _uiState.update {
            it.copy(
                name = authUser.name,
                email = authUser.email,
                avatarColor = authUser.avatarColor.ifBlank { it.avatarColor }
            )
        }

        // Step 2: Get full user data from Firestore (slower, but more complete)
        viewModelScope.launch {
            val user = userRepo.getUser(authUser.uid)
            if (user != null && (user.name.isNotBlank() || user.email.isNotBlank() || user.avatarColor.isNotBlank())) {
                // Update with Firestore data if available
                _uiState.update {
                    it.copy(
                        name = user.name.ifBlank { authUser.name },
                        email = user.email.ifBlank { authUser.email },
                        avatarColor = user.avatarColor.ifBlank { it.avatarColor }
                    )
                }
            }
        }
    }

    fun logout() {
        // Sign out from Firebase and tell screen to navigate to login
        authService.signOut()
        _uiState.update { it.copy(logoutSuccess = true) }
    }
}

// UI state for Profile screen
data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val avatarColor: String = "",
    val logoutSuccess: Boolean = false
)