package com.team.applywise.ui.screens.application

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.SelectableDates
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
import androidx.navigation.NavController
import com.team.applywise.data.model.ApplicationStatus
import com.team.applywise.ui.components.DiscardChangesDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone.getTimeZone

// Add Application
@OptIn(ExperimentalMaterial3Api::class)
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

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && initialState == null) {
            initialState = uiState
        }
    }

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add Application") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (hasChanges) {
                                showDiscardDialog = true
                            } else {
                                navController.popBackStack()
                            }
                        }
                    ) {
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
            onInterviewScheduledAtChange = viewModel::onInterviewScheduledAtChange,
            onNotesChange = viewModel::onNotesChange,
            onSaveClick = viewModel::saveApplication
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

// Edit Application
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditApplicationScreen(
    navController: NavController,
    applicationId: String,
    onNavigateBack: () -> Unit,
    onApplicationUpdated: () -> Unit,
    viewModel: AddEditApplicationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var initialState by remember { mutableStateOf<ApplicationFormUiState?>(null) }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && initialState == null) {
            initialState = uiState
        }
    }

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Application") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (hasChanges) {
                                showDiscardDialog = true
                            } else {
                                navController.popBackStack()
                            }
                        }
                    ) {
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
                onInterviewScheduledAtChange = viewModel::onInterviewScheduledAtChange,
                onNotesChange = viewModel::onNotesChange,
                onSaveClick = viewModel::saveApplication
            )
        }
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
    onInterviewScheduledAtChange: (Long) -> Unit,
    onNotesChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showInterviewDatePicker by remember { mutableStateOf(false) }
    var showInterviewTimePicker by remember { mutableStateOf(false) }
    var showStatusDropdown by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    fun startOfTodayMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun endOfTodayMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun combineDateAndTime(dateMillis: Long, hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateMillis
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun hourMinuteFromMillis(millis: Long): Pair<Int, Int> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        return cal.get(Calendar.HOUR_OF_DAY) to cal.get(Calendar.MINUTE)
    }

    fun localDateMillisToUtcDateMillis(localMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = localMillis

        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val utcCal = Calendar.getInstance(getTimeZone("UTC"))
        utcCal.clear()
        utcCal.set(year, month, day)

        return utcCal.timeInMillis
    }

    fun utcDateMillisToLocalDateMillis(utcMillis: Long): Long {
        val utcCal = Calendar.getInstance(getTimeZone("UTC"))
        utcCal.timeInMillis = utcMillis

        val year = utcCal.get(Calendar.YEAR)
        val month = utcCal.get(Calendar.MONTH)
        val day = utcCal.get(Calendar.DAY_OF_MONTH)

        val localCal = Calendar.getInstance()
        localCal.clear()
        localCal.set(year, month, day)

        return localCal.timeInMillis
    }

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

        // Interview Scheduled (Interview Date)
        if (uiState.status == ApplicationStatus.INTERVIEW_SCHEDULED) {
            val interviewMillis = uiState.interviewScheduledAt

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showInterviewDatePicker = true }
            ) {
                OutlinedTextField(
                    value = interviewMillis?.let { dateFormat.format(Date(it)) } ?: "",
                    onValueChange = {},
                    label = { Text("Interview Date *") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    placeholder = { Text("Select date") },
                    isError = uiState.interviewScheduledAtError != null,
                    supportingText = {
                        uiState.interviewScheduledAtError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    readOnly = true,
                    enabled = false,

                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor =
                            if (uiState.interviewScheduledAtError != null)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor =
                            if (uiState.interviewScheduledAtError != null)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor =
                            if (uiState.interviewScheduledAtError != null)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // Interview Scheduled (Interview Time)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showInterviewTimePicker =  true}
            ) {
                OutlinedTextField(
                    value = interviewMillis?.let { timeFormat.format(Date(it)) } ?: "",
                    onValueChange = {},
                    label = { Text("Interview Time *") },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                    placeholder = { Text("Select time") },
                    readOnly = true,
                    enabled = false,

                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        OutlinedTextField(
            value = uiState.notes,
            onValueChange = onNotesChange,
            label = { Text("Remarks") },
            placeholder = { Text("E.g. recruiter name, follow-up reminder") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            enabled = !uiState.isSaving,
            maxLines = 6
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
        Button(
            onClick = onSaveClick,
            enabled = !uiState.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContentColor = MaterialTheme.colorScheme.onPrimary
            )
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
            initialSelectedDateMillis = localDateMillisToUtcDateMillis(uiState.applicationDate),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= endOfTodayMillis()
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { utcMillis ->
                            onApplicationDateChange(utcDateMillisToLocalDateMillis(utcMillis))
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

    if (showInterviewDatePicker) {
        val initialDate = uiState.interviewScheduledAt
            ?.let { localDateMillisToUtcDateMillis(it) }
            ?: System.currentTimeMillis()

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDate,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= startOfTodayMillis()
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showInterviewDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDate = datePickerState.selectedDateMillis
                        if (selectedDate != null) {
                            val baseTime = uiState.interviewScheduledAt ?: System.currentTimeMillis()
                            val (hour, minute) = hourMinuteFromMillis(baseTime)
                            val localDateMillis = utcDateMillisToLocalDateMillis(selectedDate)
                            onInterviewScheduledAtChange(combineDateAndTime(localDateMillis, hour, minute))
                        }
                        showInterviewDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInterviewDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showInterviewTimePicker) {
        val baseTime = uiState.interviewScheduledAt ?: System.currentTimeMillis()
        val (initialHour, initialMinute) = hourMinuteFromMillis(baseTime)
        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = false
        )
       AlertDialog(
            onDismissRequest = { showInterviewTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val baseDate = uiState.interviewScheduledAt ?: System.currentTimeMillis()
                        onInterviewScheduledAtChange(
                            combineDateAndTime(baseDate, timePickerState.hour, timePickerState.minute)
                        )
                        showInterviewTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInterviewTimePicker = false }) {
                    Text("Cancel")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}