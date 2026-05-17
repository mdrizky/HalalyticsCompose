package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.halalyticscompose.R
import com.example.halalyticscompose.ui.viewmodel.*
import com.example.halalyticscompose.utils.ImageUtils


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
    historyViewModel: HistoryViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    contributionViewModel: ContributionViewModel = hiltViewModel(),
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    val userData by authViewModel.userData.collectAsState()
    val totalScans by historyViewModel.totalScans.collectAsState()
    val halalProducts by historyViewModel.halalProducts.collectAsState()
    val currentStreak by historyViewModel.currentStreak.collectAsState()
    val isDarkMode by mainViewModel.isDarkMode.collectAsState()
    val unreadNotificationCount by notificationViewModel.unreadCount.collectAsState()
    val pendingContributionCount by contributionViewModel.pendingCount.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.loadUserProfile()
        historyViewModel.refreshAll()
        notificationViewModel.loadNotifications()
        contributionViewModel.loadContributionStats()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                ProfileHeader(
                    userData = userData,
                    totalScans = totalScans,
                    halalProducts = halalProducts,
                    currentStreak = currentStreak
                )
            }

            item {
                HealthProfileCard(userData = userData)
            }

            item {
                MenuSection(
                    title = stringResource(R.string.section_activity_contribution),
                    items = listOf(
                        MenuItem(stringResource(R.string.bottom_nav_history), Icons.Default.History, { navController.navigate("history") }),
                        MenuItem(stringResource(R.string.feature_my_contribution), Icons.Default.CloudUpload, { navController.navigate("contribution") }, badge = pendingContributionCount.takeIf { it > 0 }?.toString()),
                        MenuItem(stringResource(R.string.feature_favorite_list), Icons.Default.Favorite, { navController.navigate("favorites") })
                    )
                )
            }

            item {
                MenuSection(
                    title = stringResource(R.string.profile_settings),
                    items = listOf(
                        MenuItem(stringResource(R.string.notifications), Icons.Default.Notifications, { navController.navigate("notifications") }, badge = unreadNotificationCount.takeIf { it > 0 }?.toString()),
                        MenuItem(stringResource(R.string.dark_mode), Icons.Default.DarkMode, { mainViewModel.toggleDarkMode() }, isSwitch = true, switchState = isDarkMode),
                        MenuItem(stringResource(R.string.settings_title), Icons.Default.Settings, { navController.navigate("settings") })
                    )
                )
            }

            item {
                MenuSection(
                    title = stringResource(R.string.section_account),
                    items = listOf(
                        MenuItem(stringResource(R.string.feature_edit_profile), Icons.Default.Edit, { navController.navigate("edit_profile") }),
                        MenuItem(stringResource(R.string.feature_logout_label), Icons.AutoMirrored.Filled.Logout, { 
                            authViewModel.logout {
                                navController.navigate("login") { popUpTo(0) { inclusive = true } }
                            }
                        }, color = MaterialTheme.colorScheme.error)
                    )
                )
            }

            item {
                ProfileBanner()
            }
        }
    }
}

@Composable
fun ProfileHeader(
    userData: com.example.halalyticscompose.data.model.User?,
    totalScans: Int,
    halalProducts: Int,
    currentStreak: Int
) {
    val headerColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(headerColor, headerColor.copy(alpha = 0.85f))
                )
            )
            .statusBarsPadding()
            .padding(top = 32.dp, bottom = 48.dp, start = 24.dp, end = 24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                val profileImageUrl = ImageUtils.normalizeUrl(userData?.image)
                if (profileImageUrl != null) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = userData?.fullName ?: userData?.username ?: stringResource(R.string.account_default_user),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = userData?.address ?: "Batam, Indonesia",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
            
            if (!userData?.bio.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = userData?.bio ?: "",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem("Total Scan", totalScans.toString())
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.25f)))
                StatItem("Halal Stats", halalProducts.toString())
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.25f)))
                StatItem("Streak", "$currentStreak hari")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = label.uppercase(),
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun HealthProfileCard(userData: com.example.halalyticscompose.data.model.User?) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Health Profile",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HealthInfoItem("Age", "${userData?.age ?: "-"} yrs", Icons.Default.Cake)
                HealthInfoItem("Height", "${userData?.height ?: "-"} cm", Icons.Default.Height)
                HealthInfoItem("Weight", "${userData?.weight ?: "-"} kg", Icons.Default.Scale)
                HealthInfoItem("BMI", String.format("%.1f", userData?.bmi ?: 0.0), Icons.Default.Calculate)
            }
            
            if (userData?.bloodType != null || userData?.allergy != null) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Blood Type", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(userData?.bloodType ?: "-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Allergy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(userData?.allergy ?: "None", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun HealthInfoItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MenuSection(title: String, items: List<MenuItem>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { item.onClick() }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(item.color?.copy(alpha = 0.1f) ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icon, contentDescription = null, tint = item.color ?: MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(item.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        
                        if (item.badge != null) {
                            Badge(containerColor = MaterialTheme.colorScheme.error) { Text(item.badge) }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        if (item.isSwitch) {
                            Switch(checked = item.switchState, onCheckedChange = { item.onClick() })
                        } else {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
                        }
                    }
                    if (index < items.size - 1) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

data class MenuItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val isSwitch: Boolean = false,
    val switchState: Boolean = false,
    val badge: String? = null,
    val color: Color? = null
)
@Composable
fun ProfileBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E7D32)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Celebration, null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Ayo Jadi Kontributor!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                Text(
                    text = "Bantu sesama Muslim menemukan produk Halal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1B5E20).copy(alpha = 0.7f)
                )
            }
        }
    }
}
