package com.team.applywise.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team.applywise.data.model.JobApplication
import com.team.applywise.data.model.ApplicationStatus
import com.team.applywise.ui.components.ApplicationStatusBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import android.graphics.Paint
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import java.lang.Math.toRadians

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToApplicationList: (String?) -> Unit,
    onNavigateToAddApplication: () -> Unit,
    onNavigateToApplicationDetail: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
                title = { Text("Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddApplication,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Application")
            }
        }
    ) { padding ->
        if (uiState.isLoading && uiState.recentApplications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    StatusPieCard(
                        statusCounts = uiState.statusCounts,
                        totalApplications = uiState.totalApplications
                    )
                }

                // Summary section
                item {
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Summary cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            title = "Total",
                            count = uiState.totalApplications,
                            icon = Icons.Default.Work,
                            containerColor = Color.Blue.copy(alpha = 0.18f),
                            contentColor = Color.Blue,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToApplicationList(null) }
                        )
                        SummaryCard(
                            title = "Interview Scheduled",
                            count = uiState.interviewScheduledCount,
                            icon = Icons.Default.Event,
                            containerColor = Color.Yellow.copy(alpha = 0.2f),
                            contentColor = Color(0.45f, 0.32f, 0f, 1f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToApplicationList("INTERVIEW_SCHEDULED") }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            title = "Interview Completed",
                            count = uiState.interviewCompletedCount,
                            icon = Icons.Default.CheckCircle,
                            containerColor = Color(1f, 0.6f, 0f, 0.2f),
                            contentColor = Color(0.8f, 0.35f, 0f, 1f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToApplicationList("INTERVIEW_COMPLETED") }
                        )
                        SummaryCard(
                            title = "Rejected",
                            count = uiState.rejectedCount,
                            icon = Icons.Default.Cancel,
                            containerColor = Color.Red.copy(alpha = 0.18f),
                            contentColor = Color.Red,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToApplicationList("REJECTED") }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            title = "Offers",
                            count = uiState.offersCount,
                            icon = Icons.Default.CheckCircle,
                            containerColor = Color.Green.copy(alpha = 0.2f),
                            contentColor = Color(0f, 0.4f, 0f, 1f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToApplicationList("OFFER_RECEIVED") }
                        )
                        SummaryCard(
                            title = "No Response",
                            count = uiState.noResponseCount,
                            icon = Icons.Default.Work,
                            containerColor = Color.Gray.copy(alpha = 0.2f),
                            contentColor = Color.DarkGray,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToApplicationList("NO_RESPONSE") }
                        )
                    }
                }

                // Recent applications section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Applications",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { onNavigateToApplicationList(null) }) {
                            Text("View All")
                        }
                    }
                }

                if (uiState.recentApplications.isEmpty()) {
                    item {
                        EmptyState(
                            message = "No applications yet",
                            onAddClick = onNavigateToAddApplication
                        )
                    }
                } else {
                    items(uiState.recentApplications) { application ->
                        ApplicationItem(
                            application = application,
                            onClick = { onNavigateToApplicationDetail(application.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    count: Int,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor
            )
        }
    }
}

@Composable
fun StatusPieCard(
    statusCounts: Map<ApplicationStatus, Int>,
    totalApplications: Int
) {
    val statusColors = remember {
        mapOf(
            ApplicationStatus.APPLIED to Color.Blue.copy(alpha = 0.35f),
            ApplicationStatus.INTERVIEW_SCHEDULED to Color.Yellow.copy(alpha = 0.35f),
            ApplicationStatus.INTERVIEW_COMPLETED to Color(1f, 0.6f, 0f, 0.35f),
            ApplicationStatus.OFFER_RECEIVED to Color.Green.copy(alpha = 0.35f),
            ApplicationStatus.REJECTED to Color.Red.copy(alpha = 0.35f),
            ApplicationStatus.NO_RESPONSE to Color.Gray.copy(alpha = 0.35f)
        )
    }

    val segments = ApplicationStatus.entries.map { status ->
        PieSegment(
            label = status.displayName,
            value = statusCounts[status] ?: 0,
            color = statusColors[status] ?: MaterialTheme.colorScheme.primary
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Application Status Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (totalApplications == 0) {
                Text(
                    text = "No applications yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PieChart(
                        segments = segments,
                        size = 140.dp
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        segments.filter { it.value > 0 }.forEach { segment ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(segment.color, shape = MaterialTheme.shapes.small)
                                )
                                Text(
                                    text = "${segment.label}: ${segment.value}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(
    segments: List<PieSegment>,
    size: Dp
) {
    val total = segments.sumOf { it.value }
    Canvas(
        modifier = Modifier.size(size)
    ) {
        if (total <= 0) return@Canvas
        var startAngle = -90f
        val radius = size.toPx() / 2f
        val labelPaint = Paint().apply {
            textSize = radius * 0.16f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        segments.forEach { segment ->
            val sweep = (segment.value.toFloat() / total.toFloat()) * 360f
            drawArc(
                color = segment.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                size = Size(size.toPx(), size.toPx())
            )
            if (sweep >= 12f) {
                val midAngle = startAngle + (sweep / 2f)
                val percent = (segment.value.toFloat() / total.toFloat()) * 100f
                val label = "${percent.toInt()}%"
                labelPaint.color = android.graphics.Color.BLACK
                val angleRad = toRadians(midAngle.toDouble())
                val labelRadius = radius * 0.6f
                val x = (radius + (labelRadius * cos(angleRad))).toFloat()
                val y = (radius + (labelRadius * sin(angleRad))).toFloat()
                drawContext.canvas.nativeCanvas.drawText(label, x, y, labelPaint)
            }
            startAngle += sweep
        }
    }
}

data class PieSegment(
    val label: String,
    val value: Int,
    val color: Color
)

@Composable
fun ApplicationItem(
    application: JobApplication,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = application.jobTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = application.companyName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ApplicationStatusBadge(status = application.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Applied: ${dateFormat.format(Date(application.applicationDate))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Work,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Your First Application")
        }
    }
}