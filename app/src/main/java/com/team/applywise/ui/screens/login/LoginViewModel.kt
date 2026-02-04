package com.team.applywise.ui.screens.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.team.applywise.core.utils.Utils
import com.team.applywise.data.model.User
import com.team.applywise.data.repo.UserRepo
import com.team.applywise.service.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * LoginViewModel - Handles login screen business logic
 * Manages email/password and Google Sign-In authentication
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authService: AuthService, // For Firebase authentication
    private val userRepo: UserRepo // For storing user data in Firestore
): ViewModel() {
    
    /**
     * Email/Password Login Process:
     * 1. Validate email and password format
     * 2. Try to sign in with Firebase
     * 3. Create/update user in Firestore database
     * 4. Call onSuccess if everything works, onError if something fails
     */
    fun loginWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit, // Called when login succeeds
        onError: (String) -> Unit // Called with error message if login fails
    ) {
        // First, check if email and password are valid
        val validationError = Utils.validateLoginCredentials(email, password)
        if (validationError != null) {
            onError(validationError) // Show validation error to user
            return
        }
        
        // Run login in background thread (viewModelScope)
        viewModelScope.launch {
            try {
                // Try to log in with Firebase
                authService.loginWithEmail(email, password)
                
                // If successful, save/update user in Firestore
                val user = authService.getCurrentUser()
                if (user != null) {
                    userRepo.createUser(
                        User(
                            uid = user.uid,
                            name = user.name,
                            email = user.email.ifBlank { email }
                        )
                    )
                }
                onSuccess() // Navigate to dashboard
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                // Wrong password or malformed email
                onError("Invalid email or password")
            } catch (e: FirebaseAuthInvalidUserException) {
                // User doesn't exist
                onError("Invalid email or password")
            } catch (e: FirebaseNetworkException) {
                // No internet connection
                onError("Network error. Check your connection")
            } catch (e: Exception) {
                // Any other unexpected error
                onError("Something went wrong. Please try again")
            }
        }
    }

    /**
     * Google Sign-In Process:
     * 1. Show Google account picker
     * 2. Sign in with selected account
     * 3. Create/update user in Firestore
     * 4. Navigate to dashboard or show error
     */
    fun loginWithGoogle(
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Try to sign in with Google
                val signIn = authService.signInWithGoogle(context)
                if (signIn) {
                    // If successful, save/update user in Firestore
                    val user = authService.getCurrentUser()
                    if (user != null) {
                        userRepo.createUser(
                            User(
                                uid = user.uid,
                                name = user.name,
                                email = user.email
                            )
                        )
                    }
                    onSuccess() // Navigate to dashboard
                } else {
                    // User closed Google Sign-In dialog
                    onError("Google Sign In was cancelled")
                }
            } catch (e: FirebaseNetworkException) {
                onError("Network error. Check your connection")
            } catch (e: Exception) {
                onError("Google Sign In failed")
            }
        }
    }
}