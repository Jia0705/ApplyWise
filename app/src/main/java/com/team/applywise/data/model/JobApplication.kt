package com.team.applywise.data.model

data class JobApplication(
    val id: String = "",
    val userId: String = "",
    val companyName: String = "",
    val jobTitle: String = "",
    val status: ApplicationStatus = ApplicationStatus.APPLIED,
    val applicationDate: Long = System.currentTimeMillis(),
    val interviewScheduledAt: Long? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val statusHistory: List<StatusChange> = emptyList()
) {
    fun toMap(): Map<String, Any> {
        val base = mutableMapOf<String, Any>(
            "id" to id,
            "userId" to userId,
            "companyName" to companyName,
            "jobTitle" to jobTitle,
            "status" to status.name,
            "applicationDate" to applicationDate,
            "notes" to notes,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
        if (interviewScheduledAt != null) {
            base["interviewScheduledAt"] = interviewScheduledAt
        }
        if (notes.isNotBlank()) {
            base["notes"] = notes
        }
        if (statusHistory.isNotEmpty()) {
            base["statusHistory"] = statusHistory.map { it.toMap() }
        }
        return base
    }

    companion object {
        // Create JobApplication from Firestore document
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

data class StatusChange(
    val status: ApplicationStatus,
    val timestamp: Long
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