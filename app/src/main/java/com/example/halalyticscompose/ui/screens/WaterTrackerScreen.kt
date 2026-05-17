package com.example.halalyticscompose.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTrackerScreen(navController: NavController) {
    val waterGoal = 2500 // ml
    var currentIntake by remember { mutableIntStateOf(1200) }
    val progress = (currentIntake.toFloat() / waterGoal).coerceIn(0f, 1f)
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = FastOutSlowInEasing)
    )

    val waterLogs = remember {
        mutableStateListOf(
            WaterLogEntry("08:00", 250, "Bangun Tidur"),
            WaterLogEntry("10:30", 300, "Setelah Olahraga"),
            WaterLogEntry("12:00", 250, "Sebelum Makan Siang"),
            WaterLogEntry("14:30", 200, "Sore"),
            WaterLogEntry("16:00", 200, "Setelah Meeting")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pelacak Air Minum", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
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
            // Progress Ring
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Background ring
                                drawArc(
                                    color = Color(0xFFE3F2FD),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 20f, cap = StrokeCap.Round),
                                    topLeft = Offset(20f, 20f),
                                    size = Size(size.width - 40f, size.height - 40f)
                                )
                                // Progress ring
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        listOf(Color(0xFF00B0FF), Color(0xFF2979FF), Color(0xFF00B0FF))
                                    ),
                                    startAngle = -90f,
                                    sweepAngle = animatedProgress * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 20f, cap = StrokeCap.Round),
                                    topLeft = Offset(20f, 20f),
                                    size = Size(size.width - 40f, size.height - 40f)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = Color(0xFF00B0FF),
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    "${currentIntake}",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1A1A1A)
                                )
                                Text(
                                    "/ ${waterGoal}ml",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "${(progress * 100).toInt()}% target tercapai",
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF00B0FF)
                        )
                    }
                }
            }

            // Quick Add Buttons
            item {
                Text("Tambah Cepat", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf(100, 200, 250, 500).forEach { ml ->
                        Surface(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                currentIntake = (currentIntake + ml).coerceAtMost(waterGoal + 1000)
                                waterLogs.add(0, WaterLogEntry(
                                    time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                                    amount = ml,
                                    label = "Manual"
                                ))
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFE3F2FD)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = Color(0xFF2979FF),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "+${ml}ml",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF2979FF)
                                )
                            }
                        }
                    }
                }
            }

            // History
            item {
                Text("Riwayat Hari Ini", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            items(waterLogs) { log ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE3F2FD)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = Color(0xFF2979FF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${log.amount}ml", fontWeight = FontWeight.Bold)
                            Text(log.label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Text(log.time, color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

private data class WaterLogEntry(val time: String, val amount: Int, val label: String)
