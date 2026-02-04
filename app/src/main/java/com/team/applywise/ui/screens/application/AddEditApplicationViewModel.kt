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
import com.team.applywise.core.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Add/Edit Application screen
 * This handles both creating new applications and editing existing ones
 * Same screen, different modes depending on whether we receive an applicationId
 */
@HiltViewModel
class AddEditApplicationViewModel @Inject constructor(
    private val authService: AuthService,
    private val applicationRepo: JobApplicationRepo,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // If we get an ID from navigation, we're in edit mode. Otherwise, we're adding new.
    private val applicationId: String? = savedStateHandle.get<String>("applicationId")
    
    private val _uiState = MutableStateFlow(ApplicationFormUiState())
    val uiState = _uiState.asStateFlow()

    // Check which mode we're in
    val isEditMode: Boolean get() = applicationId != null

    init {
        // If editing, load the existing application data
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
        // If user selects "Interview Scheduled", keep the interview date they set
        // If they select anything else (like "Rejected"), clear the interview date (not needed anymore)
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
        // Step 1: Validate company name and job title (cannot be empty)
        val companyNameError = Utils.validateCompanyName(_uiState.value.companyName)
        val jobTitleError = Utils.validateJobTitle(_uiState.value.jobTitle)

        if (companyNameError != null || jobTitleError != null) {
            // Show error messages under the text fields
            _uiState.update {
                it.copy(
                    companyNameError = companyNameError,
                    jobTitleError = jobTitleError
                )
            }
            return
        }

        // Step 2: If status is "Interview Scheduled", make sure interview date is set and in the future
        val interviewError = if (_uiState.value.status == ApplicationStatus.INTERVIEW_SCHEDULED) {
            Utils.validateInterviewTime(_uiState.value.interviewScheduledAt)
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
                    // We're editing an existing application
                    
                    // Build status history - track when status changed
                    // Example: Applied (Jan 1) → Interview (Jan 5) → Offer (Jan 10)
                    val existingHistory = _uiState.value.statusHistory.ifEmpty {
                        // If history is empty (old data), create first entry with original status
                        listOf(
                            StatusChange(
                                _uiState.value.originalStatus,
                                _uiState.value.createdAt
                            )
                        )
                    }
                    // If user changed status, add new entry to history
                    val updatedHistory = if (_uiState.value.status != _uiState.value.originalStatus) {
                        existingHistory + StatusChange(_uiState.value.status, now)
                    } else {
                        existingHistory
                    }
                    // Update the application in Firestore
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
                    // Update the notification reminder (if interview scheduled)
                    updateInterviewReminder(application)
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                } else {
                    // We're creating a new application
                    
                    // For status history, use application date if status is "Applied", otherwise use current time
                    val initialTimestamp = if (_uiState.value.status == ApplicationStatus.APPLIED) {
                        _uiState.value.applicationDate
                    } else {
                        now
                    }
                    // Create the new application
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
                    // Schedule notification for the new application
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

    /**
     * Sets up or cancels interview reminder notification
     * If status is "Interview Scheduled" and date is set: create alarm (30 min before interview)
     * If status changed to something else: cancel the alarm
     */
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