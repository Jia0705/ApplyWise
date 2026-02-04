package com.team.applywise.data.repo

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.team.applywise.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for User data in Firestore
 * Handles saving and loading user profiles
 * Firebase Auth stores email/password, this stores everything else (name, avatar color, etc.)
 */
@Singleton
class UserRepo @Inject constructor(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // Reference to "users" collection in Firestore
    private val usersCollection = firestore.collection("users")

    /**
     * Create a new user document in Firestore
     * Only creates if user doesn't exist already (prevents overwriting)
     */
    suspend fun createUser(user: User) {
        val docRef = usersCollection.document(user.uid)
        val snapshot = docRef.get().await()

        // Check if user already exists
        if (!snapshot.exists()) {
            // User doesn't exist, create them
            docRef.set(user).await()
        }
    }

    /**
     * Get user data from Firestore
     * Returns null if user not found
     */
    suspend fun getUser(uid: String): User? {
        val snapshot = usersCollection.document(uid).get().await()
        return snapshot.toObject(User::class.java)
    }

    /**
     * Update user profile (name and avatar color)
     * SetOptions.merge() = only update these fields, keep other fields unchanged
     */
    suspend fun updateUserProfile(uid: String, name: String, avatarColor: String) {
        val updates = mapOf(
            "name" to name,
            "avatarColor" to avatarColor
        )
        usersCollection.document(uid).set(updates, SetOptions.merge()).await()
    }
}