package com.team.applywise.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.team.applywise.ui.screens.application.AddApplicationScreen
import com.team.applywise.ui.screens.application.ApplicationDetailScreen
import com.team.applywise.ui.screens.application.ApplicationListScreen
import com.team.applywise.ui.screens.application.EditApplicationScreen
import com.team.applywise.ui.screens.application.TimelineScreen
import com.team.applywise.ui.screens.dashboard.DashboardScreen
import com.team.applywise.ui.screens.login.LoginScreen
import com.team.applywise.ui.screens.profile.EditProfileScreen
import com.team.applywise.ui.screens.profile.ProfileScreen
import com.team.applywise.ui.screens.register.RegisterScreen
import com.team.applywise.ui.screens.splash.SplashScreen

/**
 * AppNav - Main navigation setup for the entire app
 * 
 * Think of this as a map that tells Android:
 * "When user goes to Screen X, show them Screen X's content"
 * 
 * @param navController - The GPS that tracks where user is
 * @param pendingApplicationId - If user clicked notification, this has the application ID
 */
@Composable
fun AppNav(
    navController: NavHostController,
    pendingApplicationId: String? = null,
    modifier: Modifier = Modifier
) {
    // Helper function: Go to application list and clear detail screen from back stack
    val navigateToApplicationList: () -> Unit = {
        navController.navigate(Screen.ApplicationList()) {
            popUpTo<Screen.ApplicationDetail> { inclusive = true } // Remove detail from history
            launchSingleTop = true // Don't create duplicate if already there
        }
    }
    
    Box(modifier) {
        // NavHost = Container that holds all screens
        // Start at Splash screen (shows logo, checks if logged in)
        NavHost(
            navController = navController,
            startDestination = Screen.Splash
        ) {
            
            // ========================================
            // 1. SPLASH SCREEN (First screen shown)
            // ========================================
            composable<Screen.Splash> {
                SplashScreen(
                    navController = navController,
                    pendingApplicationId = pendingApplicationId
                )
            }

            // ========================================
            // 2. LOGIN SCREEN
            // ========================================
            composable<Screen.Login> {
                LoginScreen(
                    // What happens after successful login:
                    onLoginSuccess = {
                        // Go to Dashboard
                        navController.navigate(Screen.Dashboard) {
                            // Remove Login from back stack (can't press back to login again)
                            popUpTo<Screen.Login> { inclusive = true }
                        }
                    },
                    // What happens when user clicks "Sign Up":
                    onNavigateToRegister = {
                        // Go to Register screen
                        navController.navigate(Screen.Register) {
                            // Don't create duplicate Register screens if clicked multiple times
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ========================================
            // 3. REGISTER SCREEN
            // ========================================
            composable<Screen.Register> {
                RegisterScreen(
                    // What happens after successful registration:
                    onRegisterSuccess = {
                        // Go to Dashboard
                        navController.navigate(Screen.Dashboard) {
                            // Remove Register from back stack
                            popUpTo<Screen.Register> { inclusive = true }
                        }
                    },
                    // What happens when user clicks "Already have account? Login":
                    onNavigateToLogin = {
                        // Go back to Login screen
                        navController.navigate(Screen.Login) {
                            // Clear any old Login screens and create fresh one
                            popUpTo<Screen.Login> { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ========================================
            // 4. DASHBOARD (Home screen with stats)
            // ========================================
            composable<Screen.Dashboard> {
                DashboardScreen(
                    // User clicks on a status card (e.g., "5 Interviews"):
                    onNavigateToApplicationList = { filter ->
                        // Go to list, optionally filtered by status
                        navController.navigate(Screen.ApplicationList(filter))
                    },
                    // User clicks FAB (+ button):
                    onNavigateToAddApplication = {
                        // Go to Add Application form
                        navController.navigate(Screen.AddApplication)
                    },
                    // User clicks on an application card:
                    onNavigateToApplicationDetail = { applicationId ->
                        // Go to detail screen for that application
                        navController.navigate(Screen.ApplicationDetail(applicationId))
                    }
                )
            }

            // ========================================
            // 5. APPLICATION LIST (All applications)
            // ========================================
            composable<Screen.ApplicationList> { backStackEntry ->
                // Get the filter parameter (could be null, "Applied", "Interview", etc.)
                val args = backStackEntry.toRoute<Screen.ApplicationList>()
                
                ApplicationListScreen(
                    // User clicks on an application:
                    onNavigateToDetail = { applicationId ->
                        navController.navigate(Screen.ApplicationDetail(applicationId))
                    },
                    // User clicks FAB (+ button):
                    onNavigateToAdd = {
                        navController.navigate(Screen.AddApplication)
                    },
                    // User clicks back button:
                    onNavigateBack = {
                        // Go back to Dashboard (clear duplicate Dashboards if any)
                        navController.navigate(Screen.Dashboard) {
                            popUpTo<Screen.Dashboard> { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    // Pass the filter (if any) to the screen
                    initialFilter = args.filter
                )
            }

            // ========================================
            // 6. APPLICATION DETAIL (View one application)
            // ========================================
            composable<Screen.ApplicationDetail> { backStackEntry ->
                // Get the application ID from navigation
                val args = backStackEntry.toRoute<Screen.ApplicationDetail>()
                
                ApplicationDetailScreen(
                    applicationId = args.applicationId,
                    // User clicks back button:
                    onNavigateBack = {
                        navigateToApplicationList() // Go to list
                    },
                    // User clicks Edit button:
                    onNavigateToEdit = { id ->
                        navController.navigate(Screen.EditApplication(id))
                    },
                    // User clicks Timeline button:
                    onNavigateToTimeline = { id ->
                        navController.navigate(Screen.Timeline(id))
                    },
                    // User deleted the application:
                    onApplicationDeleted = {
                        navigateToApplicationList() // Go back to list
                    }
                )
            }

            // ========================================
            // 7. ADD APPLICATION (Create new)
            // ========================================
            composable<Screen.AddApplication> {
                AddApplicationScreen(
                    navController = navController,
                    // User clicks back button or X:
                    onNavigateBack = {
                        navController.popBackStack() // Go back to previous screen
                    },
                    // User saved the application:
                    onApplicationAdded = {
                        navController.popBackStack() // Go back to previous screen
                    }
                )
            }

            // ========================================
            // 8. EDIT APPLICATION (Update existing)
            // ========================================
            composable<Screen.EditApplication> { backStackEntry ->
                // Get the application ID to edit
                val args = backStackEntry.toRoute<Screen.EditApplication>()
                
                EditApplicationScreen(
                    navController = navController,
                    applicationId = args.applicationId,
                    // User clicks back button:
                    onNavigateBack = {
                        navController.popBackStack() // Go back to detail screen
                    },
                    // User saved the changes:
                    onApplicationUpdated = {
                        navController.popBackStack() // Go back to detail screen
                    }
                )
            }

            // ========================================
            // 9. TIMELINE (Status history)
            // ========================================
            composable<Screen.Timeline> { backStackEntry ->
                // Get the application ID
                val args = backStackEntry.toRoute<Screen.Timeline>()
                
                TimelineScreen(
                    applicationId = args.applicationId,
                    // User clicks back button:
                    onNavigateBack = {
                        navController.popBackStack() // Go back to detail screen
                    }
                )
            }

            // ========================================
            // 10. PROFILE (User info)
            // ========================================
            composable<Screen.Profile> { backStackEntry ->
                ProfileScreen(
                    // User clicks Edit button:
                    onEditProfile = {
                        navController.navigate(Screen.EditProfile)
                    },
                    // Check if we should show "Profile updated" message:
                    showUpdateMessage = backStackEntry.savedStateHandle.get<Boolean>("profile_updated") == true,
                    // Mark message as shown:
                    onMessageShown = {
                        backStackEntry.savedStateHandle.set("profile_updated", false)
                    },
                    // User clicks Logout:
                    onLogout = {
                        // Go to Login and clear EVERYTHING from back stack
                        navController.navigate(Screen.Login) {
                            popUpTo(0) { inclusive = true } // Clear all screens
                        }
                    }
                )
            }

            // ========================================
            // 11. EDIT PROFILE (Change name/avatar)
            // ========================================
            composable<Screen.EditProfile> {
                EditProfileScreen(
                    navController = navController,
                    // User clicks back button:
                    onNavigateBack = {
                        navController.popBackStack() // Go back to Profile
                    },
                    // User saved changes:
                    onSaveSuccess = {
                        // Tell Profile screen to show "Updated" message
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("profile_updated", true)
                        navController.popBackStack() // Go back to Profile
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    items: List<BottomNavItem>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            val itemRoute = item.screen::class.qualifiedName
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == itemRoute,
                onClick = {
                    navController.navigate(item.screen) {
                        // Clear back stack to start destination (Dashboard)
                        popUpTo(navController.graph.findStartDestination().id)
                        // Don't create duplicate if already on this screen
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val title: String,
    val screen: Screen,
    val icon: ImageVector
)