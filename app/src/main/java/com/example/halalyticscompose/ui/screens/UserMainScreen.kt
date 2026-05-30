package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.halalyticscompose.R
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.halalyticscompose.ui.screens.donation.DonationScreen

// Tab untuk bottom navigation user
sealed class UserTab(val route: String, val labelRes: Int, val icon: @Composable () -> Unit) {
    object Home : UserTab("tab_home", R.string.bottom_nav_home, { Icon(Icons.Filled.Home, stringResource(R.string.bottom_nav_home)) })
    object Article : UserTab("tab_article", R.string.bottom_nav_article, { Icon(Icons.AutoMirrored.Filled.Article, stringResource(R.string.bottom_nav_article)) })
    object Donation : UserTab("tab_donation", R.string.bottom_nav_donation, { Icon(Icons.Filled.Favorite, stringResource(R.string.bottom_nav_donation)) })
    object Profile : UserTab("tab_profile", R.string.bottom_nav_profile, { Icon(Icons.Filled.Person, stringResource(R.string.bottom_nav_profile)) })
}

@Composable
fun UserMainScreen(navController: NavController) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val tabs = listOf(
        UserTab.Home,
        UserTab.Article,
        UserTab.Donation,
        UserTab.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    // Sisipkan FAB Scan di tengah (antara index 1 dan 2)
                    if (index == 2) {
                        // Spacer untuk FAB
                        NavigationBarItem(
                            selected = false,
                            onClick = {},
                            icon = {},
                            label = {},
                            enabled = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
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
                        label = { Text(stringResource(tab.labelRes)) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("scan") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = stringResource(R.string.bottom_nav_scan))
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = UserTab.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(UserTab.Home.route) {
                HomeTabContent(outerNavController = navController)
            }
            composable(UserTab.Article.route) {
                ArticleListScreen(
                    onArticleClick = { articleId ->
                        navController.navigate("article_detail/$articleId")
                    },
                    navController = navController
                )
            }
            composable(UserTab.Donation.route) {
                DonationScreen(
                    navController = navController
                )
            }
            composable(UserTab.Profile.route) {
                ProfileScreen(
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun HomeTabContent(outerNavController: NavController) {
    // Konten Home Tab
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        HomeScreen(navController = outerNavController)
    }
}
