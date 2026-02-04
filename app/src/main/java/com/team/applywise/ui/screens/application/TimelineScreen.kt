package com.team.applywise.ui.screens.application

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team.applywise.data.model.ApplicationStatus
import com.team.applywise.data.model.StatusChange
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.SnackbarHost
import com.team.applywise.ui.components.NetworkStatusBanner
import com.team.applywise.core.utils.Utils
import com.team.applywise.core.utils.ConnectivityObserver
import androidx.compose.ui.platform.LocalContext

/**
 * TimelineScreen - Shows history of status changes for an application
 * 
 * Displays:
 * - Timeline of all status changes
 * - Each change shows: status name, icon, and timestamp
 * - Example:
 *   • Applied - Feb 01, 2026
 *   • Interview Scheduled - Feb 05, 2026
 *   • Offer Received - Feb 10, 2026
 * 
 * Shows in chronological order (oldest first)
 * If no status history exists, shows message
 * 
 * This helps user see the progression of their application
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    applicationId: String, // ID of application to show timeline for
    onNavigateBack: () -> Unit,
    viewModel: ApplicationDetailViewModel = hiltViewModel() // Reuses same ViewModel as detail screen
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val connectivityObserver = remember { ConnectivityObserver(context) }
    val isOnline by connectivityObserver.observe().collectAsStateWithLifecycle(initialValue = true)

    // Show error messages in Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Timeline", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
            NetworkStatusBanner(isOffline = !isOnline)
            if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            uiState.application?.let { application ->
                val events = remember(application) {
                    val statusHistory = application.statusHistory.ifEmpty {
                        listOf(StatusChange(ApplicationStatus.APPLIED, application.applicationDate))
                    }
                    val interviewStatusChange = statusHistory.lastOrNull {
                        it.status == ApplicationStatus.INTERVIEW_SCHEDULED
                    }?.timestamp
                    buildList {
                        // Application created
                        add(
                            TimelineEvent(
                                title = "Application Created",
                                description = "You added this application to ApplyWise",
                                timestamp = application.createdAt,
                                icon = Icons.Default.AddCircle
                            )
                        )

                        // Status changes
                        statusHistory.forEach { change ->
                            when (change.status) {
                                ApplicationStatus.APPLIED -> {
                                    add(
                                        TimelineEvent(
                                            title = "Applied to ${application.companyName}",
                                            description = "Position: ${application.jobTitle}",
                                            timestamp = change.timestamp,
                                            icon = Icons.Default.Send
                                        )
                                    )
                                }
                                ApplicationStatus.INTERVIEW_SCHEDULED -> {
                                    add(
                                        TimelineEvent(
                                            title = "Interview Scheduled",
                                            description = "Status updated to Interview Scheduled",
                                            timestamp = change.timestamp,
                                            icon = Icons.Default.Event
                                        )
                                    )
                                }
                                ApplicationStatus.INTERVIEW_COMPLETED -> {
                                    add(
                                        TimelineEvent(
                                            title = "Interview Completed",
                                            description = "You completed the interview process",
                                            timestamp = change.timestamp,
                                            icon = Icons.Default.CheckCircle
                                        )
                                    )
                                }
                                ApplicationStatus.OFFER_RECEIVED -> {
                                    add(
                                        TimelineEvent(
                                            title = "Offer Received",
                                            description = "Congratulations! You received an offer",
                                            timestamp = change.timestamp,
                                            icon = Icons.Default.EmojiEvents
                                        )
                                    )
                                }
                                ApplicationStatus.REJECTED -> {
                                    add(
                                        TimelineEvent(
                                            title = "Application Rejected",
                                            description = "Keep applying, success is around the corner",
                                            timestamp = change.timestamp,
                                            icon = Icons.Default.Cancel
                                        )
                                    )
                                }
                                ApplicationStatus.NO_RESPONSE -> {
                                    add(
                                        TimelineEvent(
                                            title = "No Response Yet",
                                            description = "Waiting to hear back from the company",
                                            timestamp = change.timestamp,
                                            icon = Icons.Default.HourglassEmpty
                                        )
                                    )
                                }
                            }
                        }

                        application.interviewScheduledAt?.let { interviewTime ->
                            add(
                                TimelineEvent(
                                    title = "Interview Scheduled For",
                                    description = "Scheduled for: ${Utils.formatTimelineDate(interviewTime)}",
                                    timestamp = interviewStatusChange ?: application.updatedAt,
                                    icon = Icons.Default.Event
                                )
                            )
                        }
                    }.sortedByDescending { it.timestamp }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = application.jobTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = application.companyName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Timeline History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    items(events) { event ->
                        TimelineEventItem(
                            event = event,
                            isLast = event == events.last()
                        )
                    }
                }
            }
        } }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

data class TimelineEvent(
    val title: String,
    val description: String,
    val timestamp: Long,
    val icon: ImageVector
)

@Composable
fun TimelineEventItem(
    event: TimelineEvent,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isLast) 0.dp else 24.dp)
    ) {
        // Timeline indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = event.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            if (!isLast) {
                Divider(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Event details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = Utils.formatTimelineDate(event.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}