package com.team.applywise.core.utils

import android.util.Patterns
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utils - Helper functions used throughout the app
 * Contains validation and date formatting to keep code clean and reusable
 */
object Utils {
    // Check if email format is valid (has @ and proper structure)
    fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    
    // ===== DATE FORMATTING FUNCTIONS =====
    // These convert timestamps (numbers) into readable dates
    // Example: 1706918400000 → "Feb 03, 2026 02:00 PM"
    
    // Format: Feb 03, 2026 02:00 PM (used in most places)
    fun formatDateTime(timeMillis: Long): String {
        return SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date(timeMillis))
    }
    
    // Format: February 03, 2026 02:00 PM (full month name)
    fun formatFullDateTime(timeMillis: Long): String {
        return SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date(timeMillis))
    }
    
    // Format: Feb 03, 2026 (date only, no time)
    fun formatDate(timeMillis: Long): String {
        return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timeMillis))
    }
    
    // Format: Wed, Feb 03, 2026 (includes day of week)
    fun formatDateWithDay(timeMillis: Long): String {
        return SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()).format(Date(timeMillis))
    }
    
    // Format: 02:00 PM (time only, no date)
    fun formatTime(timeMillis: Long): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timeMillis))
    }
    
    // Format: Feb 03, 2026 at 02:00 PM (used in timeline screen)
    fun formatTimelineDate(timeMillis: Long): String {
        return SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault()).format(Date(timeMillis))
    }
    
    // ===== VALIDATION FUNCTIONS =====
    // These check user input and return error messages if something is wrong
    
    /**
     * Validate email input
     * Returns: null if valid, error message if invalid
     */
    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required" // Empty input
            !isValidEmail(email) -> "Invalid email format" // Wrong format (no @ etc)
            else -> null // All good!
        }
    }
    
    /**
     * Validate password input
     * Returns: null if valid, error message if invalid
     */
    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required" // Empty input
            password.length < 6 -> "Password must be at least 6 characters" // Too short
            else -> null // All good!
        }
    }
    
    fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Name is required"
            else -> null
        }
    }
    
    fun validateLoginCredentials(email: String, password: String): String? {
        validateEmail(email)?.let { return it }
        validatePassword(password)?.let { return it }
        return null
    }
    
    fun validateRegisterCredentials(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): String? {
        validateName(name)?.let { return it }
        validateEmail(email)?.let { return it }
        validatePassword(password)?.let { return it }
        if (confirmPassword.isBlank()) return "Confirm password is required"
        if (password != confirmPassword) return "Passwords do not match"
        return null
    }
    
    fun validateCompanyName(companyName: String): String? {
        return if (companyName.isBlank()) "Company name is required" else null
    }
    
    fun validateJobTitle(jobTitle: String): String? {
        return if (jobTitle.isBlank()) "Job title is required" else null
    }
    
    fun validateInterviewTime(interviewTime: Long?): String? {
        return when {
            interviewTime == null -> "Interview date and time are required"
            interviewTime < System.currentTimeMillis() -> "Interview date/time cannot be in the past"
            else -> null
        }
    }
}