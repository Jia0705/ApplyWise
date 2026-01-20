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

    private fun loadUserInfo() {
        val authUser = authService.getCurrentUser()
        if (authUser == null) {
            _uiState.update { it.copy(email = "Unknown") }
            return
        }

        viewModelScope.launch {
            val user = userRepo.getUser(authUser.uid)
            _uiState.update {
                it.copy(
                    email = user?.email ?: authUser.email
                )
            }
        }
    }

    fun logout() {
        authService.signOut()
        _uiState.update { it.copy(logoutSuccess = true) }
    }
}

// UI state for Profile screen
data class ProfileUiState(
    val email: String = "",
    val logoutSuccess: Boolean = false
)