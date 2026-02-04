package com.team.applywise.data.model

/**
 * User - Represents a user in our app
 * uid: Unique ID from Firebase Auth
 * name: User's display name
 * email: User's email address
 * photoURL: Profile picture URL (not currently used)
 * avatarColor: Color for avatar circle (Red, Blue, Green, etc.)
 * createdAt: When account was created
 */
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoURL: String = "",
    val avatarColor: String = "",
    val createdAt: Long = System.currentTimeMillis()
)