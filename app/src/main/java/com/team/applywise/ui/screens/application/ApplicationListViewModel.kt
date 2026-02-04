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

/**
 * Sort options for application list
 */
enum class SortOption(val displayName: String) {
    LATEST("Latest"),
    EARLIEST("Earliest"),
    A_TO_Z("A to Z"),
    Z_TO_A("Z to A")
}

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

    private val _selectedSort = MutableStateFlow(SortOption.LATEST)
    val selectedSort = _selectedSort.asStateFlow()

    init {
        loadApplications()
    }

    private fun loadApplications() {
        val userId = authService.getCurrentUser()?.uid ?: return

        viewModelScope.launch {
            // Combine four data sources: applications list, search text, selected filter, and sort option
            // Whenever any of these change, this code runs again
            combine(
                applicationRepo.getApplicationsByUser(userId),
                _searchQuery,
                _selectedFilter,
                _selectedSort
            ) { applications, query, filter, sort ->
                var filtered = applications

                // Step 1: Apply status filter if user selected one (like "Interview Scheduled" only)
                if (filter != null) {
                    val status = ApplicationStatus.fromString(filter)
                    filtered = filtered.filter { it.status == status }
                }

                // Step 2: Apply search query if user typed something
                // Search in both company name and job title (ignores uppercase/lowercase)
                // Example: searching "google" will find "Google", "GOOGLE", or "google"
                if (query.isNotBlank()) {
                    filtered = filtered.filter {
                        it.companyName.contains(query, ignoreCase = true) || it.jobTitle.contains(query, ignoreCase = true)
                    }
                }
                
                // Step 3: Apply sorting
                filtered = when (sort) {
                    SortOption.LATEST -> filtered.sortedByDescending { it.updatedAt } // Most recently updated first
                    SortOption.EARLIEST -> filtered.sortedBy { it.updatedAt } // Least recently updated first
                    SortOption.A_TO_Z -> filtered.sortedBy { it.jobTitle.lowercase() } // Alphabetical by job title
                    SortOption.Z_TO_A -> filtered.sortedByDescending { it.jobTitle.lowercase() } // Reverse alphabetical by job title
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

    fun onSortSelected(sort: SortOption) {
        _selectedSort.value = sort
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