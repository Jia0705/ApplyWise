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

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authService: AuthService,
    private val applicationRepo: JobApplicationRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val userId = authService.getCurrentUser()?.uid ?: return

        // Load applications for recent list and counts
        viewModelScope.launch {
            applicationRepo.getApplicationsByUser(userId)
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = "Failed to load applications: ${e.message}")
                    }
                }
                .collect { applications ->
                    val counts = ApplicationStatus.entries.associateWith { status ->
                        applications.count { it.status == status }
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            recentApplications = applications.take(5),
                            totalApplications = counts.values.sum(),
                            interviewingCount = (counts[ApplicationStatus.INTERVIEW_SCHEDULED] ?: 0) + (counts[ApplicationStatus.INTERVIEW_COMPLETED] ?: 0),
                            offersCount = counts[ApplicationStatus.OFFER_RECEIVED] ?: 0,
                            rejectedCount = counts[ApplicationStatus.REJECTED] ?: 0,
                            noResponseCount = counts[ApplicationStatus.NO_RESPONSE] ?: 0,
                            error = null
                        )
                    }
                }
        }
    }

//    fun refresh() {
//        _uiState.update { it.copy(isLoading = true, error = null) }
//        loadDashboardData()
//    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class DashboardUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val totalApplications: Int = 0,
    val interviewingCount: Int = 0,
    val offersCount: Int = 0,
    val rejectedCount: Int = 0,
    val noResponseCount: Int = 0,
    val recentApplications: List<JobApplication> = emptyList()
)