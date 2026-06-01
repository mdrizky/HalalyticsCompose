package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Science
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
import com.example.halalyticscompose.ui.components.ShimmerCard
import com.example.halalyticscompose.ui.theme.*
import com.example.halalyticscompose.ui.viewmodel.NutritionistDashboardViewModel

/**
 * Dasbor Ahli Gizi — ringkasan pasien & konsultasi (data dari API `/nutritionist/dashboard`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionistHomeScreen(
    navController: NavController,
    viewModel: NutritionistDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Scaffold(
        containerColor = Slate50,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(Brush.verticalGradient(listOf(TealDark, Emerald)))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp)
                ) {
                    Text(
                        "Dashboard Ahli Gizi",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    )
                    Text(
                        "Pantau kesehatan komunitas Halalytics",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val s = state) {
                is NutritionistDashboardViewModel.NutritionistUiState.Loading -> {
                    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        repeat(4) { ShimmerCard(height = 100.dp) }
                    }
                }
                is NutritionistDashboardViewModel.NutritionistUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = Error, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(s.message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.load() },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Coba Lagi") }
                    }
                }
                is NutritionistDashboardViewModel.NutritionistUiState.Loaded -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Statistik Pasien & Konsultasi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Slate900
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            NutritionStatCard("Konsultasi", "${s.activeConsultations}", Emerald, Modifier.weight(1f))
                            NutritionStatCard("Total Pasien", "${s.totalPatients}", Color(0xFF3B82F6), Modifier.weight(1f))
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Distribusi BMI Pasien", fontWeight = FontWeight.Bold, color = Slate800)
                                Spacer(Modifier.height(16.dp))
                                BmiRow("Obesitas (≥30)", s.obesity, Error)
                                BmiRow("Underweight (<18.5)", s.underweight, Color(0xFFF59E0B))
                                BmiRow("Lainnya (Normal/Overweight)", s.otherBmi, Emerald)
                            }
                        }

                        Text(
                            "Verifikasi Produk AI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Slate900
                        )
                        
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { navController.navigate("nutri_tab_verify") },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            border = BorderStroke(1.dp, Emerald.copy(alpha = 0.2f))
                        ) {
                            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Science, null, tint = Emerald, modifier = Modifier.size(32.dp))
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Analisis Menunggu Verifikasi", fontWeight = FontWeight.Bold, color = Slate900)
                                    Text("Tinjau hasil AI untuk akurasi medis.", fontSize = 12.sp, color = Slate600)
                                }
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Emerald, modifier = Modifier.size(20.dp))
                            }
                        }

                        Text(
                            "Aktivitas Terbaru",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Slate900
                        )

                        if (s.recentTitles.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Text(
                                    "Belum ada data konsultasi terbaru.",
                                    modifier = Modifier.padding(24.dp),
                                    color = Slate500,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            s.recentTitles.forEach { title ->
                                RecentActivityItem(title)
                            }
                        }
                        
                        Spacer(Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionStatCard(label: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(value, fontWeight = FontWeight.Black, fontSize = 28.sp, color = color)
            Text(label, fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun BmiRow(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp, color = Slate700)
        Text("$count orang", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
    }
}

@Composable
private fun RecentActivityItem(title: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Emerald))
            Spacer(Modifier.width(16.dp))
            Text(title, fontSize = 14.sp, color = Slate800, fontWeight = FontWeight.Medium)
        }
    }
}
