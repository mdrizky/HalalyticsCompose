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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.halalyticscompose.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityHubScreen(navController: NavController) {
    val primaryColor = Color(0xFF00C853) // Halal Green
    val secondaryColor = Color(0xFF00B0FF) // Blue
    val accentColor = Color(0xFFFFB300) // Gold

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Community Hub", 
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(primaryColor, Color(0xFF69F0AE))
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(modifier = Modifier.align(Alignment.CenterStart)) {
                        Text(
                            "Hidup Sehat Bersama",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "Bagikan pengalaman dan temukan resep halal.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                    }
                    Icon(
                        Icons.Default.Groups,
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.CenterEnd)
                            .offset(x = 20.dp),
                        tint = Color.White.copy(alpha = 0.2f)
                    )
                }
            }

            // Main Grid Actions
            item {
                Text(
                    "Fitur Komunitas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CommunityCard(
                        modifier = Modifier.weight(1f),
                        title = "Diskusi",
                        subtitle = "Tanya & Jawab",
                        icon = Icons.AutoMirrored.Filled.Chat,
                        color = Color(0xFFE8F5E9),
                        iconColor = primaryColor,
                        onClick = { navController.navigate("community") }
                    )
                    CommunityCard(
                        modifier = Modifier.weight(1f),
                        title = "Resep",
                        subtitle = "Menu Sehat",
                        icon = Icons.Default.RestaurantMenu,
                        color = Color(0xFFE3F2FD),
                        iconColor = secondaryColor,
                        onClick = { navController.navigate("recipes") }
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CommunityCard(
                        modifier = Modifier.weight(1f),
                        title = "Misi",
                        subtitle = "Dapatkan Poin",
                        icon = Icons.Default.EmojiEvents,
                        color = Color(0xFFFFF8E1),
                        iconColor = accentColor,
                        onClick = { navController.navigate("daily_mission_dashboard") }
                    )
                    CommunityCard(
                        modifier = Modifier.weight(1f),
                        title = "Belanja",
                        subtitle = "Dalam Pengembangan",
                        icon = Icons.Default.ShoppingCart,
                        color = Color(0xFFF5F5F5),
                        iconColor = Color.Gray,
                        isDevelopment = true,
                        onClick = { navController.navigate("grocery_list") }
                    )
                }
            }

            // Featured Content Placeholder
            item {
                Text(
                    "Postingan Populer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.VolunteerActivism,
                            contentDescription = null,
                            tint = primaryColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Segera Hadir: Forum Diskusi Cerdas",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Kami sedang menyiapkan ruang diskusi yang lebih interaktif untuk kamu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.navigate("community") },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Buka Forum Lama")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    iconColor: Color,
    isDevelopment: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(
                subtitle, 
                style = MaterialTheme.typography.labelSmall, 
                color = if (isDevelopment) Color.Red else Color.Gray,
                maxLines = 1
            )
        }
    }
}
