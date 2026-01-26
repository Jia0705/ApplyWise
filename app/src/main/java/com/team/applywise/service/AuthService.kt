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

@Singleton
class AuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    init {
        firebaseAuth.currentUser?.let {updateUser(it)}
    }

    private fun updateUser(firebaseUser: FirebaseUser) {
        val existingAvatarColor = _user.value?.avatarColor ?: ""
        _user.update {
            User(
                uid = firebaseUser.uid,
                name = firebaseUser.displayName ?: "Unknown",
                email = firebaseUser.email ?: "",
                photoURL = firebaseUser.photoUrl?.toString() ?: "",
                avatarColor = existingAvatarColor
            )
        }
    }

    // Google Login
    suspend fun signInWithGoogle(context: Context): Boolean {
        val token = getGoogleCredentialToken(context) ?: return false
        val credential = GoogleAuthProvider.getCredential(token, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        result.user?.let { updateUser(it) }
        return result.user != null
    }

    // Email Authentication
    suspend fun registerWithEmail(name: String, email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()

        val firebaseUser = firebaseAuth.currentUser
        firebaseUser?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build())?.await()
        firebaseUser?.let { updateUser(it) }
    }

    suspend fun loginWithEmail(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
        firebaseAuth.currentUser?.let { updateUser(it) }
    }

    suspend fun updateDisplayName(name: String) {
        val firebaseUser = firebaseAuth.currentUser ?: return
        firebaseUser.updateProfile(
            UserProfileChangeRequest.Builder().setDisplayName(name).build()
        ).await()
        updateUser(firebaseUser)
    }

    fun updateAvatarColor(colorName: String) {
        _user.update { current ->
            (current ?: User()).copy(avatarColor = colorName)
        }
    }

    // Sign out
    fun signOut() {
        firebaseAuth.signOut()
        _user.value = null
    }

    fun getCurrentUser(): User? = _user.value

    // Token
    private suspend fun getGoogleCredentialToken(context: Context): String? {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(Constants.GOOGLE_CLIENT_ID)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context, request)
            result.credential.data.getString(
                "com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN"
            )
        } catch (e: GetCredentialCancellationException) {
            Log.d("AuthService", "Sign In cancelled", e)
            null
        } catch (e: GetCredentialException) {
            Log.d("AuthService", "Google Sign In failed", e)
            null
        } catch (e: Exception) {
            Log.d("AuthService", "Failed to get token", e)
            null
        }
    }
}