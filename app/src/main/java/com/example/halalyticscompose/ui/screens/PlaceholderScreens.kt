package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController




import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.ui.graphics.Color

@Composable fun AdminMainScreen(navController: NavController) {
    UnderDevelopmentScreen(
        navController = navController,
        title = "Admin Dashboard",
        featureName = "Admin Control Panel",
        description = "Dashboard admin sedang dalam pengembangan untuk versi mobile.",
        icon = Icons.Default.AdminPanelSettings,
        gradientColors = listOf(Color(0xFF333333), Color(0xFF555555)),
        plannedFeatures = listOf(
            PlannedFeature("Statistik", "Melihat statistik pengguna dan sistem"),
            PlannedFeature("Manajemen", "Kelola konten dan pengguna dari mobile")
        )
    )
}

@Composable fun AdminUserManagementScreen(navController: NavController) {
    UnderDevelopmentScreen(navController = navController, title = "Manajemen User", featureName = "User Management", description = "Fitur manajemen pengguna sedang dikembangkan.", icon = Icons.Default.AdminPanelSettings, gradientColors = listOf(Color(0xFF333333), Color(0xFF555555)), plannedFeatures = emptyList())
}
@Composable fun AdminAIPromptScreen(navController: NavController) {
    UnderDevelopmentScreen(navController = navController, title = "Manajemen Prompt AI", featureName = "AI Prompts", description = "Pengaturan prompt AI sedang dikembangkan.", icon = Icons.Default.AdminPanelSettings, gradientColors = listOf(Color(0xFF333333), Color(0xFF555555)), plannedFeatures = emptyList())
}
@Composable fun AdminHalalRulesScreen(navController: NavController) {
    UnderDevelopmentScreen(navController = navController, title = "Manajemen Aturan Halal", featureName = "Halal Rules", description = "Pengaturan aturan halal sedang dikembangkan.", icon = Icons.Default.AdminPanelSettings, gradientColors = listOf(Color(0xFF333333), Color(0xFF555555)), plannedFeatures = emptyList())
}
@Composable fun AdminBpomScreen(navController: NavController) {
    UnderDevelopmentScreen(navController = navController, title = "Manajemen BPOM", featureName = "BPOM Data", description = "Sinkronisasi BPOM sedang dikembangkan.", icon = Icons.Default.AdminPanelSettings, gradientColors = listOf(Color(0xFF333333), Color(0xFF555555)), plannedFeatures = emptyList())
}
@Composable fun AdminDonationScreen(navController: NavController) {
    UnderDevelopmentScreen(navController = navController, title = "Manajemen Donasi", featureName = "Donations", description = "Manajemen donasi sedang dikembangkan.", icon = Icons.Default.AdminPanelSettings, gradientColors = listOf(Color(0xFF333333), Color(0xFF555555)), plannedFeatures = emptyList())
}
@Composable fun AdminAILogsScreen(navController: NavController) {
    UnderDevelopmentScreen(navController = navController, title = "Log AI", featureName = "AI Logs", description = "Pemantauan log AI sedang dikembangkan.", icon = Icons.Default.AdminPanelSettings, gradientColors = listOf(Color(0xFF333333), Color(0xFF555555)), plannedFeatures = emptyList())
}
@Composable fun AdminArticleScreen(navController: NavController) {
    UnderDevelopmentScreen(navController = navController, title = "Manajemen Artikel", featureName = "Articles", description = "Manajemen artikel sedang dikembangkan.", icon = Icons.Default.AdminPanelSettings, gradientColors = listOf(Color(0xFF333333), Color(0xFF555555)), plannedFeatures = emptyList())
}
@Composable fun ProfileSetupScreen(onSetupComplete: () -> Unit = {}) { Box { Text("Profile Setup (In Development)") } }

