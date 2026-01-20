package com.team.applywise.data.model

data class JobApplication(
    val id: String = "",
    val userId: String = "",
    val companyName: String = "",
    val jobTitle: String = "",
    val status: ApplicationStatus = ApplicationStatus.APPLIED,
    val applicationDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "companyName" to companyName,
            "jobTitle" to jobTitle,
            "status" to status.name,
            "applicationDate" to applicationDate,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        // Create JobApplication from Firestore document
        fun fromMap(map: Map<String, Any>): JobApplication {
            return JobApplication(
                id = map["id"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                companyName = map["companyName"] as? String ?: "",
                jobTitle = map["jobTitle"] as? String ?: "",
                status = ApplicationStatus.fromString(map["status"] as? String ?: "APPLIED"),
                applicationDate = map["applicationDate"] as? Long ?: System.currentTimeMillis(),
                createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis(),
                updatedAt = map["updatedAt"] as? Long ?: System.currentTimeMillis()
            )
        }
    }
}