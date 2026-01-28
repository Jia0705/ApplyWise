package com.team.applywise.ui.screens.application

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.applywise.data.model.ApplicationStatus
import com.team.applywise.data.model.JobApplication
import com.team.applywise.data.model.StatusChange
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
class AddEditApplicationViewModel @Inject constructor(
    private val authService: AuthService,
    private val applicationRepo: JobApplicationRepo,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val applicationId: String? = savedStateHandle.get<String>("applicationId")
    
    private val _uiState = MutableStateFlow(ApplicationFormUiState())
    val uiState = _uiState.asStateFlow()

    val isEditMode: Boolean get() = applicationId != null

    init {
        if (applicationId != null) {
            loadApplication(applicationId)
        }
    }

    private fun loadApplication(id: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val application = applicationRepo.getApplicationById(id)
                application?.let {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            companyName = it.companyName,
                            jobTitle = it.jobTitle,
                            status = it.status,
                            applicationDate = it.applicationDate,
                            interviewScheduledAt = it.interviewScheduledAt,
                            notes = it.notes,
                            createdAt = it.createdAt,
                            originalStatus = it.status,
                            statusHistory = it.statusHistory
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onCompanyNameChange(value: String) {
        _uiState.update { it.copy(companyName = value, companyNameError = null) }
    }

    fun onJobTitleChange(value: String) {
        _uiState.update { it.copy(jobTitle = value, jobTitleError = null) }
    }

    fun onStatusChange(value: ApplicationStatus) {
        val clearedInterviewDate = if (value == ApplicationStatus.INTERVIEW_SCHEDULED) {
            _uiState.value.interviewScheduledAt
        } else {
            null
        }
        _uiState.update {
            it.copy(
                status = value,
                interviewScheduledAt = clearedInterviewDate,
                interviewScheduledAtError = null
            )
        }
    }

    fun onApplicationDateChange(value: Long) {
        _uiState.update { it.copy(applicationDate = value) }
    }

    fun onInterviewScheduledAtChange(value: Long) {
        _uiState.update { it.copy(interviewScheduledAt = value, interviewScheduledAtError = null) }
    }

    fun onNotesChange(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    fun saveApplication() {
        // Validate
        val companyNameError = if (_uiState.value.companyName.isBlank()) {
            "Company name is required"
        } else null

        val jobTitleError = if (_uiState.value.jobTitle.isBlank()) {
            "Job title is required"
        } else null

        if (companyNameError != null || jobTitleError != null) {
            _uiState.update {
                it.copy(
                    companyNameError = companyNameError,
                    jobTitleError = jobTitleError
                )
            }
            return
        }

        val interviewError = if (_uiState.value.status == ApplicationStatus.INTERVIEW_SCHEDULED) {
            val interviewTime = _uiState.value.interviewScheduledAt
            when {
                interviewTime == null -> "Interview date and time are required"
                interviewTime < System.currentTimeMillis() -> "Interview date/time cannot be in the past"
                else -> null
            }
        } else {
            null
        }
        if (interviewError != null) {
            _uiState.update { it.copy(interviewScheduledAtError = interviewError) }
            return
        }

        val userId = authService.getCurrentUser()?.uid ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            try {
                val now = System.currentTimeMillis()
                if (isEditMode && applicationId != null) {
                    val existingHistory = _uiState.value.statusHistory.ifEmpty {
                        listOf(
                            StatusChange(
                                _uiState.value.originalStatus,
                                _uiState.value.createdAt
                            )
                        )
                    }
                    val updatedHistory = if (_uiState.value.status != _uiState.value.originalStatus) {
                        existingHistory + StatusChange(_uiState.value.status, now)
                    } else {
                        existingHistory
                    }
                    // Update application
                    val application = JobApplication(
                        id = applicationId,
                        userId = userId,
                        companyName = _uiState.value.companyName,
                        jobTitle = _uiState.value.jobTitle,
                        status = _uiState.value.status,
                        applicationDate = _uiState.value.applicationDate,
                        interviewScheduledAt = _uiState.value.interviewScheduledAt,
                        notes = _uiState.value.notes,
                        createdAt = _uiState.value.createdAt,
                        updatedAt = now,
                        statusHistory = updatedHistory
                    )
                    applicationRepo.updateApplication(application)
                    updateInterviewReminder(application)
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                } else {
                    val initialTimestamp = if (_uiState.value.status == ApplicationStatus.APPLIED) {
                        _uiState.value.applicationDate
                    } else {
                        now
                    }
                    // Create new application
                    val application = JobApplication(
                        userId = userId,
                        companyName = _uiState.value.companyName,
                        jobTitle = _uiState.value.jobTitle,
                        status = _uiState.value.status,
                        applicationDate = _uiState.value.applicationDate,
                        interviewScheduledAt = _uiState.value.interviewScheduledAt,
                        notes = _uiState.value.notes,
                        statusHistory = listOf(StatusChange(_uiState.value.status, initialTimestamp))
                    )
                    val newId = applicationRepo.createApplication(application)
                    updateInterviewReminder(application.copy(id = newId))
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun updateInterviewReminder(application: JobApplication) {
        if (application.id.isBlank()) return
        val interviewAt = application.interviewScheduledAt
        if (application.status == ApplicationStatus.INTERVIEW_SCHEDULED && interviewAt != null) {
            alarmScheduler.cancelInterviewReminder(application.id)
            alarmScheduler.scheduleInterviewReminder(
                applicationId = application.id,
                companyName = application.companyName,
                jobTitle = application.jobTitle,
                interviewAtMillis = interviewAt
            )
        } else {
            alarmScheduler.cancelInterviewReminder(application.id)
        }
    }
}

// UI state for Application Form
data class ApplicationFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val companyName: String = "",
    val jobTitle: String = "",
    val status: ApplicationStatus = ApplicationStatus.APPLIED,
    val applicationDate: Long = System.currentTimeMillis(),
    val interviewScheduledAt: Long? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val originalStatus: ApplicationStatus = ApplicationStatus.APPLIED,
    val statusHistory: List<StatusChange> = emptyList(),
    val companyNameError: String? = null,
    val jobTitleError: String? = null,
    val interviewScheduledAtError: String? = null
)