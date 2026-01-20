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

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authService: AuthService,
    private val userRepo: UserRepo
) : ViewModel() {
    fun register(
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val validationError = validateCredentials(email, password, confirmPassword)
        if (validationError != null) {
            onError(validationError)
            return
        }

        viewModelScope.launch {
            try {
                authService.registerWithEmail(email, password)
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
            } catch (e: FirebaseAuthWeakPasswordException) {
                onError("Password is too weak")
            } catch (e: FirebaseAuthUserCollisionException) {
                onError("Email already exists")
            } catch (e: FirebaseNetworkException) {
                onError("Network error. Check your connection")
            } catch (e: Exception) {
                onError("Something went wrong. Please try again")
            }
        }
    }

    private fun validateCredentials(email: String, password: String, confirmPassword: String): String? {
        if (email.isBlank()) return "Email is required"
        if (!Utils.isValidEmail(email)) return "Invalid email format"
        if (password.isBlank()) return "Password is required"
        if (password.length < 6) return "Password must be at least 6 characters"
        if (confirmPassword.isBlank()) return "Confirm password is required"
        if (password != confirmPassword) return "Passwords do not match"
        return null
    }
}