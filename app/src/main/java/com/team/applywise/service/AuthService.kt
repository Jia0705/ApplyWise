package com.team.applywise.service

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.team.applywise.core.constants.Constants
import com.team.applywise.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AuthService - Handles all user authentication (login, register, logout)
 * This service manages Firebase Authentication and keeps track of current user
 */
@Singleton
class AuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    // Store current user information that can be observed by the app
    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    // When service starts, check if user is already logged in
    init {
        firebaseAuth.currentUser?.let {updateUser(it)}
    }

    /**
     * Update our local user object from Firebase user
     * Keeps avatar color from previous state (Firebase doesn't store this)
     */
    private fun updateUser(firebaseUser: FirebaseUser) {
        val existingAvatarColor = _user.value?.avatarColor ?: ""
        _user.update {
            User(
                uid = firebaseUser.uid,
                name = firebaseUser.displayName ?: "Unknown",
                email = firebaseUser.email ?: "",
                photoURL = firebaseUser.photoUrl?.toString() ?: "",
                avatarColor = existingAvatarColor // Keep user's chosen avatar color
            )
        }
    }

    /**
     * Google Sign-In Process:
     * 1. Show Google account picker
     * 2. Get ID token from selected account
     * 3. Use token to sign in with Firebase
     * 4. Update our local user data
     */
    suspend fun signInWithGoogle(context: Context): Boolean {
        val token = getGoogleCredentialToken(context) ?: return false // Get Google token
        val credential = GoogleAuthProvider.getCredential(token, null) // Create Firebase credential
        val result = firebaseAuth.signInWithCredential(credential).await() // Sign in with Firebase
        result.user?.let { updateUser(it) } // Update our user data
        return result.user != null // Return true if successful
    }

    /**
     * Email Registration Process:
     * 1. Create new Firebase account with email/password
     * 2. Set the user's display name
     * 3. Update our local user data
     */
    suspend fun registerWithEmail(name: String, email: String, password: String) {
        // Create account in Firebase
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()

        // Set the user's name (Firebase stores this)
        val firebaseUser = firebaseAuth.currentUser
        firebaseUser?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build())?.await()
        firebaseUser?.let { updateUser(it) }
    }

    /**
     * Email Login - Simple email and password authentication
     */
    suspend fun loginWithEmail(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
        firebaseAuth.currentUser?.let { updateUser(it) }
    }

    /**
     * Update user's display name in Firebase and locally
     */
    suspend fun updateDisplayName(name: String) {
        val firebaseUser = firebaseAuth.currentUser ?: return
        firebaseUser.updateProfile(
            UserProfileChangeRequest.Builder().setDisplayName(name).build()
        ).await()
        updateUser(firebaseUser)
    }

    /**
     * Update avatar color (stored locally, not in Firebase)
     */
    fun updateAvatarColor(colorName: String) {
        _user.update { current ->
            (current ?: User()).copy(avatarColor = colorName)
        }
    }

    /**
     * Sign out - Clear Firebase session and local user data
     */
    fun signOut() {
        firebaseAuth.signOut()
        _user.value = null
    }

    fun getCurrentUser(): User? = _user.value

    /**
     * Get Google ID Token - Complex process to get authentication token from Google
     * This uses Android's Credential Manager API
     */
    private suspend fun getGoogleCredentialToken(context: Context): String? {
        val credentialManager = CredentialManager.create(context)

        // Configure what we want from Google Sign-In
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // Show all Google accounts, not just logged in ones
            .setServerClientId(Constants.GOOGLE_CLIENT_ID) // Our app's Google OAuth client ID
            .build()

        // Build the credential request
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // Try to get the Google ID token
        return try {
            val result = credentialManager.getCredential(context, request)
            // Extract the token from the result
            result.credential.data.getString(
                "com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN"
            )
        } catch (e: GetCredentialCancellationException) {
            // User cancelled the Google Sign-In dialog
            Log.d("AuthService", "Sign In cancelled", e)
            null
        } catch (e: GetCredentialException) {
            // Google Sign-In failed for some reason
            Log.d("AuthService", "Google Sign In failed", e)
            null
        } catch (e: Exception) {
            // Any other unexpected error
            Log.d("AuthService", "Failed to get token", e)
            null
        }
    }
}