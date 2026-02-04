package com.team.applywise.data.model

/**
 * JobApplication - Represents one job application
 * Contains all information about an application:
 * - Company and job details
 * - Current status (Applied, Interview, Offer, etc.)
 * - Interview date (if scheduled)
 * - Notes
 * - Status history (tracks when status changed)
 */
data class JobApplication(
    val id: String = "", // Unique ID from Firestore
    val userId: String = "", // Who created this application
    val companyName: String = "",
    val jobTitle: String = "",
    val status: ApplicationStatus = ApplicationStatus.APPLIED,
    val applicationDate: Long = System.currentTimeMillis(), // When user applied
    val interviewScheduledAt: Long? = null, // When interview is scheduled (null if no interview)
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(), // When record was created
    val updatedAt: Long = System.currentTimeMillis(), // When record was last modified
    val statusHistory: List<StatusChange> = emptyList() // List of status changes with timestamps
) {
    /**
     * Convert JobApplication to Map for saving to Firestore
     * Firestore stores data as key-value pairs, not as Kotlin objects
     */
    fun toMap(): Map<String, Any> {
        val base = mutableMapOf<String, Any>(
            "id" to id,
            "userId" to userId,
            "companyName" to companyName,
            "jobTitle" to jobTitle,
            "status" to status.name, // Save enum as string ("APPLIED", "INTERVIEW_SCHEDULED", etc.)
            "applicationDate" to applicationDate,
            "notes" to notes,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
        // Only add interviewScheduledAt if it exists (not null)
        if (interviewScheduledAt != null) {
            base["interviewScheduledAt"] = interviewScheduledAt
        }
        if (notes.isNotBlank()) {
            base["notes"] = notes
        }
        // Convert statusHistory list to list of maps
        if (statusHistory.isNotEmpty()) {
            base["statusHistory"] = statusHistory.map { it.toMap() }
        }
        return base
    }

    companion object {
        /**
         * Create JobApplication from Firestore Map
         * Converts Map (from Firestore) back to Kotlin object
         */
        fun fromMap(map: Map<String, Any>): JobApplication {
            val history = (map["statusHistory"] as? List<*>)?.mapNotNull { entry ->
                (entry as? Map<*, *>)?.let { StatusChange.fromMap(it) }
            } ?: emptyList()
            return JobApplication(
                id = map["id"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                companyName = map["companyName"] as? String ?: "",
                jobTitle = map["jobTitle"] as? String ?: "",
                status = ApplicationStatus.fromString(map["status"] as? String ?: "APPLIED"),
                applicationDate = map["applicationDate"] as? Long ?: System.currentTimeMillis(),
                interviewScheduledAt = map["interviewScheduledAt"] as? Long,
                notes = map["notes"] as? String ?: "",
                createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis(),
                updatedAt = map["updatedAt"] as? Long ?: System.currentTimeMillis(),
                statusHistory = history
            )
        }
    }
}

/**
 * StatusChange - Records when status was changed
 * Example: User changed from "Applied" to "Interview Scheduled" on Jan 5, 2026
 * This helps us show a timeline of the application progress
 */
data class StatusChange(
    val status: ApplicationStatus, // The status it changed to
    val timestamp: Long // When the change happened (milliseconds)
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "status" to status.name,
            "timestamp" to timestamp
        )
    }

    companion object {
        fun fromMap(map: Map<*, *>): StatusChange? {
            val statusValue = map["status"] as? String ?: return null
            val timestamp = map["timestamp"] as? Long ?: return null
            return StatusChange(
                status = ApplicationStatus.fromString(statusValue),
                timestamp = timestamp
            )
        }
    }
}