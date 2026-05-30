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
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.ui.res.painterResource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import com.example.halalyticscompose.ui.theme.*

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val userData by viewModel.userData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    Scaffold(
        containerColor = Slate50,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(bottomStart = 50.dp, bottomEnd = 50.dp))
                    .background(Brush.verticalGradient(listOf(Emerald, TealDark)))
            ) {
                // Decorative Circle
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .offset(x = (-100).dp, y = (-50).dp)
                        .background(Gold.copy(alpha = 0.1f), CircleShape)
                )

                Column(
                    modifier = Modifier.fillMaxSize().statusBarsPadding().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back), tint = Color.White)
                        }
                        Text(stringResource(R.string.profile_my_profile_label), color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(Icons.Default.Settings, stringResource(R.string.settings_title), tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Profile Image with Gold Border
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .border(3.dp, Gold, CircleShape)
                            .padding(4.dp)
                            .border(2.dp, Color.White, CircleShape)
                            .clip(CircleShape)
                    ) {
                        val imageUrl = ImageUtils.normalizeUrl(userData?.image)
                        if (imageUrl != null) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = stringResource(R.string.account_profile_image),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.logo_halalytics_official),
                                contentDescription = stringResource(R.string.account_profile_image),
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = userData?.fullName ?: stringResource(R.string.home_default_user),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = userData?.email ?: "",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Stats Section (Jewelry Store Style)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileStatCard(stringResource(R.string.profile_total_scans_summary), "${userData?.totalScans ?: 0}", Emerald, Modifier.weight(1f))
                ProfileStatCard(stringResource(R.string.profile_halal_score), "98%", Gold, Modifier.weight(1f))
                ProfileStatCard(stringResource(R.string.profile_health_rank), "Elite", Color(0xFF8B5CF6), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Menu
            ProfileMenuItem(stringResource(R.string.profile_edit_profile_label), stringResource(R.string.profile_edit_profile_desc), Icons.Default.Edit, Gold) { navController.navigate("edit_profile") }
            ProfileMenuItem(stringResource(R.string.profile_family_account), stringResource(R.string.profile_family_account_desc), Icons.Default.FamilyRestroom, Color(0xFF3B82F6)) { navController.navigate("family_box") }
            ProfileMenuItem(stringResource(R.string.profile_favorite_products), stringResource(R.string.profile_favorite_products_desc), Icons.Default.Favorite, Color(0xFFF43F5E)) { navController.navigate("favorites") }
            ProfileMenuItem(stringResource(R.string.profile_scan_history), stringResource(R.string.profile_scan_history_desc), Icons.Default.History, Color(0xFFF59E0B)) { navController.navigate("history") }
            ProfileMenuItem(stringResource(R.string.profile_help_cs), stringResource(R.string.profile_help_cs_desc), Icons.Default.SupportAgent, Color(0xFF10B981)) { navController.navigate("help_center") }

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button
            TextButton(
                onClick = {
                    viewModel.logout(onComplete = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    })
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Error)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.profile_logout_confirm), fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ProfileStatCard(label: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Black, fontSize = 20.sp, color = color)
            Text(label, fontSize = 11.sp, color = Slate500, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ProfileMenuItem(title: String, subtitle: String, icon: ImageVector, iconColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
            Text(subtitle, fontSize = 12.sp, color = Slate500)
        }
        Icon(Icons.Default.ChevronRight, null, tint = Slate300)
    }
}