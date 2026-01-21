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
        ApplicationStatus.INTERVIEW_SCHEDULED -> Color.Yellow.copy(alpha = 0.45f) to Color.Black
        ApplicationStatus.INTERVIEW_COMPLETED -> Color.Magenta.copy(alpha = 0.15f) to Color.Magenta
        ApplicationStatus.OFFER_RECEIVED -> Color.Green.copy(alpha = 0.45f) to Color.White
        ApplicationStatus.REJECTED -> Color.Red.copy(alpha = 0.15f) to Color.Red
        ApplicationStatus.NO_RESPONSE -> Color.Black.copy(alpha = 0.15f) to Color.Black
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