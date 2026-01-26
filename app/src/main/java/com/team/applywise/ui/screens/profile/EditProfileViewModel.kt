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
class EditProfileViewModel @Inject constructor(
    private val authService: AuthService,
    private val userRepo: UserRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserInfo()
    }

    private fun loadUserInfo() {
        val authUser = authService.getCurrentUser()
        if (authUser == null) {
            _uiState.update { it.copy(nameInput = "Unknown", email = "Unknown") }
            return
        }

        _uiState.update {
            it.copy(
                nameInput = authUser.name,
                email = authUser.email
            )
        }

        viewModelScope.launch {
            val user = userRepo.getUser(authUser.uid)
            _uiState.update {
                it.copy(
                    nameInput = user?.name?.ifBlank { authUser.name } ?: authUser.name,
                    email = user?.email?.ifBlank { authUser.email } ?: authUser.email,
                    avatarColor = user?.avatarColor ?: ""
                )
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(nameInput = value, error = null, saveSuccess = false) }
    }

    fun updateAvatarColor(colorName: String) {
        _uiState.update { it.copy(avatarColor = colorName, error = null, saveSuccess = false) }
    }

    fun saveProfile() {
        val authUser = authService.getCurrentUser() ?: return
        val trimmedName = _uiState.value.nameInput.trim()
        if (trimmedName.isBlank()) {
            _uiState.update { it.copy(error = "Name cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, saveSuccess = false) }
            try {
                userRepo.updateUserProfile(authUser.uid, trimmedName, _uiState.value.avatarColor)
                try {
                    authService.updateDisplayName(trimmedName)
                } catch (_: Exception) {
                    // Keep Firestore update even if auth profile update fails
                }
                authService.updateAvatarColor(_uiState.value.avatarColor)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Failed to save profile") }
            }
        }
    }

    fun clearSaveState() {
        _uiState.update { it.copy(saveSuccess = false, error = null) }
    }
}

data class EditProfileUiState(
    val nameInput: String = "",
    val email: String = "",
    val avatarColor: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)