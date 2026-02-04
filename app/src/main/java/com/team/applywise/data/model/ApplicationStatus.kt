package com.team.applywise.data.model

/**
 * ApplicationStatus - All possible states of a job application
 * displayName is what the user sees on screen
 * name (from enum) is what we save to Firestore
 */
enum class ApplicationStatus(val displayName: String) {
    APPLIED("Applied"), // Just submitted application
    INTERVIEW_SCHEDULED("Interview Scheduled"), // Interview date is set
    INTERVIEW_COMPLETED("Interview Completed"), // Interview done, waiting for decision
    OFFER_RECEIVED("Offer Received"), // Got job offer!
    REJECTED("Rejected"), // Didn't get the job
    NO_RESPONSE("No Response"); // Company never replied

    companion object {
        /**
         * Convert string (from Firestore) to enum
         * If string doesn't match any status, default to APPLIED
         */
        fun fromString(value: String): ApplicationStatus {
            return entries.find { it.name == value } ?: APPLIED
        }
    }
}