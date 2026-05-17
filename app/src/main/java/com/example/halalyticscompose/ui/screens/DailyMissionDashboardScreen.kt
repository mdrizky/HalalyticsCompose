package com.example.halalyticscompose.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyMissionDashboardScreen(navController: NavController) {
    val primaryColor = Color(0xFF004D40)
    val secondaryColor = Color(0xFF00796B)
    val accentColor = Color(0xFFFFC107)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Daily Missions", 
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Help */ }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Help")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE0F2F1),
                            Color.White
                        )
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Point Progress Header
                item {
                    MissionProgressCard(points = 1250, target = 2000)
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // Category Tabs
                item {
                    Text(
                        "Active Missions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Daily Missions List
                items(getMockMissions()) { mission ->
                    MissionItem(mission)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // Weekly Rewards Preview
                item {
                    WeeklyRewardsSection()
                }
            }
        }
    }
}

@Composable
fun MissionProgressCard(points: Int, target: Int) {
    val progress = points.toFloat() / target.toFloat()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF004D40))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Your Points",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text(
                        points.toString(),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFC107)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = "Points",
                        tint = Color(0xFF004D40)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = Color(0xFFFFC107),
                trackColor = Color.White.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Keep scanning to reach today's goal of $target points!",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MissionItem(mission: MissionData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(mission.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    mission.icon,
                    contentDescription = null,
                    tint = mission.color
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    mission.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.DarkGray
                )
                Text(
                    mission.description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { mission.progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape),
                        color = mission.color,
                        trackColor = mission.color.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${(mission.progress * 100).toInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = mission.color
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (mission.isCompleted) Color(0xFFE8F5E9) else Color(0xFFF5F5F5))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "+${mission.reward}",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = if (mission.isCompleted) Color(0xFF43A047) else Color.Gray
                )
            }
        }
    }
}

@Composable
fun WeeklyRewardsSection() {
    Column {
        Text(
            "Weekly Streak Rewards",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val days = listOf("M", "T", "W", "T", "F", "S", "S")
            val currentDay = 4 // Friday
            
            days.forEachIndexed { index, day ->
                val isPast = index < currentDay
                val isToday = index == currentDay
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isToday -> Color(0xFFFFC107)
                                    isPast -> Color(0xFF004D40)
                                    else -> Color.White
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPast) {
                            Icon(Icons.Default.Check, "Done", tint = Color.White, modifier = Modifier.size(20.dp))
                        } else if (isToday) {
                            Icon(Icons.Default.Star, "Today", tint = Color(0xFF004D40), modifier = Modifier.size(20.dp))
                        } else {
                            Text(day, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                        }
                    }
                }
            }
        }
    }
}

data class MissionData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val progress: Float,
    val reward: Int,
    val isCompleted: Boolean = false
)

fun getMockMissions() = listOf(
    MissionData("Halal Scanner", "Scan 5 food products today", Icons.Default.QrCodeScanner, Color(0xFF00695C), 0.6f, 500),
    MissionData("Water Tracker", "Drink 8 glasses of water", Icons.Default.WaterDrop, Color(0xFF0277BD), 1.0f, 200, true),
    MissionData("BPOM Check", "Verify 2 medicine barcodes", Icons.Default.HealthAndSafety, Color(0xFFD32F2F), 0.5f, 300),
    MissionData("Community Active", "Post 1 healthy recipe", Icons.Default.Groups, Color(0xFF7B1FA2), 0.0f, 400),
    MissionData("BMI Watch", "Update your weight profile", Icons.Default.Scale, Color(0xFFF57C00), 1.0f, 150, true)
)
