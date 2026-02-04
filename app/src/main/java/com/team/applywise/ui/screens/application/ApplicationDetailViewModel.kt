package com.team.applywise.ui.screens.application

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.applywise.data.model.JobApplication
import com.team.applywise.data.repo.JobApplicationRepo
import com.team.applywise.service.AlarmScheduler
import com.team.applywise.service.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Application Detail screen
 * Shows full details of one application and allows deleting it
 * Gets the application ID from navigation arguments
 */
@HiltViewModel
class ApplicationDetailViewModel @Inject constructor(
    private val authService: AuthService,
    private val applicationRepo: JobApplicationRepo,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Get application ID from navigation (passed when user clicks on an application)
    private val applicationId: String = savedStateHandle.get<String>("applicationId") ?: ""

    private val _uiState = MutableStateFlow(ApplicationDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Load data as soon as ViewModel is created
        loadApplication()
    }

    /**
     * Load application details from Firestore
     * This is a one-time fetch (not real-time like the list screen)
     */
    private fun loadApplication() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                // Fetch from Firestore
                val application = applicationRepo.getApplicationById(applicationId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        application = application,
                        error = if (application == null) "Application not found" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    /**
     * Reload application (used after editing to refresh data)
     */
    fun reload() {
        loadApplication()
    }

    /**
     * Delete application permanently
     * Also cancels any interview reminder notification
     */
    fun deleteApplication() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isDeleting = true) }
                // Delete from Firestore
                applicationRepo.deleteApplication(applicationId)
                // Cancel notification alarm (if any)
                alarmScheduler.cancelInterviewReminder(applicationId)
                // Tell screen: delete successful, navigate back
                _uiState.update { it.copy(isDeleting = false, deleteSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isDeleting = false, error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ApplicationDetailUiState(
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val error: String? = null,
    val application: JobApplication? = null
)