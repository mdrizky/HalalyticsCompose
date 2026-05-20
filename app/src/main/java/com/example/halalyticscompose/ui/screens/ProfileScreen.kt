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
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Emerald, TealDark)
                )
            )
            .statusBarsPadding()
            .padding(top = 24.dp, bottom = 36.dp, start = 24.dp, end = 24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .border(3.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    .padding(3.dp)
                    .shadow(12.dp, CircleShape)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = userData?.fullName ?: userData?.username ?: stringResource(R.string.account_default_user),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    letterSpacing = (-0.5).sp
                )
                if (totalScans > 10) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = "Premium Member",
                        tint = Mint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = userData?.address ?: "Batam, Indonesia",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (!userData?.bio.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = userData?.bio ?: "",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(stringResource(R.string.profile_total_scan_label), totalScans.toString())
                Box(modifier = Modifier.width(1.dp).height(28.dp).background(Color.White.copy(alpha = 0.25f)))
                StatItem(stringResource(R.string.profile_halal_stats), halalProducts.toString())
                Box(modifier = Modifier.width(1.dp).height(28.dp).background(Color.White.copy(alpha = 0.25f)))
                StatItem(stringResource(R.string.streak), stringResource(R.string.profile_streak_value, currentStreak))
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
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = label.uppercase(),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
fun HealthProfileCard(userData: com.example.halalyticscompose.data.model.User?) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = stringResource(R.string.profile_health_profile).uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Emerald,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(HalalyticsShadows.elevation2, RoundedCornerShape(HalalyticsDimensions.radius2XLarge)),
            shape = RoundedCornerShape(HalalyticsDimensions.radius2XLarge),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HealthInfoItem(stringResource(R.string.profile_age), stringResource(R.string.profile_age_value, userData?.age?.toString() ?: "-"), Icons.Default.Cake)
                HealthInfoItem(stringResource(R.string.profile_height), stringResource(R.string.profile_height_value, userData?.height?.toString() ?: "-"), Icons.Default.Height)
                HealthInfoItem(stringResource(R.string.profile_weight), stringResource(R.string.profile_weight_value, userData?.weight?.toString() ?: "-"), Icons.Default.Scale)
                HealthInfoItem(stringResource(R.string.profile_bmi), String.format("%.1f", userData?.bmi ?: 0.0), Icons.Default.Calculate)
            }
            
            if (userData?.bloodType != null || userData?.allergy != null) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Slate200)
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(), 
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(HalalyticsDimensions.radiusLarge))
                            .background(EmeraldLight.copy(alpha = 0.1f))
                            .padding(vertical = 12.dp)
                    ) {
                        Text(stringResource(R.string.profile_blood_type), style = MaterialTheme.typography.labelSmall, color = Slate500, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(userData?.bloodType ?: "-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Error)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(HalalyticsDimensions.radiusLarge))
                            .background(TealLight.copy(alpha = 0.15f))
                            .padding(vertical = 12.dp)
                    ) {
                        Text(stringResource(R.string.profile_allergy), style = MaterialTheme.typography.labelSmall, color = Slate500, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(userData?.allergy ?: stringResource(R.string.profile_allergy_none), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Teal, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
fun HealthInfoItem(label: String, value: String, icon: ImageVector) {
    Card(
        modifier = Modifier.width(76.dp),
        shape = RoundedCornerShape(HalalyticsDimensions.radiusLarge),
        colors = CardDefaults.cardColors(containerColor = EmeraldLight.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp).fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Emerald.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Emerald, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value, 
                style = MaterialTheme.typography.bodyMedium, 
                fontWeight = FontWeight.Black,
                color = Slate900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label, 
                style = MaterialTheme.typography.labelSmall, 
                color = Slate500,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MenuSection(title: String, items: List<MenuItem>) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Emerald,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(HalalyticsShadows.elevation2, RoundedCornerShape(HalalyticsDimensions.radius2XLarge)),
            shape = RoundedCornerShape(HalalyticsDimensions.radius2XLarge),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { item.onClick() }
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(HalalyticsDimensions.radiusMedium))
                                .background(item.color?.copy(alpha = 0.1f) ?: EmeraldLight.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon, 
                                contentDescription = null, 
                                tint = item.color ?: Emerald, 
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = item.title, 
                            modifier = Modifier.weight(1f), 
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate900
                        )
                        
                        if (item.badge != null) {
                            Badge(
                                containerColor = Error,
                                modifier = Modifier.padding(end = 4.dp)
                            ) { 
                                Text(
                                    text = item.badge,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ) 
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        if (item.isSwitch) {
                            Switch(
                                checked = item.switchState, 
                                onCheckedChange = { item.onClick() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Emerald
                                )
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, 
                                contentDescription = null, 
                                tint = Slate400, 
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp), 
                            color = Slate100
                        )
                    }
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
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(HalalyticsDimensions.radius2XLarge),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(EmeraldLight.copy(alpha = 0.3f), TealLight.copy(alpha = 0.4f))
                    )
                )
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Emerald),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Celebration, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.profile_contributor_title),
                        fontWeight = FontWeight.Bold,
                        color = TealDark,
                        fontSize = 15.sp
                    )
                    Text(
                        text = stringResource(R.string.profile_contributor_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
