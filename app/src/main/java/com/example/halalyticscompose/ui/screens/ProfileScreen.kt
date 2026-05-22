package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.foundation.border
import com.example.halalyticscompose.ui.theme.*

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
                PremiumProfileHeader(
                    userData = userData,
                    totalScans = totalScans,
                    halalProducts = halalProducts,
                    currentStreak = currentStreak
                )
            }

            item {
                PremiumHealthProfileCard(userData = userData)
            }

            item {
                PremiumMenuSection(
                    title = stringResource(R.string.section_activity_contribution),
                    items = listOf(
                        MenuItem(stringResource(R.string.bottom_nav_history), Icons.Default.History, { navController.navigate("history") }),
                        MenuItem(stringResource(R.string.feature_my_contribution), Icons.Default.CloudUpload, { navController.navigate("contribution") }, badge = pendingContributionCount.takeIf { it > 0 }?.toString()),
                        MenuItem(stringResource(R.string.feature_favorite_list), Icons.Default.Favorite, { navController.navigate("favorites") })
                    )
                )
            }

            item {
                PremiumMenuSection(
                    title = stringResource(R.string.profile_settings),
                    items = listOf(
                        MenuItem(stringResource(R.string.notifications), Icons.Default.Notifications, { navController.navigate("notifications") }, badge = unreadNotificationCount.takeIf { it > 0 }?.toString()),
                        MenuItem(stringResource(R.string.dark_mode), Icons.Default.DarkMode, { mainViewModel.toggleDarkMode() }, isSwitch = true, switchState = isDarkMode),
                        MenuItem(stringResource(R.string.settings_title), Icons.Default.Settings, { navController.navigate("settings") })
                    )
                )
            }

            item {
                PremiumMenuSection(
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
fun PremiumProfileHeader(
    userData: com.example.halalyticscompose.data.model.User?,
    totalScans: Int,
    halalProducts: Int,
    currentStreak: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Emerald, TealDark)
                )
            )
            .statusBarsPadding()
            .padding(top = 32.dp, bottom = 40.dp, start = 24.dp, end = 24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .border(4.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    .padding(4.dp)
                    .shadow(24.dp, CircleShape)
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
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = userData?.fullName ?: userData?.username ?: stringResource(R.string.account_default_user),
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = userData?.address ?: "Batam, Indonesia", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(stringResource(R.string.profile_total_scan_label), totalScans.toString())
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.3f)))
                StatItem(stringResource(R.string.profile_halal_stats), halalProducts.toString())
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.3f)))
                StatItem(stringResource(R.string.streak), "$currentStreak hari")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
        Text(text = label.uppercase(), color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PremiumHealthProfileCard(userData: com.example.halalyticscompose.data.model.User?) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(text = "Data Kesehatan", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Emerald, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                HealthInfoItem("Umur", "${userData?.age ?: "-"} th", Icons.Default.Cake)
                HealthInfoItem("Tinggi", "${userData?.height ?: "-"} cm", Icons.Default.Height)
                HealthInfoItem("Berat", "${userData?.weight ?: "-"} kg", Icons.Default.Scale)
                HealthInfoItem("BMI", String.format("%.1f", userData?.bmi ?: 0.0), Icons.Default.Calculate)
            }
            if (userData?.bloodType != null || userData?.allergy != null) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Slate200)
                Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Gol. Darah", fontSize = 12.sp, color = Slate500)
                        Text(userData?.bloodType ?: "-", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Error)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Alergi", fontSize = 12.sp, color = Slate500)
                        Text(userData?.allogy ?: "Tidak ada", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Teal)
                    }
                }
            }
        }
    }
}

@Composable
fun HealthInfoItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(EmeraldLight.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Emerald, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
        Text(text = label, fontSize = 10.sp, color = Slate500)
    }
}

@Composable
fun PremiumMenuSection(title: String, items: List<MenuItem>) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Emerald, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { item.onClick() }.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(16.dp)).background(item.color?.copy(alpha = 0.1f) ?: EmeraldLight.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            Icon(item.icon, contentDescription = null, tint = item.color ?: Emerald, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = item.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, color = Slate900)
                        if (item.badge != null) {
                            Badge(containerColor = Error) { Text(item.badge, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        if (item.isSwitch) {
                            Switch(checked = item.switchState, onCheckedChange = { item.onClick() }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Emerald))
                        } else {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Slate400)
                        }
                    }
                    if (index < items.size - 1) HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Slate100)
                }
            }
        }
    }
}

data class MenuItem(val title: String, val icon: ImageVector, val onClick: () -> Unit, val isSwitch: Boolean = false, val switchState: Boolean = false, val badge: String? = null, val color: Color? = null)

@Composable
fun ProfileBanner() {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).shadow(8.dp, RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp)) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(EmeraldLight, TealLight))).padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Emerald), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Celebration, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Jadi Kontributor", fontWeight = FontWeight.Bold, color = TealDark)
                    Text("Bagikan produk halal & dapatkan poin", fontSize = 12.sp, color = Slate600)
                }
            }
        }
    }
}