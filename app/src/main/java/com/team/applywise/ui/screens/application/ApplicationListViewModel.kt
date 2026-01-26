package com.team.applywise.ui.screens.application

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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ViewModel for Application List screen
// Handles listing, filtering, and searching applications
@HiltViewModel
class ApplicationListViewModel @Inject constructor(
    private val authService: AuthService,
    private val applicationRepo: JobApplicationRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApplicationListUiState())
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow<String?>(null)
    val selectedFilter = _selectedFilter.asStateFlow()

    init {
        loadApplications()
    }

    private fun loadApplications() {
        val userId = authService.getCurrentUser()?.uid ?: return

        viewModelScope.launch {
            combine(
                applicationRepo.getApplicationsByUser(userId),
                _searchQuery,
                _selectedFilter
            ) { applications, query, filter ->
                var filtered = applications

                // Apply status filter
                if (filter != null) {
                    val status = ApplicationStatus.fromString(filter)
                    filtered = filtered.filter { it.status == status }
                }

                // Apply search query
                if (query.isNotBlank()) {
                    filtered = filtered.filter {
                        it.companyName.contains(query, ignoreCase = true) || it.jobTitle.contains(query, ignoreCase = true)
                    }
                }
                filtered
            }
            .catch { e ->
                _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load applications: ${e.message}")
                }
            }
            .collect { applications ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        applications = applications,
                        error = null
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(filter: String?) {
        _selectedFilter.value = filter
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ApplicationListUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val applications: List<JobApplication> = emptyList()
)