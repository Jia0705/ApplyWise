package com.team.applywise.data.model

enum class ApplicationStatus(val displayName: String) {
    APPLIED("Applied"),
    INTERVIEW_SCHEDULED("Interview Scheduled"),
    INTERVIEW_COMPLETED("Interview Completed"),
    OFFER_RECEIVED("Offer Received"),
    REJECTED("Rejected"),
    NO_RESPONSE("No Response");

    companion object {
        fun fromString(value: String): ApplicationStatus {
            return entries.find { it.name == value } ?: APPLIED
        }
    }
}