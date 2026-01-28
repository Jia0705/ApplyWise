package com.team.applywise.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
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

@Composable
fun AppNav(
    navController: NavHostController,
    pendingApplicationId: String? = null
) {
    val navigateToApplicationList: () -> Unit = {
        navController.navigate(Screen.ApplicationList()) {
            popUpTo<Screen.ApplicationDetail> { inclusive = true }
            launchSingleTop = true
        }
    }
    val bottomNavItems = remember {
        listOf(
            BottomNavItem("Dashboard", Screen.Dashboard, Icons.Default.Dashboard),
            BottomNavItem("Applications", Screen.ApplicationList(), Icons.Default.Work),
            BottomNavItem("Profile", Screen.Profile, Icons.Default.AccountCircle)
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val bottomNavRoutes = listOfNotNull(
        Screen.Dashboard::class.qualifiedName,
        Screen.ApplicationList::class.qualifiedName,
        Screen.Profile::class.qualifiedName
    )
    val showBottomBar = bottomNavRoutes.any { routeKey ->
        currentRoute?.startsWith(routeKey) == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    items = bottomNavItems
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash
            ) {
                composable<Screen.Splash> {
                    SplashScreen(
                        navController = navController,
                        pendingApplicationId = pendingApplicationId
                    )
                }

                composable<Screen.Login> {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(Screen.Dashboard) {
                                popUpTo<Screen.Login> { inclusive = true }
                            }
                        },
                        onNavigateToRegister = {
                            navController.navigate(Screen.Register)
                        }
                    )
                }

                composable<Screen.Register> {
                    RegisterScreen(
                        onRegisterSuccess = {
                            navController.navigate(Screen.Dashboard) {
                                popUpTo<Screen.Register> { inclusive = true }
                            }
                        },
                        onNavigateToLogin = {
                            navController.popBackStack()
                        }
                    )
                }

                composable<Screen.Dashboard> {
                    DashboardScreen(
                        onNavigateToApplicationList = { filter ->
                            navController.navigate(Screen.ApplicationList(filter))
                        },
                        onNavigateToAddApplication = {
                            navController.navigate(Screen.AddApplication)
                        },
                        onNavigateToApplicationDetail = { applicationId ->
                            navController.navigate(Screen.ApplicationDetail(applicationId))
                        }
                    )
                }

                composable<Screen.ApplicationList> { backStackEntry ->
                    val args = backStackEntry.toRoute<Screen.ApplicationList>()
                    ApplicationListScreen(
                        onNavigateToDetail = { applicationId ->
                            navController.navigate(Screen.ApplicationDetail(applicationId))
                        },
                        onNavigateToAdd = {
                            navController.navigate(Screen.AddApplication)
                        },
                        onNavigateBack = {
                            navController.navigate(Screen.Dashboard) {
                                popUpTo<Screen.Dashboard> { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        initialFilter = args.filter
                    )
                }

                composable<Screen.ApplicationDetail> { backStackEntry ->
                    val args = backStackEntry.toRoute<Screen.ApplicationDetail>()
                    ApplicationDetailScreen(
                        applicationId = args.applicationId,
                        onNavigateBack = {
                            navigateToApplicationList()
                        },
                        onNavigateToEdit = { id ->
                            navController.navigate(Screen.EditApplication(id))
                        },
                        onNavigateToTimeline = { id ->
                            navController.navigate(Screen.Timeline(id))
                        },
                        onApplicationDeleted = {
                            navigateToApplicationList()
                        }
                    )
                }

                composable<Screen.AddApplication> {
                    AddApplicationScreen(
                        navController = navController,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onApplicationAdded = {
                            navController.popBackStack()
                        }
                    )
                }

                composable<Screen.EditApplication> { backStackEntry ->
                    val args = backStackEntry.toRoute<Screen.EditApplication>()
                    EditApplicationScreen(
                        navController = navController,
                        applicationId = args.applicationId,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onApplicationUpdated = {
                            navController.popBackStack()
                        }
                    )
                }

                composable<Screen.Timeline> { backStackEntry ->
                    val args = backStackEntry.toRoute<Screen.Timeline>()
                    TimelineScreen(
                        applicationId = args.applicationId,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable<Screen.Profile> { backStackEntry ->
                    ProfileScreen(
                        onEditProfile = {
                            navController.navigate(Screen.EditProfile)
                        },
                        showUpdateMessage = backStackEntry.savedStateHandle.get<Boolean>("profile_updated") == true,
                        onMessageShown = {
                            backStackEntry.savedStateHandle.set("profile_updated", false)
                        },
                        onLogout = {
                            navController.navigate(Screen.Login) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable<Screen.EditProfile> {
                    EditProfileScreen(
                        navController = navController,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onSaveSuccess = {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("profile_updated", true)
                            navController.popBackStack()
                        }
                    )
                }
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
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        items.forEach { item ->
            val itemRoute = item.screen::class.qualifiedName
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentDestination?.hierarchy?.any { it.route == itemRoute } == true,
                onClick = {
                    navController.navigate(item.screen) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
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