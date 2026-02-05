package com.team.applywise.ui.screens.application

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.team.applywise.core.utils.ConnectivityObserver
import com.team.applywise.ui.components.ApplicationFormFields
import com.team.applywise.ui.components.DiscardChangesDialog
import com.team.applywise.ui.components.NetworkStatusBanner

/**
 * AddApplicationScreen - Form to create a new job application
 * 
 * User fills in:
 * - Company name (required)
 * - Job title (required)
 * - Status (Applied, Interview Scheduled, etc.)
 * - Application date
 * - Interview date/time (only if status = Interview Scheduled)
 * - Notes (optional)
 * 
 * When saved:
 * - Creates new application in Firestore
 * - If interview scheduled, sets up reminder notification (30 min before)
 * - Navigates back to list
 */
@Composable
fun AddApplicationScreen(
    navController: NavController,
    onNavigateBack: () -> Unit,
    onApplicationAdded: () -> Unit,
    viewModel: AddEditApplicationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var initialState by remember { mutableStateOf<ApplicationFormUiState?>(null) }
    val context = LocalContext.current
    val connectivityObserver = remember { ConnectivityObserver(context) }
    val isOnline by connectivityObserver.observe().collectAsStateWithLifecycle(initialValue = true)

    // Remember initial state to detect if user made changes
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && initialState == null) {
            initialState = uiState
        }
    }

    // When save succeeds, navigate back
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onApplicationAdded()
        }
    }

    // Show error messages in Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val hasChanges = initialState?.let {
        uiState.companyName != it.companyName ||
                uiState.jobTitle != it.jobTitle ||
                uiState.status != it.status ||
                uiState.applicationDate != it.applicationDate ||
                uiState.interviewScheduledAt != it.interviewScheduledAt ||
                uiState.notes != it.notes
    } ?: false

    BackHandler {
        if (hasChanges) {
            showDiscardDialog = true
        } else {
            navController.popBackStack()
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
                IconButton(onClick = {
                    if (hasChanges) {
                        showDiscardDialog = true
                    } else {
                        navController.popBackStack()
                    }
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                
                Text(
                    text = "Add Application",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            NetworkStatusBanner(isOffline = !isOnline)
            ApplicationFormFields(
                modifier = Modifier,
                uiState = uiState,
                onCompanyNameChange = viewModel::onCompanyNameChange,
                onJobTitleChange = viewModel::onJobTitleChange,
                onStatusChange = viewModel::onStatusChange,
                onApplicationDateChange = viewModel::onApplicationDateChange,
                onInterviewScheduledAtChange = viewModel::onInterviewScheduledAtChange,
                onNotesChange = viewModel::onNotesChange,
                onSaveClick = viewModel::saveApplication
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showDiscardDialog) {
        DiscardChangesDialog(
            onDiscard = {
                showDiscardDialog = false
                navController.popBackStack()
            },
            onDismiss = { showDiscardDialog = false }
        )
    }
}