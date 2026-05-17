package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.halalyticscompose.R
import com.example.halalyticscompose.ui.viewmodel.MainViewModel
import com.example.halalyticscompose.ui.viewmodel.MedicalRecordsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthSuiteHubScreen(
    navController: NavController,
    mainViewModel: MainViewModel = hiltViewModel(),
    medicalRecordsViewModel: MedicalRecordsViewModel = hiltViewModel()
) {
    val familyProfilesState = mainViewModel.familyProfiles.collectAsState()
    val selectedProfileState = mainViewModel.selectedFamilyProfile.collectAsState()
    val medicalRecordsState = medicalRecordsViewModel.records.collectAsState()
    val isMedicalLoadingState = medicalRecordsViewModel.isLoading.collectAsState()
    val familyProfiles = familyProfilesState.value
    val selectedProfile = selectedProfileState.value
    val medicalRecords = medicalRecordsState.value
    val isMedicalLoading = isMedicalLoadingState.value

    LaunchedEffect(Unit) {
        mainViewModel.fetchFamilyProfiles()
        medicalRecordsViewModel.loadRecords()
    }

    // ═══ Feature Categories ═══
    val nutritionFeatures = listOf(
        HealthFeatureItem("Kalori", "Hitung asupan harian", Icons.Default.LocalFireDepartment, Color(0xFFFF6D00), "calorie_counter"),
        HealthFeatureItem("Air Minum", "Pelacak hidrasi", Icons.Default.WaterDrop, Color(0xFF00B0FF), "water_tracker"),
        HealthFeatureItem("Scan Makanan", "AI foto-ke-gizi", Icons.Default.CameraAlt, Color(0xFF00C853), "meal_scan"),
        HealthFeatureItem("Voice Log", "Catat lewat suara", Icons.Default.Mic, Color(0xFF7C4DFF), "voice_logging"),
    )

    val healthFeatures = listOf(
        HealthFeatureItem("AI Asisten", "Tanya kesehatan", Icons.Default.SmartToy, Color(0xFF00897B), "health_assistant"),
        HealthFeatureItem("Monitor", "Pantauan tubuh", Icons.Default.MonitorHeart, Color(0xFFE91E63), "health_monitor"),
        HealthFeatureItem("Diary", "Jurnal kesehatan", Icons.Default.Edit, Color(0xFF5C6BC0), "health_diary"),
        HealthFeatureItem("Resep", "Menu sehat", Icons.Default.RestaurantMenu, Color(0xFFFF9800), "recipes"),
    )

    val medicalFeatures = listOf(
        HealthFeatureItem("Rekam Medis", "Catatan digital", Icons.Default.Description, Color(0xFF26A69A), "medical_records"),
        HealthFeatureItem("Obat", "Pengingat jadwal", Icons.Default.Medication, Color(0xFFEF5350), "medicine_reminders"),
        HealthFeatureItem("Emergency", "QR medis darurat", Icons.Default.QrCode2, Color(0xFFD32F2F), "emergency_qr"),
        HealthFeatureItem("Health Pass", "Kartu kesehatan", Icons.Default.Badge, Color(0xFF0288D1), "health_pass"),
    )

    val advancedFeatures = listOf(
        HealthFeatureItem("Drug Check", "Cek interaksi obat", Icons.Default.Science, Color(0xFF8E24AA), "drug_interaction"),
        HealthFeatureItem("Pill ID", "Identifikasi pil", Icons.Default.Visibility, Color(0xFF43A047), "pill_scanner"),
        HealthFeatureItem("Mental", "Kesehatan mental", Icons.Default.Psychology, Color(0xFFFF7043), "mental_health_hub"),
        HealthFeatureItem("BMI", "Kalkulator berat", Icons.Default.FitnessCenter, Color(0xFF546E7A), "bmi_calculator"),
    )

    val comingSoonFeatures = listOf(
        HealthFeatureItem("Tidur", "Analisis tidur", Icons.Default.Bedtime, Color(0xFF5C6BC0), "sleep_tracker"),
        HealthFeatureItem("Wearable", "Smartwatch sync", Icons.Default.Watch, Color(0xFF2979FF), "wearable_integration"),
        HealthFeatureItem("Belanja", "Auto grocery list", Icons.Default.ShoppingCart, Color(0xFF00C853), "grocery_list"),
    )

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.health_suite_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("notifications") }) {
                        Icon(Icons.Default.NotificationsNone, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A)
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Family Profile Selector
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        "Profil Aktif",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF00C853),
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        item {
                            HealthProfileChip(
                                name = "Saya",
                                isSelected = selectedProfile == null,
                                onClick = { mainViewModel.selectFamilyProfile(null) }
                            )
                        }
                        items(familyProfiles.size) { index ->
                            val profile = familyProfiles[index]
                            HealthProfileChip(
                                name = profile.name,
                                isSelected = selectedProfile?.id == profile.id,
                                onClick = { mainViewModel.selectFamilyProfile(profile) }
                            )
                        }
                    }
                }
            }

            // Hero Card
            item {
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF004D40), Color(0xFF26A69A)))
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            Text(
                                stringResource(R.string.health_suite_hero_title),
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                stringResource(R.string.health_suite_hero_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                HeroActionChip("Scan AI", Icons.Default.QrCodeScanner) {
                                    navController.navigate("scan_hub")
                                }
                                HeroActionChip("Kalori", Icons.Default.LocalFireDepartment) {
                                    navController.navigate("calorie_counter")
                                }
                                HeroActionChip("Resume", Icons.Default.Description) {
                                    navController.navigate("medical_resume")
                                }
                            }
                        }
                    }
                }
            }

            // ═══ NUTRISI & MAKANAN ═══
            item {
                SectionHeader("🍽️ Nutrisi & Makanan")
            }
            item {
                FeatureGrid(features = nutritionFeatures, navController = navController)
            }

            // ═══ KESEHATAN ═══
            item {
                SectionHeader("💚 Kesehatan & Wellness")
            }
            item {
                FeatureGrid(features = healthFeatures, navController = navController)
            }

            // ═══ MEDIS ═══
            item {
                SectionHeader("🏥 Rekam Medis & Darurat")
            }
            item {
                FeatureGrid(features = medicalFeatures, navController = navController)
            }

            // ═══ ADVANCED AI ═══
            item {
                SectionHeader("🤖 AI Lanjutan")
            }
            item {
                FeatureGrid(features = advancedFeatures, navController = navController)
            }

            // ═══ COMING SOON ═══
            item {
                SectionHeader("🚀 Segera Hadir")
            }
            item {
                FeatureGrid(features = comingSoonFeatures, navController = navController, isComingSoon = true)
            }

            // Recent Medical Records
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Catatan Medis Terbaru", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "Lihat Semua",
                        color = Color(0xFF00C853),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { navController.navigate("medical_records") }
                    )
                }
            }

            if (isMedicalLoading && medicalRecords.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Memuat ringkasan medis...")
                        }
                    }
                }
            } else if (medicalRecords.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Belum ada catatan medis", fontWeight = FontWeight.SemiBold)
                            Text("Tambah data di Rekam Medis Digital.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            } else {
                items(medicalRecords.take(3)) { record ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { navController.navigate("medical_records") },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE0F2F1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF00C853))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(record.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    "${record.recordType} • ${record.recordDate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══ COMPOSABLE HELPERS ═══

data class HealthFeatureItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun FeatureGrid(
    features: List<HealthFeatureItem>,
    navController: NavController,
    isComingSoon: Boolean = false
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        features.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { feature ->
                    FeatureItemCard(
                        modifier = Modifier.weight(1f),
                        feature = feature,
                        isComingSoon = isComingSoon,
                        onClick = { navController.navigate(feature.route) }
                    )
                }
                // Fill remaining space if row has less than 4 items
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FeatureItemCard(
    modifier: Modifier = Modifier,
    feature: HealthFeatureItem,
    isComingSoon: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(0.85f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(feature.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    feature.icon,
                    contentDescription = null,
                    tint = feature.color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                feature.title,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                feature.subtitle,
                fontSize = 9.sp,
                color = if (isComingSoon) Color(0xFFFF6D00) else Color.Gray,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun HeroActionChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun HealthProfileChip(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(100.dp),
        color = if (isSelected) Color(0xFF00C853) else Color(0xFF00C853).copy(alpha = 0.1f)
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else Color(0xFF00C853),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
