package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.halalyticscompose.ui.viewmodel.AuthViewModel
import com.example.halalyticscompose.ui.viewmodel.HistoryViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    historyViewModel: HistoryViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val color = MaterialTheme.colorScheme
    
    // Health data
    val userData by authViewModel.userData.collectAsState()
    val totalScans by historyViewModel.totalScans.collectAsState()
    val halalProducts by historyViewModel.halalProducts.collectAsState()
    
    // Calculated values
    val bmi = userData?.bmi?.toString() ?: "0.0"
    val activityLevel = userData?.activityLevel ?: "Sedang"

    var showBmiDialog by remember { mutableStateOf(false) }
    var showActivityDialog by remember { mutableStateOf(false) }
    var heightInput by remember { mutableStateOf(userData?.height?.toString().orEmpty()) }
    var weightInput by remember { mutableStateOf(userData?.weight?.toString().orEmpty()) }
    var selectedActivity by remember { mutableStateOf(activityLevel) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Profil Kesehatan", 
                        fontWeight = FontWeight.Bold,
                        color = color.onBackground
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = color.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = color.background
                )
            )
        },
        containerColor = color.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            color.background,
                            color.background,
                            color.primary.copy(alpha = 0.04f)
                        )
                    )
                )
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // BMI Card
            HealthCard(
                title = "Indeks Massa Tubuh (BMI)",
                icon = Icons.Default.MonitorWeight,
                accentColor = Color(0xFF3B82F6)
            ) {
                Column {
                    Text(
                        text = bmi,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = color.onSurface
                    )
                    Text(
                        text = getBMICategory(bmi),
                        fontSize = 14.sp,
                        color = color.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { 
                            heightInput = userData?.height?.toString().orEmpty()
                            weightInput = userData?.weight?.toString().orEmpty()
                            showBmiDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6)
                        )
                    ) {
                        Text("Hitung Ulang BMI")
                    }
                }
            }

            // Activity Level Card
            HealthCard(
                title = "Tingkat Aktivitas",
                icon = Icons.AutoMirrored.Filled.DirectionsRun,
                accentColor = Color(0xFF10B981)
            ) {
                Column {
                    Text(
                        text = activityLevel,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = color.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { 
                            selectedActivity = activityLevel
                            showActivityDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        )
                    ) {
                        Text("Ubah Aktivitas")
                    }
                }
            }

            // Scan Statistics Card
            HealthCard(
                title = "Statistik Scan",
                icon = Icons.Default.QrCodeScanner,
                accentColor = Color(0xFF8B5CF6)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem("Total Scan", totalScans.toString())
                        StatItem("Produk Halal", halalProducts.toString())
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { 
                            navController.navigate("history")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B5CF6)
                        )
                    ) {
                        Text("Lihat Riwayat")
                    }
                }
            }

            // Health Tools Card
            HealthCard(
                title = "Alat Kesehatan",
                icon = Icons.Default.HealthAndSafety,
                accentColor = Color(0xFF0EA5E9)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HealthToolItem(
                        title = "Scanner Makanan",
                        description = "Scan kesehatan makanan",
                        icon = Icons.Default.CameraAlt,
                        onClick = { navController.navigate("scan") }
                    )
                    
                    HealthToolItem(
                        title = "AI Meal Scanner",
                        description = "Analisis makanan dengan AI",
                        icon = Icons.Default.Restaurant,
                        onClick = { navController.navigate("meal_scan") }
                    )
                    
                    HealthToolItem(
                        title = "Asisten Kesehatan AI",
                        description = "Konsultasi dengan AI",
                        icon = Icons.Default.MedicalInformation,
                        onClick = { navController.navigate("health_assistant") }
                    )
                }
            }
        }
    }

    if (showBmiDialog) {
        AlertDialog(
            onDismissRequest = { showBmiDialog = false },
            title = { Text("Hitung BMI") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it },
                        label = { Text("Tinggi (cm)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Berat (kg)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val h = heightInput.toDoubleOrNull()
                        val w = weightInput.toDoubleOrNull()
                        if (h != null && w != null && h > 0 && w > 0) {
                            authViewModel.updateProfile(
                                height = h,
                                weight = w
                            )
                        }
                        showBmiDialog = false
                    }
                ) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showBmiDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showActivityDialog) {
        val activityOptions = listOf("Rendah", "Sedang", "Tinggi", "Sangat Tinggi")
        AlertDialog(
            onDismissRequest = { showActivityDialog = false },
            title = { Text("Pilih Tingkat Aktivitas") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    activityOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedActivity = option }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedActivity.equals(option, ignoreCase = true),
                                onClick = { selectedActivity = option }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(option)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.updateProfile(activityLevel = selectedActivity)
                        showActivityDialog = false
                    }
                ) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showActivityDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun HealthCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    val color = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = color.onSurface
                )
            }
            content()
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    val color = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color.onSurface
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = color.onSurfaceVariant
        )
    }
}

@Composable
fun HealthToolItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val color = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF0EA5E9),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color.onSurface
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = color.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = color.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

fun getBMICategory(bmi: String): String {
    return try {
        val bmiValue = bmi.toFloatOrNull() ?: return "Tidak diketahui"
        when {
            bmiValue < 17.0 -> "Sangat Kurus"
            bmiValue < 18.5 -> "Kurus"
            bmiValue < 20.0 -> "Normal (Tipis)"
            bmiValue < 23.0 -> "Normal (Ideal)"
            bmiValue < 25.0 -> "Berlebih"
            else -> "Obesitas"
        }
    } catch (e: Exception) {
        "Tidak diketahui"
    }
}
