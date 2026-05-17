package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/**
 * Grocery List & Smart Wearable — "Dalam Pengembangan" Placeholder Screens
 * These use a premium design to show planned features.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreen(navController: NavController) {
    UnderDevelopmentScreen(
        navController = navController,
        title = "Daftar Belanja Cerdas",
        featureName = "Auto Grocery List",
        description = "Fitur ini akan otomatis menyusun daftar belanja berdasarkan meal plan Anda. " +
                "Terintegrasi dengan layanan belanja online untuk kemudahan Anda.",
        icon = Icons.Default.ShoppingCart,
        gradientColors = listOf(Color(0xFF00C853), Color(0xFF69F0AE)),
        plannedFeatures = listOf(
            PlannedFeature("Auto-generate dari Meal Plan", "Daftar belanja otomatis dari rencana makan mingguan."),
            PlannedFeature("Integrasi E-Commerce", "Hubungkan ke layanan belanja online favorit."),
            PlannedFeature("Smart Substitution", "AI merekomendasikan bahan pengganti yang lebih sehat."),
            PlannedFeature("Budget Tracker", "Lacak pengeluaran belanjaan bulanan Anda."),
            PlannedFeature("Shared List", "Bagikan daftar belanja dengan anggota keluarga.")
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WearableIntegrationScreen(navController: NavController) {
    UnderDevelopmentScreen(
        navController = navController,
        title = "Integrasi Perangkat",
        featureName = "Smart Wearable Hub",
        description = "Hubungkan smartwatch dan perangkat kebugaran untuk memantau detak jantung, " +
                "langkah kaki, dan kualitas tidur secara real-time.",
        icon = Icons.Default.Watch,
        gradientColors = listOf(Color(0xFF2979FF), Color(0xFF00B0FF)),
        plannedFeatures = listOf(
            PlannedFeature("Google Fit Sync", "Sinkronisasi otomatis dengan Google Fit API."),
            PlannedFeature("Heart Rate Monitor", "Pantau detak jantung real-time dari smartwatch."),
            PlannedFeature("Step Counter", "Hitung langkah harian dan estimasi kalori terbakar."),
            PlannedFeature("Sleep Analysis", "Analisis kualitas tidur dari data sensor."),
            PlannedFeature("Smart Scale", "Hubungkan timbangan pintar untuk tracking berat badan.")
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTrackerScreen(navController: NavController) {
    UnderDevelopmentScreen(
        navController = navController,
        title = "Pelacak Tidur",
        featureName = "Sleep Analytics",
        description = "Analisis pola tidur Anda dan dapatkan rekomendasi AI untuk kualitas tidur yang lebih baik.",
        icon = Icons.Default.Bedtime,
        gradientColors = listOf(Color(0xFF5C6BC0), Color(0xFF9FA8DA)),
        plannedFeatures = listOf(
            PlannedFeature("Smart Alarm", "Alarm yang membangunkan di fase tidur ringan."),
            PlannedFeature("Sleep Score", "Skor kualitas tidur berdasarkan durasi dan pola."),
            PlannedFeature("Trend Analysis", "Grafik tren tidur mingguan dan bulanan."),
            PlannedFeature("Wind Down", "Panduan relaksasi sebelum tidur dengan audio."),
            PlannedFeature("AI Insight", "Korelasi kualitas tidur dengan pola makan Anda.")
        )
    )
}

// ═══════════════════════════════════════════════════════════════
// REUSABLE "UNDER DEVELOPMENT" TEMPLATE
// ═══════════════════════════════════════════════════════════════

data class PlannedFeature(val title: String, val description: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnderDevelopmentScreen(
    navController: NavController,
    title: String,
    featureName: String,
    description: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    plannedFeatures: List<PlannedFeature>
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(gradientColors))
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                featureName,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = Color.White.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    "🚀 Dalam Pengembangan",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                description,
                                color = Color.White.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Planned Features
            item {
                Text(
                    "Fitur yang Direncanakan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(plannedFeatures.size) { index ->
                val feature = plannedFeatures[index]
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(gradientColors[0].copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${index + 1}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = gradientColors[0]
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                feature.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1A1A1A)
                            )
                            Text(
                                feature.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                lineHeight = 16.sp
                            )
                        }
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Notify Me Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = gradientColors[0],
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Ingin diberitahu saat fitur ini siap?",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Aktifkan notifikasi untuk mendapat update terbaru.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { /* Register for notification */ },
                            colors = ButtonDefaults.buttonColors(containerColor = gradientColors[0]),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Beritahu Saya")
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
