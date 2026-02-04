package com.team.applywise.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.applywise.data.model.ApplicationStatus
import com.team.applywise.data.model.JobApplication
import com.team.applywise.data.repo.JobApplicationRepo
import com.team.applywise.service.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Dashboard screen
 * This calculates statistics from all applications and shows recent applications
 * It counts how many applications are in each status (Applied, Interview, Offer, etc.)
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authService: AuthService,
    private val applicationRepo: JobApplicationRepo
) : ViewModel() {

    // Holds all the dashboard data (counts, recent applications, etc.)
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Load data as soon as ViewModel is created
        loadDashboardData()
    }

    private fun loadDashboardData() {
        // Get current user ID - if no user logged in, exit early
        val userId = authService.getCurrentUser()?.uid ?: return

        // Load all applications and calculate statistics
        viewModelScope.launch {
            applicationRepo.getApplicationsByUser(userId)
                .catch { e ->
                    // If loading fails, show error message
                    _uiState.update {
                        it.copy(isLoading = false, error = "Failed to load applications: ${e.message}")
                    }
                }
                .collect { applications ->
                    // Count how many applications are in each status
                    // Example: If user has 3 "Interview Scheduled" and 2 "Applied", counts will be {INTERVIEW_SCHEDULED: 3, APPLIED: 2, ...}
                    val counts = ApplicationStatus.entries.associateWith { status ->
                        applications.count { it.status == status }
                    }
                    
                    // Update the screen with new data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            recentApplications = applications.take(5), // Show only first 5 applications
                            totalApplications = counts.values.sum(), // Add all counts together
                            interviewScheduledCount = counts[ApplicationStatus.INTERVIEW_SCHEDULED] ?: 0,
                            interviewCompletedCount = counts[ApplicationStatus.INTERVIEW_COMPLETED] ?: 0,
                            offersCount = counts[ApplicationStatus.OFFER_RECEIVED] ?: 0,
                            rejectedCount = counts[ApplicationStatus.REJECTED] ?: 0,
                            noResponseCount = counts[ApplicationStatus.NO_RESPONSE] ?: 0,
                            statusCounts = counts,
                            error = null
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class DashboardUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val totalApplications: Int = 0,
    val interviewScheduledCount: Int = 0,
    val interviewCompletedCount: Int = 0,
    val offersCount: Int = 0,
    val rejectedCount: Int = 0,
    val noResponseCount: Int = 0,
    val statusCounts: Map<ApplicationStatus, Int> = emptyMap(),
    val recentApplications: List<JobApplication> = emptyList()
)