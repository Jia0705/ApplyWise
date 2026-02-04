package com.team.applywise.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.applywise.core.utils.Utils
import com.team.applywise.data.repo.UserRepo
import com.team.applywise.service.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Edit Profile screen
 * Allows user to change their name and avatar color
 * Saves to both Firebase Auth (display name) and Firestore (full profile)
 */
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
        // Get user from Firebase Auth first
        val authUser = authService.getCurrentUser()
        if (authUser == null) {
            _uiState.update {
                it.copy(
                    nameInput = "Unknown",
                    email = "Unknown",
                    isLoading = false
                )
            }
            return
        }

        // Show auth data immediately while we load from Firestore
        _uiState.update {
            it.copy(
                nameInput = authUser.name,
                email = authUser.email,
                isLoading = true
            )
        }

        // Load full data from Firestore
        viewModelScope.launch {
            val user = userRepo.getUser(authUser.uid)
            _uiState.update {
                it.copy(
                    nameInput = user?.name?.ifBlank { authUser.name } ?: authUser.name,
                    email = user?.email?.ifBlank { authUser.email } ?: authUser.email,
                    avatarColor = user?.avatarColor ?: "",
                    isLoading = false
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
        
        // Step 1: Validate name (cannot be empty)
        val validationError = Utils.validateName(trimmedName)
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }

        // Step 2: Save to both places
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, saveSuccess = false) }
            try {
                // Save to Firestore (always works)
                userRepo.updateUserProfile(authUser.uid, trimmedName, _uiState.value.avatarColor)
                
                // Try to update Firebase Auth display name (might fail but that's OK)
                try {
                    authService.updateDisplayName(trimmedName)
                } catch (_: Exception) {
                    // Keep going even if Firebase Auth update fails
                    // Firestore is more important and already updated
                }
                
                // Update avatar color in local memory (not stored in Firebase Auth)
                authService.updateAvatarColor(_uiState.value.avatarColor)
                
                // Tell screen: save successful!
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
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)