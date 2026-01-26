package com.team.applywise.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team.applywise.data.model.ApplicationStatus

@Composable
fun ApplicationStatusBadge(
    status: ApplicationStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        ApplicationStatus.APPLIED -> Color.Blue.copy(alpha = 0.15f) to Color.Blue
        ApplicationStatus.INTERVIEW_SCHEDULED -> Color.Yellow.copy(alpha = 0.2f) to Color(0.45f, 0.32f, 0f, 1f)
        ApplicationStatus.INTERVIEW_COMPLETED -> Color(1f, 0.6f, 0f, 0.2f) to Color(0.8f, 0.35f, 0f, 1f)
        ApplicationStatus.OFFER_RECEIVED -> Color.Green.copy(alpha = 0.2f) to Color(0f, 0.4f, 0f, 1f)
        ApplicationStatus.REJECTED -> Color.Red.copy(alpha = 0.15f) to Color.Red
        ApplicationStatus.NO_RESPONSE -> Color.Gray.copy(alpha = 0.2f) to Color.DarkGray
    }

    Text(
        text = status.displayName,
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        color = textColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
    )
}