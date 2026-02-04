package com.team.applywise.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.team.applywise.core.utils.Utils
import com.team.applywise.data.model.User
import com.team.applywise.data.repo.UserRepo
import com.team.applywise.service.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Registration screen
 * This handles creating a new user account with email/password
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authService: AuthService,
    private val userRepo: UserRepo
) : ViewModel() {
    fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Step 1: Check if all fields are valid (name not empty, email format correct, passwords match, etc.)
        val validationError = Utils.validateRegisterCredentials(name, email, password, confirmPassword)
        if (validationError != null) {
            onError(validationError)
            return
        }

        // Step 2: Create the account in Firebase (runs in background)
        viewModelScope.launch {
            try {
                // Register with Firebase Authentication - creates the account
                authService.registerWithEmail(name, email, password)
                
                // Step 3: Get the newly created user info
                val user = authService.getCurrentUser()
                if (user != null) {
                    // Save user details to Firestore database
                    // We save to Firestore because Firebase Auth only stores email/password
                    userRepo.createUser(
                        User(
                            uid = user.uid,
                            name = user.name,
                            email = user.email.ifBlank { email }
                        )
                    )
                }
                // Step 4: Tell the screen everything worked - navigate to dashboard
                onSuccess()
            } catch (e: FirebaseAuthWeakPasswordException) {
                // Password is too simple (Firebase requires at least 6 characters)
                onError("Password is too weak")
            } catch (e: FirebaseAuthUserCollisionException) {
                // Someone already registered with this email
                onError("Email already exists")
            } catch (e: FirebaseNetworkException) {
                // No internet connection
                onError("Network error. Check your connection")
            } catch (e: Exception) {
                // Something else went wrong
                onError("Something went wrong. Please try again")
            }
        }
    }
}