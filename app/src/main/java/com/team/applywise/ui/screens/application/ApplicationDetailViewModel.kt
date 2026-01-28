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

@HiltViewModel
class ApplicationDetailViewModel @Inject constructor(
    private val authService: AuthService,
    private val applicationRepo: JobApplicationRepo,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val applicationId: String = savedStateHandle.get<String>("applicationId") ?: ""

    private val _uiState = MutableStateFlow(ApplicationDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadApplication()
    }

    private fun loadApplication() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
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

    fun reload() {
        loadApplication()
    }

    fun deleteApplication() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isDeleting = true) }
                applicationRepo.deleteApplication(applicationId)
                alarmScheduler.cancelInterviewReminder(applicationId)
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