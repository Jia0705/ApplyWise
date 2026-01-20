package com.team.applywise.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable object Splash : Screen()
    @Serializable object Login : Screen()
    @Serializable object Register : Screen()
    @Serializable object Dashboard : Screen()
    @Serializable object ApplicationList : Screen()
    @Serializable data class ApplicationDetail(val applicationId: String) : Screen()
    @Serializable object AddApplication : Screen()
    @Serializable data class EditApplication(val applicationId: String) : Screen()
    @Serializable data class Timeline(val applicationId: String) : Screen()
    @Serializable object Profile : Screen()
}