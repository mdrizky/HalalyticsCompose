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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.halalyticscompose.R
import com.example.halalyticscompose.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllFeaturesScreen(navController: NavController) {
    val coreFeatures = listOf(
        FeatureActionItem("Scan Halal", Icons.Default.QrCode2, "scan"),
        FeatureActionItem("Cek BPOM", Icons.Default.HealthAndSafety, "bpom_scanner"),
        FeatureActionItem("Skincare AI", Icons.Default.AutoAwesome, "skincare_scanner"),
        FeatureActionItem("Interaksi Obat", Icons.Default.Medication, "drug_interaction"),
        FeatureActionItem("Cari Obat", Icons.Default.Search, "medicine_search"),
        FeatureActionItem("Obat Internasional", Icons.Default.Public, "international_medicine")
    )
    val healthFeatures = listOf(
        FeatureActionItem("Rekam Medis", Icons.Default.MedicalServices, "medical_records"),
        FeatureActionItem("Resume Medis", Icons.Default.Description, "medical_resume"),
        FeatureActionItem("Monitor Kesehatan", Icons.Default.MonitorHeart, "health_monitor"),
        FeatureActionItem("Kalkulator BMI", Icons.Default.Calculate, "bmi_calculator"),
        FeatureActionItem("Info Medis", Icons.Default.Info, "medical_info"),
        FeatureActionItem("Kesehatan Mental", Icons.Default.Psychology, "mental_health_hub")
    )
    val aiFeatures = listOf(
        FeatureActionItem("AI Chat", Icons.AutoMirrored.Filled.Chat, "ai_chat"),
        FeatureActionItem("Asisten Kesehatan", Icons.Default.SmartToy, "health_assistant"),
        FeatureActionItem("Perjalanan Sehat", Icons.Default.CalendarMonth, "health_journey"),
        FeatureActionItem("Nutrisi AI", Icons.Default.CameraAlt, "nutrition_scanner"),
        FeatureActionItem("Buku Harian", Icons.Default.Edit, "health_diary"),
        FeatureActionItem("Scanner Pil", Icons.Default.PhotoCamera, "pill_scanner"),
        FeatureActionItem("Laporan Mingguan", Icons.Default.Assessment, "weekly_report")
    )
    val supportFeatures = listOf(
        FeatureActionItem("Donasi", Icons.Default.VolunteerActivism, "donations"),
        FeatureActionItem("Komunitas", Icons.Default.Groups, "community_hub"),
        FeatureActionItem("Kartu Sehat", Icons.Default.VerifiedUser, "health_pass"),
        FeatureActionItem("Darurat", Icons.Default.LocalHospital, "emergency_p3k", iconTint = Error),
        FeatureActionItem("Laporkan", Icons.Default.Warning, "report_issue/0/General"),
        FeatureActionItem("Pengingat Obat", Icons.Default.Alarm, "medicine_reminders")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Semua Fitur", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Slate50
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(vertical = 16.dp)) {
            item { PremiumFeatureSection("Fitur Utama", coreFeatures, navController) }
            item { PremiumFeatureSection("Kesehatan & Medis", healthFeatures, navController) }
            item { PremiumFeatureSection("Kecerdasan Buatan", aiFeatures, navController) }
            item { PremiumFeatureSection("Dukungan & Komunitas", supportFeatures, navController) }
        }
    }
}

@Composable
fun PremiumFeatureSection(title: String, features: List<FeatureActionItem>, navController: NavController) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate800, modifier = Modifier.padding(start = 4.dp, bottom = 12.dp))
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                features.chunked(4).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        row.forEach { feature ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).clickable { navController.navigate(feature.route) }) {
                                Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(18.dp)).background(feature.iconTint.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                                    Icon(feature.icon, contentDescription = feature.title, tint = feature.iconTint, modifier = Modifier.size(28.dp))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(feature.title, fontSize = 11.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            }
                        }
                        repeat(4 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

data class FeatureActionItem(val title: String, val icon: ImageVector, val route: String, val iconTint: Color = Emerald)