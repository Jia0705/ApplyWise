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

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authService: AuthService,
    private val userRepo: UserRepo
): ViewModel() {
    fun loginWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val validationError = validateCredentials(email, password)
        if (validationError != null) {
            onError(validationError)
            return
        }
        viewModelScope.launch {
            try {
                authService.loginWithEmail(email, password)
                val user = authService.getCurrentUser()
                if (user != null) {
                    userRepo.createUser(
                        User(
                            uid = user.uid,
                            email = user.email.ifBlank { email }
                        )
                    )
                }
                onSuccess()
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                onError("Invalid email or password")
            } catch (e: FirebaseAuthInvalidUserException) {
                onError("Invalid email or password")
            } catch (e: FirebaseNetworkException) {
                onError("Network error. Check your connection")
            } catch (e: Exception) {
                onError("Something went wrong. Please try again")
            }
        }
    }

    fun loginWithGoogle(
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val signIn = authService.signInWithGoogle(context)
                if (signIn) {
                    val user = authService.getCurrentUser()
                    if (user != null) {
                        userRepo.createUser(
                            User(
                                uid = user.uid,
                                email = user.email
                            )
                        )
                    }
                    onSuccess()
                } else {
                    onError("Google Sign In was cancelled")
                }
            } catch (e: FirebaseNetworkException) {
                onError("Network error. Check your connection")
            } catch (e: Exception) {
                onError("Google Sign In failed")
            }
        }
    }

    private fun validateCredentials(email: String, password: String): String? {
        if (email.isBlank()) return "Email is required"
        if (!Utils.isValidEmail(email)) return "Invalid email format"
        if (password.isBlank()) return "Password is required"
        if (password.length < 6) return "Password must be at least 6 characters"
        return null
    }
}