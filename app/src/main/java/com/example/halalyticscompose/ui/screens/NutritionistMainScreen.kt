package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.halalyticscompose.R

// Tabs for nutritionist bottom navigation
sealed class NutritionistTab(val route: String, val labelRes: Int, val icon: @Composable () -> Unit) {
    object Dashboard : NutritionistTab("nutri_tab_home", R.string.bottom_nav_nutritionist_dashboard, { Icon(Icons.Filled.Dashboard, null) })
    object Patients : NutritionistTab("nutri_tab_patients", R.string.bottom_nav_nutritionist_patients, { Icon(Icons.Filled.People, null) })
    object AIReview : NutritionistTab("nutri_tab_verify", R.string.bottom_nav_nutritionist_verify, { Icon(Icons.Filled.Science, null) })
    object Education : NutritionistTab("nutri_tab_edu", R.string.bottom_nav_nutritionist_education, { Icon(Icons.AutoMirrored.Filled.MenuBook, null) })
    object Profile : NutritionistTab("nutri_tab_profile", R.string.bottom_nav_nutritionist_profile, { Icon(Icons.Filled.Person, null) })
}

@Composable
fun NutritionistMainScreen(navController: NavController) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val tabs = listOf(
        NutritionistTab.Dashboard,
        NutritionistTab.Patients,
        NutritionistTab.AIReview,
        NutritionistTab.Education,
        NutritionistTab.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            bottomNavController.navigate(tab.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = tab.icon,
                        label = { Text(stringResource(tab.labelRes)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = NutritionistTab.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(NutritionistTab.Dashboard.route) {
                NutritionistHomeScreen(navController = navController)
            }
            composable(NutritionistTab.Patients.route) {
                PatientListScreen(
                    navController = navController,
                    onPatientClick = { userId ->
                        navController.navigate("patient_detail/$userId")
                    }
                )
            }
            composable(NutritionistTab.AIReview.route) {
                AIReviewScreen(navController = navController)
            }
            composable(NutritionistTab.Education.route) {
                ArticleEditorScreen(navController = navController)
            }
            composable(NutritionistTab.Profile.route) {
                ProfileScreen(navController = navController)
            }
        }
    }
}
