package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.halalyticscompose.ui.components.MedicalAiDisclaimerBanner
import com.example.halalyticscompose.ui.theme.*
import com.example.halalyticscompose.ui.viewmodel.AiReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiReportScreen(
    navController: NavController,
    viewModel: AiReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.fetchWeeklyReport() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan AI Medis", fontWeight = FontWeight.Bold, color = Slate900) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate800)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Slate50
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> {
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(color = Emerald)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Menyusun laporan...", color = Slate600)
                    }
                }
                uiState.errorMessage != null -> {
                    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.ErrorOutline, null, tint = Error, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(uiState.errorMessage!!, textAlign = TextAlign.Center, color = Slate700)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { viewModel.fetchWeeklyReport() }, colors = ButtonDefaults.buttonColors(containerColor = Emerald)) {
                            Text("Coba Lagi")
                        }
                    }
                }
                uiState.stats != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MedicalAiDisclaimerBanner(compact = true)
                        
                        // AI Insight Card
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Brush.horizontalGradient(listOf(Emerald, TealDark)))
                                    .padding(20.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("AI HEALTH INSIGHT", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = uiState.insight?.summary ?: "Analisis tidak tersedia",
                                        color = Color.White,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                        
                        // Stats Row
                        Text("Aktivitas Klinis", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard("Total Scan", uiState.stats!!.totalScans.toString(), Icons.Default.QrCodeScanner, Color(0xFF3B82F6), Modifier.weight(1f))
                            StatCard("Halal Valid", uiState.stats!!.halalCount.toString(), Icons.Default.CheckCircle, Emerald, Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard("Peringatan", uiState.stats!!.syubhatCount.toString(), Icons.Default.Warning, Color(0xFFF59E0B), Modifier.weight(1f))
                            StatCard("Sehat", uiState.stats!!.healthyCount.toString(), Icons.Default.Favorite, Error, Modifier.weight(1f))
                        }
                        
                        // Tips
                        if (!uiState.insight?.tips.isNullOrEmpty()) {
                            Text("Tips Kesehatan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                            uiState.insight!!.tips.forEach { tip ->
                                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.TipsAndUpdates, null, tint = Emerald)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(tip, color = Slate700)
                                    }
                                }
                            }
                        }
                        
                        // History
                        if (uiState.reports.isNotEmpty()) {
                            Text("Riwayat Konsultasi AI", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                            uiState.reports.forEach { report ->
                                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.MedicalInformation, null, tint = Emerald)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(if (report.type == "bmi") "Analisis BMI" else "Konsultasi Kesehatan", fontWeight = FontWeight.Bold)
                                            }
                                            Text(report.created_at.substringBefore("T"), fontSize = 12.sp, color = Slate500)
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(report.ai_response.status_fisik ?: "Tidak ada analisis", color = Slate700)
                                        if (report.ai_response.pesan_motivasi != null) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(color = EmeraldLight.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
                                                Text("💡 ${report.ai_response.pesan_motivasi}", modifier = Modifier.padding(12.dp), fontSize = 13.sp, color = Emerald)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text(label, fontSize = 12.sp, color = Slate500)
        }
    }
}