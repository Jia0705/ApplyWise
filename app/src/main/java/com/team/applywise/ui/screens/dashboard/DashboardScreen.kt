package com.team.applywise.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.team.applywise.data.model.ApplicationStatus
import androidx.compose.foundation.layout.Column
import com.team.applywise.ui.components.JobApplicationCard
import com.team.applywise.ui.components.EmptyApplicationState
import com.team.applywise.ui.components.NetworkStatusBanner
import com.team.applywise.core.utils.ConnectivityObserver
import androidx.compose.ui.platform.LocalContext
import kotlin.math.cos
import kotlin.math.sin
import android.graphics.Paint
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import java.lang.Math.toRadians

/**
 * DashboardScreen - Home screen showing application statistics and recent applications
 * 
 * Displays:
 * 1. Status pie chart - Visual breakdown of applications by status
 * 2. Quick statistics - Numbers for each status (Interview, Offers, Rejected, etc.)
 * 3. Recent applications - Last 5 applications added/updated
 * 4. FAB (+ button) - Quick add new application
 * 
 * All data comes from DashboardViewModel which counts applications in Firestore
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToApplicationList: (String?) -> Unit, // Navigate to list (optionally filtered by status)
    onNavigateToAddApplication: () -> Unit, // Navigate to Add Application screen
    onNavigateToApplicationDetail: (String) -> Unit, // Navigate to detail of specific application
    viewModel: DashboardViewModel = hiltViewModel()
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
            // Custom header with title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dashboard",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            
            NetworkStatusBanner(isOffline = !isOnline)
        
        if (uiState.isLoading && uiState.recentApplications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            )
 {
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
                        EmptyApplicationState(
                            message = "No applications yet",
                            buttonText = "Add Your First Application",
                            onAddClick = onNavigateToAddApplication
                        )
                    }
                } else {
                    items(uiState.recentApplications) { application ->
                        JobApplicationCard(
                            application = application,
                            onClick = { onNavigateToApplicationDetail(application.id) }
                        )
                    }
                }
            }
        } }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        
        FloatingActionButton(
            onClick = onNavigateToAddApplication,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Application")
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
    segments: List<PieSegment>, // List of pie slices (value + color)
    size: Dp // Size of the pie chart
) {

    // Sum of all segment values (used to calculate percentages)
    val total = segments.sumOf { it.value }

    // Canvas lets us draw custom shapes
    Canvas(
        modifier = Modifier.size(size)
    ) {

        // If total is zero or negative, do not draw anything
        if (total <= 0) return@Canvas

        // Start from top (12 o'clock position)
        var startAngle = -90f

        // Radius of the pie (half of width/height)
        val radius = size.toPx() / 2f

        // Paint used to draw percentage text
        val labelPaint = Paint().apply {
            textSize = radius * 0.16f // text size relative to pie size
            isAntiAlias = true        // smooth text
            textAlign = Paint.Align.CENTER
        }

        // Loop through each pie segment
        segments.forEach { segment ->

            // Calculate how big this slice should be (in degrees)
            val sweep = (segment.value.toFloat() / total.toFloat()) * 360f

            // Draw the pie slice
            drawArc(
                color = segment.color,   // slice color
                startAngle = startAngle, // where slice starts
                sweepAngle = sweep,      // how large the slice is
                useCenter = true,        // draw from center (pie style)
                size = Size(size.toPx(), size.toPx())
            )

            // Only draw percentage text if slice is big enough
            if (sweep >= 12f) {

                // Angle in the middle of the slice
                val midAngle = startAngle + (sweep / 2f)

                // Calculate percentage value
                val percent =
                    (segment.value.toFloat() / total.toFloat()) * 100f

                // Convert percentage to text (e.g. "25%")
                val label = "${percent.toInt()}%"

                // Text color
                labelPaint.color = android.graphics.Color.BLACK

                // Convert angle to radians (for sin & cos)
                val angleRad = toRadians(midAngle.toDouble())

                // Distance of label from center
                val labelRadius = radius * 0.6f

                // X position of text
                val x =
                    (radius + (labelRadius * cos(angleRad))).toFloat()

                // Y position of text
                val y =
                    (radius + (labelRadius * sin(angleRad))).toFloat()

                // Draw percentage text inside slice
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    x,
                    y,
                    labelPaint
                )
            }
            // Move startAngle forward for next slice
            startAngle += sweep
        }
    }
}

data class PieSegment(
    val label: String,
    val value: Int,
    val color: Color
)