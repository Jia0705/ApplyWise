package com.team.applywise.ui.screens.application

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team.applywise.data.model.ApplicationStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Add Application
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddApplicationScreen(
    onNavigateBack: () -> Unit,
    onApplicationAdded: () -> Unit,
    viewModel: ApplicationFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onApplicationAdded()
        }
    }

    // Show error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add Application") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        ApplicationForm(
            modifier = Modifier.padding(padding),
            uiState = uiState,
            onCompanyNameChange = viewModel::onCompanyNameChange,
            onJobTitleChange = viewModel::onJobTitleChange,
            onStatusChange = viewModel::onStatusChange,
            onApplicationDateChange = viewModel::onApplicationDateChange,
            onSaveClick = viewModel::saveApplication
        )
    }
}

// Edit Application
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditApplicationScreen(
    applicationId: String,
    onNavigateBack: () -> Unit,
    onApplicationUpdated: () -> Unit,
    viewModel: ApplicationFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onApplicationUpdated()
        }
    }

    // Show error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Application") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            ApplicationForm(
                modifier = Modifier.padding(padding),
                uiState = uiState,
                onCompanyNameChange = viewModel::onCompanyNameChange,
                onJobTitleChange = viewModel::onJobTitleChange,
                onStatusChange = viewModel::onStatusChange,
                onApplicationDateChange = viewModel::onApplicationDateChange,
                onSaveClick = viewModel::saveApplication
            )
        }
    }
}

// Reusable form component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationForm(
    modifier: Modifier = Modifier,
    uiState: ApplicationFormUiState,
    onCompanyNameChange: (String) -> Unit,
    onJobTitleChange: (String) -> Unit,
    onStatusChange: (ApplicationStatus) -> Unit,
    onApplicationDateChange: (Long) -> Unit,
    onSaveClick: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showStatusDropdown by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Company Name
        OutlinedTextField(
            value = uiState.companyName,
            onValueChange = onCompanyNameChange,
            label = { Text("Company Name *") },
            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
            isError = uiState.companyNameError != null,
            supportingText = {
                uiState.companyNameError?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving,
            singleLine = true
        )

        // Job Title
        OutlinedTextField(
            value = uiState.jobTitle,
            onValueChange = onJobTitleChange,
            label = { Text("Job Title *") },
            leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
            isError = uiState.jobTitleError != null,
            supportingText = {
                uiState.jobTitleError?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving,
            singleLine = true
        )

        // Application Date
        OutlinedTextField(
            value = dateFormat.format(Date(uiState.applicationDate)),
            onValueChange = {},
            label = { Text("Application Date") },
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // Status Dropdown
        ExposedDropdownMenuBox(
            expanded = showStatusDropdown,
            onExpandedChange = { showStatusDropdown = it }
        ) {
            OutlinedTextField(
                value = uiState.status.displayName,
                onValueChange = {},
                label = { Text("Status") },
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                trailingIcon = { TrailingIcon(expanded = showStatusDropdown) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                readOnly = true,
                enabled = !uiState.isSaving
            )
            ExposedDropdownMenu(
                expanded = showStatusDropdown,
                onDismissRequest = { showStatusDropdown = false }
            ) {
                ApplicationStatus.entries.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status.displayName) },
                        onClick = {
                            onStatusChange(status)
                            showStatusDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !uiState.isSaving
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save Application")
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.applicationDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onApplicationDateChange(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}