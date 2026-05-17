package com.example.halalyticscompose.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

/**
 * Main layout wrapper that includes bottom navigation for main screens
 * with dynamic role-based behavior
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    navController: NavController,
    showBottomNav: Boolean = false,
    isAdmin: Boolean = false,
    isNutritionist: Boolean = false,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(
                    navController = navController,
                    isAdmin = isAdmin,
                    isNutritionist = isNutritionist
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        content(paddingValues)
    }
}
