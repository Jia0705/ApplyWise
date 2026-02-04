package com.team.applywise.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

/**
 * Avatar - Shows user's initial in a colored circle
 * 
 * Example: "Jia" shows "J" in a blue circle
 * Used in Profile screen instead of actual photos
 * 
 * @param name User's name (we use first letter)
 * @param colorName Color of circle: "red", "blue", "green", "orange", "magenta"
 */
@Composable
fun Avatar(
    name: String,
    modifier: Modifier = Modifier,
    colorName: String? = null
) {
    // Get first letter of name, uppercase
    // "Jia" -> "J", "jia" -> "J"
    val initial = name.trim().firstOrNull()?.uppercase() ?: "?"
    
    // Resolve color from name ("blue" -> Blue color)
    // If no color or invalid color, use default
    val bg = colorName?.takeIf { it.isNotBlank() }?.let { resolveColor(it) }
        ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)

    Box(
        modifier = modifier
            .clip(CircleShape) // Make it circular
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

/**
 * Convert color name string to actual Color
 * Example: "red" -> Color.Red, "blue" -> Color.Blue
 */
private fun resolveColor(value: String): Color {
    return when (value.trim().lowercase()) {
        "red" -> Color.Red
        "magenta" -> Color.Magenta
        "orange" -> Color(1f, 0.6f, 0f, 1f)
        "green" -> Color.Green
        "blue" -> Color.Blue
        else -> Color.Blue
    }
}