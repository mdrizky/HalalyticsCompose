package com.example.halalyticscompose.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.halalyticscompose.ui.screens.*

fun NavGraphBuilder.commonNavGraph(navController: NavController) {
    // These routes are accessible to all roles and unauthenticated users
    composable("login") {
        LoginScreen(navController = navController)
    }
    composable("register") {
        SimpleRegisterScreen(navController = navController)
    }
    composable("forgot_password") {
        ForgotPasswordScreen(navController = navController)
    }
    composable("onboarding") {
        OnboardingScreen(
            navController = navController,
            onFinish = {
                navController.navigate("login") {
                    popUpTo("onboarding") { inclusive = true }
                }
            }
        )
    }
}

fun NavGraphBuilder.adminNavGraph(navController: NavController) {
    // Admin specific routes
    composable("home") {
        AdminPanelScreen(navController = navController)
    }
    composable("admin_users") {
        AdminUsersListScreen(navController = navController)
    }
    composable("admin_system_health") {
        AdminSystemHealthScreen(navController = navController)
    }
}

fun NavGraphBuilder.nutritionistNavGraph(navController: NavController) {
    // Nutritionist specific routes
    composable("nutritionist_home") {
        NutritionistHomeScreen(navController = navController)
    }
    composable("home") {
        NutritionistHomeScreen(navController = navController)
    }
}

fun NavGraphBuilder.userNavGraph(navController: NavController) {
    // Will be populated by migrating the massive MainActivity NavHost block
    // Temporarily, we will rely on MainActivity keeping the main host for users,
    // or we can gradually migrate screens here.
}
