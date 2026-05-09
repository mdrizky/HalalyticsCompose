package com.example.halalyticscompose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.halalyticscompose.R

private val FabGradientStart = Color(0xFF004D40)
private val FabGradientEnd = Color(0xFF26A69A)

sealed class BottomNavItem(
    val route: String,
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem("home", R.string.home, Icons.Filled.Home, Icons.Outlined.Home)
    object Search : BottomNavItem("manual_input", R.string.bottom_nav_search, Icons.Filled.Search, Icons.Outlined.Search)
    object History : BottomNavItem("history", R.string.bottom_nav_history, Icons.Outlined.History, Icons.Outlined.History)
    object Profile : BottomNavItem("profile", R.string.profile, Icons.Filled.Person, Icons.Outlined.Person)
    
    // Admin Items
    object AdminDashboard : BottomNavItem("home", R.string.admin_panel_title, Icons.Filled.Build, Icons.Outlined.Build)
    object AdminUsers : BottomNavItem("admin_users", R.string.admin_panel_users, Icons.Filled.Person, Icons.Outlined.Person)
    object AdminNotifications : BottomNavItem("admin_notifications_app", R.string.notification_title, Icons.Filled.Notifications, Icons.Outlined.Notifications)
}

@Composable
fun BottomNavBar(
    navController: NavController,
    isAdmin: Boolean = false,
    modifier: Modifier = Modifier
) {
    val items = if (isAdmin) {
        listOf(
            BottomNavItem.AdminDashboard,
            BottomNavItem.AdminUsers,
            BottomNavItem.AdminNotifications,
            BottomNavItem.Profile
        )
    } else {
        listOf(
            BottomNavItem.Home,
            BottomNavItem.Search,
            BottomNavItem.History,
            BottomNavItem.Profile
        )
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val navBg = MaterialTheme.colorScheme.surface
    val navBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(72.dp),
            color = navBg,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, navBorder, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavItem(items[0], currentRoute == items[0].route) {
                        if (currentRoute != items[0].route) navController.navigate(items[0].route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    NavItem(items[1], currentRoute == items[1].route) {
                        if (currentRoute != items[1].route) navController.navigate(items[1].route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }

                    Spacer(modifier = Modifier.size(54.dp))

                    NavItem(items[2], currentRoute == items[2].route) {
                        if (currentRoute != items[2].route) navController.navigate(items[2].route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    NavItem(items[3], currentRoute == items[3].route) {
                        if (currentRoute != items[3].route) navController.navigate(items[3].route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        }

        if (!isAdmin) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(56.dp)
                    .background(
                        brush = Brush.linearGradient(listOf(FabGradientStart, FabGradientEnd)),
                        shape = CircleShape
                    )
                    .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .clickable {
                        if (currentRoute != "scan_hub") {
                            navController.navigate("scan_hub")
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = stringResource(R.string.scan),
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "H",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 10.dp, bottom = 8.dp)
                )
            }
        }

    }
}

@Composable
private fun NavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant
    val title = stringResource(item.titleRes)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
            contentDescription = title,
            tint = if (selected) activeColor else inactiveColor,
            modifier = Modifier.size(21.dp)
        )
        Text(
            text = title,
            color = if (selected) activeColor else inactiveColor,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
