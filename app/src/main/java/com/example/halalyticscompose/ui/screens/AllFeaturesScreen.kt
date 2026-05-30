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
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
    val halalFeatures = listOf(
        FeatureActionItem("Scan Barcode", Icons.Default.QrCodeScanner, "scan"),
        FeatureActionItem("OCR Ingredient", Icons.Default.TextFields, "ocr_scan"),
        FeatureActionItem("Halal AI Analysis", Icons.Default.AutoAwesome, "ai_analysis"),
        FeatureActionItem("Halal Education", Icons.AutoMirrored.Filled.MenuBook, "encyclopedia"),
        FeatureActionItem("Verifikasi Produk", Icons.Default.Verified, "product_request"),
        FeatureActionItem("Cek Bahan Berisiko", Icons.Default.Warning, "risk_checker")
    )
    val healthFeatures = listOf(
        FeatureActionItem("BMI Checker", Icons.Default.Calculate, "bmi_calculator"),
        FeatureActionItem("Analisis Nutrisi", Icons.Default.Analytics, "nutrition_scanner"),
        FeatureActionItem("Peringatan Gula", Icons.Default.ErrorOutline, "sugar_warning"),
        FeatureActionItem("Rekomendasi Sehat", Icons.Default.Recommend, "health_monitor"),
        FeatureActionItem("Tips Diet", Icons.Default.Lightbulb, "diet_tips"),
        FeatureActionItem("Water Reminder", Icons.Default.WaterDrop, "water_tracker")
    )
    val aiFeatures = listOf(
        FeatureActionItem("AI Chat", Icons.AutoMirrored.Filled.Chat, "ai_chat"),
        FeatureActionItem("AI Nutritionist", Icons.Default.SmartToy, "health_assistant"),
        FeatureActionItem("AI Halal Assistant", Icons.Default.Assistant, "ai_chat"),
        FeatureActionItem("AI Product Analysis", Icons.Default.Psychology, "ai_analysis"),
        FeatureActionItem("AI Recommendation", Icons.Default.AutoGraph, "search_external")
    )
    val donorFeatures = listOf(
        FeatureActionItem("Donor Event", Icons.Default.Event, "donor_events"),
        FeatureActionItem("Emergency Blood", Icons.Default.LocalHospital, "emergency_blood", iconTint = Error),
        FeatureActionItem("Volunteer Donor", Icons.Default.VolunteerActivism, "donor_home"),
        FeatureActionItem("Nearby Donor", Icons.Default.LocationOn, "nearby_donor"),
        FeatureActionItem("Blood History", Icons.Default.History, "donor_history")
    )
    val userFeatures = listOf(
        FeatureActionItem("Profil", Icons.Default.Person, "profile"),
        FeatureActionItem("Notifikasi", Icons.Default.Notifications, "notifications"),
        FeatureActionItem("Produk Favorit", Icons.Default.Favorite, "favorites"),
        FeatureActionItem("History Scan", Icons.Default.Restore, "history"),
        FeatureActionItem("Upload Produk", Icons.Default.CloudUpload, "contribution"),
        FeatureActionItem("Report Produk", Icons.Default.Flag, "report_issue/0/General")
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
            item { PremiumFeatureSection("HALAL FEATURES", halalFeatures, navController) }
            item { PremiumFeatureSection("HEALTH FEATURES", healthFeatures, navController) }
            item { PremiumFeatureSection("AI FEATURES", aiFeatures, navController) }
            item { PremiumFeatureSection("DONOR FEATURES", donorFeatures, navController) }
            item { PremiumFeatureSection("USER FEATURES", userFeatures, navController) }
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