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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalorieCounterScreen(navController: NavController) {
    val calorieGoal = 2000
    var caloriesConsumed by remember { mutableIntStateOf(1340) }
    var proteinConsumed by remember { mutableIntStateOf(65) }
    var carbConsumed by remember { mutableIntStateOf(140) }
    var fatConsumed by remember { mutableIntStateOf(45) }

    val proteinGoal = 120
    val carbGoal = 250
    val fatGoal = 65

    val calorieProgress = (caloriesConsumed.toFloat() / calorieGoal).coerceIn(0f, 1f)
    val animatedCalProgress by animateFloatAsState(
        targetValue = calorieProgress,
        animationSpec = tween(800, easing = FastOutSlowInEasing)
    )

    val mealLog = remember {
        listOf(
            MealEntry("Sarapan", "Nasi Uduk + Telur Balado", 450, "08:15", Icons.Default.WbSunny, Color(0xFFFFF8E1)),
            MealEntry("Snack Pagi", "Pisang + Yogurt", 180, "10:30", Icons.Default.BrunchDining, Color(0xFFE8F5E9)),
            MealEntry("Makan Siang", "Ayam Bakar + Sayur", 520, "12:45", Icons.Default.Restaurant, Color(0xFFE3F2FD)),
            MealEntry("Snack Sore", "Kacang Almond (30g)", 190, "15:00", Icons.Default.EmojiFoodBeverage, Color(0xFFFCE4EC))
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Penghitung Kalori", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("voice_logging") }) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Log", tint = Color(0xFF00C853))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("meal_scan") },
                containerColor = Color(0xFF00C853),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                text = { Text("Scan Makanan") },
                shape = RoundedCornerShape(16.dp)
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
            // Calorie Ring Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Hari Ini", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier.size(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = Color(0xFFF5F5F5),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 24f, cap = StrokeCap.Round),
                                    topLeft = Offset(20f, 20f),
                                    size = Size(size.width - 40f, size.height - 40f)
                                )
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        listOf(Color(0xFF00C853), Color(0xFF69F0AE), Color(0xFF00C853))
                                    ),
                                    startAngle = -90f,
                                    sweepAngle = animatedCalProgress * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 24f, cap = StrokeCap.Round),
                                    topLeft = Offset(20f, 20f),
                                    size = Size(size.width - 40f, size.height - 40f)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${calorieGoal - caloriesConsumed}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1A1A1A)
                                )
                                Text("kkal tersisa", fontSize = 12.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Macro Bars
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MacroIndicator("Protein", proteinConsumed, proteinGoal, Color(0xFFE91E63), "g")
                            MacroIndicator("Karbo", carbConsumed, carbGoal, Color(0xFFFF9800), "g")
                            MacroIndicator("Lemak", fatConsumed, fatGoal, Color(0xFF2196F3), "g")
                        }
                    }
                }
            }

            // Quick Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionChip(
                        modifier = Modifier.weight(1f),
                        label = "Voice Log",
                        icon = Icons.Default.Mic,
                        color = Color(0xFFF1F8E9),
                        onClick = { navController.navigate("voice_logging") }
                    )
                    QuickActionChip(
                        modifier = Modifier.weight(1f),
                        label = "Scan Label",
                        icon = Icons.Default.QrCodeScanner,
                        color = Color(0xFFE3F2FD),
                        onClick = { navController.navigate("ocr_scan") }
                    )
                    QuickActionChip(
                        modifier = Modifier.weight(1f),
                        label = "Air Minum",
                        icon = Icons.Default.WaterDrop,
                        color = Color(0xFFE0F7FA),
                        onClick = { navController.navigate("water_tracker") }
                    )
                }
            }

            // Meal Log
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Log Makanan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "${caloriesConsumed} / ${calorieGoal} kkal",
                        fontSize = 13.sp,
                        color = Color(0xFF00C853),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            items(mealLog) { meal ->
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
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(meal.bgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(meal.icon, contentDescription = null, tint = Color(0xFF004D40))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(meal.mealType, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(meal.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${meal.calories} kkal",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF00C853),
                                fontSize = 15.sp
                            )
                            Text(meal.time, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }
            }

            // Nutrition Insight
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF004D40)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFFD600))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Insight AI", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Asupan protein kamu masih kurang 55g hari ini. " +
                            "Coba tambahkan dada ayam (31g protein/100g) atau tahu (8g protein/100g) untuk makan malam.",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun MacroIndicator(name: String, current: Int, goal: Int, color: Color, unit: String) {
    val progress = (current.toFloat() / goal).coerceIn(0f, 1f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$current/$goal$unit", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .width(80.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(name, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
private fun QuickActionChip(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = color
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF004D40), modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF004D40))
        }
    }
}

private data class MealEntry(
    val mealType: String,
    val description: String,
    val calories: Int,
    val time: String,
    val icon: ImageVector,
    val bgColor: Color
)
