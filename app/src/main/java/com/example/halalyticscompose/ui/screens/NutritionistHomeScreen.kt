package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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
        topBar = {
            TopAppBar(
                title = { Text("Dasbor Ahli Gizi") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val s = state) {
                is NutritionistDashboardViewModel.NutritionistUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is NutritionistDashboardViewModel.NutritionistUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(s.message, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.load() }) { Text("Coba lagi") }
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
                            "Ringkasan profesional",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        s.disclaimer?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatCard("Konsultasi aktif", s.activeConsultations.toString())
                        StatCard("Total pasien (unik)", s.totalPatients.toString())
                        StatCard("BMI populasi — obesitas (≥30)", s.obesity.toString())
                        StatCard("BMI populasi — underweight (<18.5)", s.underweight.toString())
                        StatCard("BMI populasi — lainnya (tercatat)", s.otherBmi.toString())
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Konsultasi terbaru",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (s.recentTitles.isEmpty()) {
                            Text("Belum ada data konsultasi.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            s.recentTitles.forEach { line ->
                                AssistChip(onClick = {}, label = { Text(line) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}
