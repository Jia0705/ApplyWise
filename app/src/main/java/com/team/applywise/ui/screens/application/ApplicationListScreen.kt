package com.team.applywise.ui.screens.application

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team.applywise.data.model.ApplicationStatus
import com.team.applywise.ui.components.JobApplicationCard
import com.team.applywise.ui.components.EmptyApplicationState
import com.team.applywise.ui.components.NetworkStatusBanner
import com.team.applywise.core.utils.ConnectivityObserver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.SnackbarHost
import androidx.compose.ui.unit.sp

/**
 * ApplicationListScreen - Shows all job applications with search and filter
 * 
 * Features:
 * 1. Search bar - Search by company name or job title
 * 2. Filter chips - Filter by status (Applied, Interview, Offer, etc.)
 * 3. List of applications - Shows company, job, status, dates
 * 4. FAB (+ button) - Quick add new application
 * 5. Click application -> navigate to detail
 * 
 * Real-time updates: When applications are added/edited in Firestore,
 * this list automatically updates (thanks to Flow in ViewModel)
 */
// Application List screen with search and filter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationListScreen(
    onNavigateToDetail: (String) -> Unit, // Navigate to application detail
    onNavigateToAdd: () -> Unit, // Navigate to add new application
    onNavigateBack: () -> Unit, // Back button
    initialFilter: String? = null, // Initial filter (if navigated from Dashboard stat)
    viewModel: ApplicationListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val selectedSort by viewModel.selectedSort.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSortMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val connectivityObserver = remember { ConnectivityObserver(context) }
    val isOnline by connectivityObserver.observe().collectAsStateWithLifecycle(initialValue = true)

    LaunchedEffect(initialFilter) {
        viewModel.onFilterSelected(initialFilter)
    }

    // Show error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Custom header with back button and title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                
                Text(
                    text = "Applications",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            NetworkStatusBanner(isOffline = !isOnline)
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
            // Search bar and Sort button in a Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search field (takes most space)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search company or job") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true
                )
                
                // Sort button with dropdown
                Box {
                    IconButton(
                        onClick = { showSortMenu = true }
                    ) {
                        Icon(
                            Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortOption.entries.forEach { sortOption ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        sortOption.displayName,
                                        fontWeight = if (selectedSort == sortOption) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    viewModel.onSortSelected(sortOption)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Filter chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { viewModel.onFilterSelected(null) },
                        label = { Text("All") }
                    )
                }
                items(ApplicationStatus.entries.toTypedArray()) { status ->
                    FilterChip(
                        selected = selectedFilter == status.name,
                        onClick = { viewModel.onFilterSelected(status.name) },
                        label = { Text(status.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Application list
            if (uiState.isLoading && uiState.applications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.applications.isEmpty()) {
                EmptyApplicationState(
                    message = if (searchQuery.isNotEmpty() || selectedFilter != null) {
                        "No applications found"
                    } else {
                        "No applications yet"
                    },
                    onAddClick = onNavigateToAdd
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.applications) { application ->
                        JobApplicationCard(
                            application = application,
                            onClick = { onNavigateToDetail(application.id) }
                        )
                    }
                }
            } }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        FloatingActionButton(
            onClick = onNavigateToAdd,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Application")
        }
    }
}