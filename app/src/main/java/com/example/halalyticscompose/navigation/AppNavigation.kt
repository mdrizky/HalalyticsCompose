package com.example.halalyticscompose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

/**
 * Role-Based Navigation Architecture.
 * This separates the monolithic NavHost into isolated graphs based on the user's role,
 * preventing permission leaks and improving performance.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    role: String?
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Common routes (Splash, Login, Register, Onboarding, ForgotPassword)
        commonNavGraph(navController)

        // Route switching based on Role
        when (role) {
            "admin" -> adminNavGraph(navController)
            "ahli_gizi", "nutritionist", "expert" -> nutritionistNavGraph(navController)
            else -> userNavGraph(navController) // Default to user
        }
    }
}
