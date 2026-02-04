package com.team.applywise.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Screen - Defines all screens in our app for type-safe navigation
 * @Serializable allows passing data between screens safely
 * 
 * Screens without parameters (object): Splash, Login, Register, Dashboard, Profile
 * Screens with parameters (data class): ApplicationList, ApplicationDetail, EditApplication, Timeline
 */
@Serializable
sealed class Screen {
    @Serializable object Splash : Screen() // First screen - checks if user logged in
    @Serializable object Login : Screen() // Login screen
    @Serializable object Register : Screen() // Registration screen
    @Serializable object Dashboard : Screen() // Home screen with stats
    @Serializable data class ApplicationList(val filter: String? = null) : Screen() // List of all applications (optional filter)
    @Serializable data class ApplicationDetail(val applicationId: String) : Screen() // Detail of one application
    @Serializable object AddApplication : Screen() // Add new application
    @Serializable data class EditApplication(val applicationId: String) : Screen() // Edit existing application
    @Serializable data class Timeline(val applicationId: String) : Screen() // Timeline showing status changes
    @Serializable object Profile : Screen() // User profile screen
    @Serializable object EditProfile : Screen() // Edit user profile
}