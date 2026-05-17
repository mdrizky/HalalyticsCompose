package com.example.halalyticscompose.healthcare.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.halalyticscompose.healthcare.model.*
import com.example.halalyticscompose.healthcare.viewmodel.HealthScannerViewModel
import com.example.halalyticscompose.ui.components.MedicalAiDisclaimerBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisResultScreen(
    navController: NavController,
    viewModel: HealthScannerViewModel
) {
    val analysis by viewModel.analysisResult.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Analysis") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        analysis?.let { result ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MedicalAiDisclaimerBanner(modifier = Modifier.padding(bottom = 8.dp), compact = true)
                // Safety Indicator
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (result.overallSafety) {
                            SafetyLevel.SAFE -> Color(0xFFDCFCE7)
                            SafetyLevel.CAUTION -> Color(0xFFFEF9C3)
                            SafetyLevel.DANGER -> Color(0xFFFEE2E2)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (result.overallSafety) {
                                SafetyLevel.SAFE -> Icons.Default.CheckCircle
                                SafetyLevel.CAUTION -> Icons.Default.Warning
                                SafetyLevel.DANGER -> Icons.Default.Report
                            },
                            contentDescription = null,
                            tint = when (result.overallSafety) {
                                SafetyLevel.SAFE -> Color(0xFF166534)
                                SafetyLevel.CAUTION -> Color(0xFF854D0E)
                                SafetyLevel.DANGER -> Color(0xFF991B1B)
                            },
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = when (result.overallSafety) {
                                    SafetyLevel.SAFE -> "Relatively Safe for You"
                                    SafetyLevel.CAUTION -> "Use with Caution"
                                    SafetyLevel.DANGER -> "Avoid This Product"
                                },
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = result.productName,
                                fontSize = 14.sp,
                                color = Color.Black.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Warnings
                Text(
                    text = "Health Warnings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                result.warnings.forEach { warning ->
                    WarningItem(warning)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Alternatives
                Text(
                    text = "Healthier Alternatives",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                result.recommendations.forEach { recommendation ->
                    AlternativeItem(recommendation)
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = result.disclaimer,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun WarningItem(warning: HealthWarning) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            when (warning.severity) {
                                WarningSeverity.LOW -> Color.Blue
                                WarningSeverity.MEDIUM -> Color(0xFFF97316) // Orange
                                WarningSeverity.HIGH -> Color.Red
                            },
                            RoundedCornerShape(4.dp)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(warning.title, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(warning.message, fontSize = 14.sp, color = Color.DarkGray)
        }
    }
}

@Composable
fun AlternativeItem(alt: AlternativeProduct) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Recommend, contentDescription = null, tint = Color(0xFF0284C7))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(alt.name, fontWeight = FontWeight.Bold)
                Text(alt.reason, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
