package com.example.halalyticscompose.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.halalyticscompose.ui.viewmodel.MedicineViewModel
import androidx.hilt.navigation.compose.hiltViewModel

// ═══════════════════════════════════════════════════════════════════
// MEDICINE DETAIL SCREEN — DIGITAL PHARMACIST
// Enhanced UI with structured layout, schedule, conflict check
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineDetailScreen(
    navController: NavController,
    medicineId: Int,
    viewModel: MedicineViewModel = hiltViewModel()
) {
    val medicine by viewModel.selectedMedicine.collectAsState()
    val safeSchedule by viewModel.safeSchedule.collectAsState()
    val personalRiskScore by viewModel.personalRiskScore.collectAsState()
    val drugFoodConflict by viewModel.drugFoodConflict.collectAsState()
    val isRiskLoading by viewModel.isRiskLoading.collectAsState()
    val isConflictLoading by viewModel.isConflictLoading.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    var mealRelation by remember { mutableStateOf("after_meal") }
    var showScheduleSection by remember { mutableStateOf(false) }
    var showConflictSection by remember { mutableStateOf(false) }

    LaunchedEffect(medicineId) {
        viewModel.getMedicineDetail(medicineId)
        viewModel.clearSafeSchedule()
        viewModel.fetchPersonalRiskScore()
    }

    LaunchedEffect(medicine?.name, medicine?.idMedicine) {
        medicine?.let { med ->
            viewModel.checkDrugFoodConflict(
                medicineName = med.genericName ?: med.name,
                medicineId = med.idMedicine ?: med.id,
                lookbackMinutes = 180
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocalPharmacy,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Digital Pharmacist", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    titleContentColor = colorScheme.onBackground,
                    navigationIconContentColor = colorScheme.onBackground
                )
            )
        },
        containerColor = colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading && medicine == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF3B82F6)
                )
            } else if (errorMessage != null && medicine == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        errorMessage ?: "Gagal memuat data",
                        textAlign = TextAlign.Center,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { viewModel.getMedicineDetail(medicineId) }) {
                        Text("Coba Lagi")
                    }
                }
            } else {
                medicine?.let { med ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // ─── Medicine Hero Card ──────────────────
                        MedicineHeroCard(med = med)

                        Spacer(modifier = Modifier.height(20.dp))

                        // ─── Name & Badges ───────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        ) {
                            Text(
                                text = med.name,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colorScheme.onSurface
                            )
                            med.genericName?.let {
                                Text(
                                    text = it,
                                    fontSize = 15.sp,
                                    color = colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MedBadge(
                                    text = med.dosageForm ?: "Tablet",
                                    icon = Icons.Default.Medication,
                                    color = Color(0xFF3B82F6)
                                )
                                MedBadge(
                                    text = med.kategori ?: "Umum",
                                    icon = Icons.Default.Category,
                                    color = Color(0xFF8B5CF6)
                                )
                                MedBadge(
                                    text = med.halalStatus.uppercase(),
                                    icon = when (med.halalStatus.lowercase()) {
                                        "halal" -> Icons.Default.CheckCircle
                                        "haram" -> Icons.Default.Cancel
                                        else -> Icons.AutoMirrored.Filled.Help
                                    },
                                    color = when (med.halalStatus.lowercase()) {
                                        "halal" -> Color(0xFF22C55E)
                                        "haram" -> Color(0xFFEF4444)
                                        else -> Color(0xFFF59E0B)
                                    }
                                )
                                MedBadge(
                                    text = (med.bpomStatus ?: "Unverified").uppercase(),
                                    icon = if ((med.bpomStatus ?: "").lowercase() == "registered") Icons.Default.Verified else Icons.Default.GppMaybe,
                                    color = if ((med.bpomStatus ?: "").lowercase() == "registered") Color(0xFF0EA5E9) else Color(0xFF64748B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ─── Info Cards ──────────────────────────
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            InfoCard(
                                title = "Indikasi / Kegunaan",
                                content = med.description ?: "Informasi tidak tersedia",
                                icon = Icons.Default.Info
                            )
                            InfoCard(
                                title = "Dosis",
                                content = med.dosageInfo ?: "Gunakan sesuai petunjuk dokter",
                                icon = Icons.Default.Straighten
                            )
                            if (!med.ingredients.isNullOrEmpty()) {
                                InfoCard(
                                    title = "Komposisi",
                                    content = med.ingredients!!.joinToString(", "),
                                    icon = Icons.Default.Science
                                )
                            }
                            InfoCard(
                                title = "Efek Samping",
                                content = med.sideEffects ?: "Efek samping minimal jika sesuai dosis",
                                icon = Icons.Default.Warning,
                                tint = Color(0xFFF59E0B)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // ─── Digital Pharmacist Section ──────────
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Text(
                                "🩺 Digital Pharmacist",
                                fontWeight = FontWeight.ExtraBold,
                                color = colorScheme.onSurface,
                                fontSize = 20.sp
                            )
                            Text(
                                "Fitur cerdas untuk membantu jadwal & keamanan obat",
                                color = colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Schedule Generator Toggle
                            PharmacistFeatureCard(
                                title = "Generate Jadwal Aman",
                                subtitle = "AI membuat jadwal minum obat optimal",
                                icon = Icons.Default.Schedule,
                                gradientColors = listOf(Color(0xFF0EA5E9), Color(0xFF38BDF8)),
                                expanded = showScheduleSection,
                                onToggle = { showScheduleSection = !showScheduleSection }
                            )

                            AnimatedVisibility(
                                visible = showScheduleSection,
                                enter = fadeIn() + expandVertically()
                            ) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ScheduleChip("Sesudah makan", mealRelation == "after_meal") { mealRelation = "after_meal" }
                                        ScheduleChip("Sebelum makan", mealRelation == "before_meal") { mealRelation = "before_meal" }
                                        ScheduleChip("Saat makan", mealRelation == "with_meal") { mealRelation = "with_meal" }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            viewModel.generateSafeSchedule(
                                                medicineId = med.idMedicine ?: med.id,
                                                medicineName = med.genericName ?: med.name,
                                                frequencyPerDay = med.frequencyPerDay ?: 3,
                                                mealRelation = mealRelation
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9))
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Generate", fontWeight = FontWeight.Bold)
                                    }

                                    safeSchedule?.let { schedule ->
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Text(
                                                    "Jadwal: ${schedule.scheduleTimes.joinToString(" • ")}",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1E40AF),
                                                    fontSize = 14.sp
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    schedule.disclaimer ?: "Data ini hanya referensi dan bukan pengganti konsultasi dokter.",
                                                    color = Color(0xFF64748B),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Drug-Food Conflict Toggle
                            PharmacistFeatureCard(
                                title = "Drug-Food Conflict",
                                subtitle = "Cek interaksi obat dengan makanan terakhir",
                                icon = Icons.Default.ReportProblem,
                                gradientColors = listOf(Color(0xFF7C3AED), Color(0xFFA78BFA)),
                                expanded = showConflictSection,
                                onToggle = { showConflictSection = !showConflictSection }
                            )

                            AnimatedVisibility(
                                visible = showConflictSection,
                                enter = fadeIn() + expandVertically()
                            ) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    Button(
                                        onClick = {
                                            viewModel.checkDrugFoodConflict(
                                                medicineName = med.genericName ?: med.name,
                                                medicineId = med.idMedicine ?: med.id,
                                                lookbackMinutes = 180
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Cek Ulang Konflik", fontWeight = FontWeight.Bold)
                                    }

                                    if (isConflictLoading) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                    }

                                    drugFoodConflict?.let { conflict ->
                                        Spacer(modifier = Modifier.height(10.dp))
                                        val level = (conflict.severity ?: "none").lowercase()
                                        val conflictColor = when (level) {
                                            "major", "contraindicated" -> Color(0xFFDC2626)
                                            "moderate" -> Color(0xFFD97706)
                                            "minor" -> Color(0xFF16A34A)
                                            else -> Color(0xFF0EA5E9)
                                        }
                                        val conflictBg = when (level) {
                                            "major", "contraindicated" -> Color(0xFFFEF2F2)
                                            "moderate" -> Color(0xFFFFFBEB)
                                            "minor" -> Color(0xFFF0FDF4)
                                            else -> Color(0xFFF0F9FF)
                                        }

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = conflictBg)
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        if (conflict.hasConflict) Icons.Default.Warning else Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = conflictColor,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        if (conflict.hasConflict) "Konflik Terdeteksi (${conflict.severity ?: "-"})" else "Tidak Ada Konflik Besar",
                                                        fontWeight = FontWeight.Bold,
                                                        color = conflictColor,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))

                                                val topMatch = conflict.matches.firstOrNull()
                                                val conflictText = buildString {
                                                    if (conflict.hasConflict && topMatch != null) {
                                                        append("Pemicu: ${topMatch.foodName ?: "-"}\n")
                                                        append("Alasan: ${topMatch.reason ?: "-"}\n\n")
                                                    }
                                                    append(conflict.recommendation ?: "")
                                                    if (!conflict.disclaimer.isNullOrBlank()) {
                                                        append("\n\n${conflict.disclaimer}")
                                                    }
                                                }
                                                Text(
                                                    conflictText,
                                                    color = Color(0xFF475569),
                                                    fontSize = 12.sp,
                                                    lineHeight = 18.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Risk Score Section
                            personalRiskScore?.let { risk ->
                                val riskColor = when ((risk.riskLevel ?: "").lowercase()) {
                                    "high" -> Color(0xFFDC2626)
                                    "moderate" -> Color(0xFFD97706)
                                    else -> Color(0xFF16A34A)
                                }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.HealthAndSafety,
                                                contentDescription = null,
                                                tint = riskColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Health Risk: ${risk.riskLevel?.uppercase() ?: "-"} (${risk.riskScore ?: 0})",
                                                fontWeight = FontWeight.Bold,
                                                color = riskColor,
                                                fontSize = 15.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            NutrientMini("Sugar", "${risk.totals?.sugarG ?: 0.0}g", "${risk.limits?.sugarG ?: 50.0}g")
                                            NutrientMini("Sodium", "${risk.totals?.sodiumMg ?: 0.0}mg", "${risk.limits?.sodiumMg ?: 2300.0}mg")
                                            NutrientMini("Fat", "${risk.totals?.fatG ?: 0.0}g", "${risk.limits?.fatG ?: 67.0}g")
                                        }

                                        risk.recommendation?.takeIf { it.isNotBlank() }?.let {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(it, color = colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            if (isRiskLoading) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // ─── Set Reminder Button ─────────────────
                        Button(
                            onClick = { navController.navigate("add_medicine_reminder") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1A237E)
                            )
                        ) {
                            Icon(Icons.Default.AlarmAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pasang Pengingat Minum Obat", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// SUB-COMPOSABLES
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun MedicineHeroCard(med: com.example.halalyticscompose.data.model.MedicineData) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        val fallbackUrl = "https://ui-avatars.com/api/?name=${med.name}&background=1E293B&color=3B82F6&size=400"
        AsyncImage(
            model = if (!med.imageUrl.isNullOrBlank()) med.imageUrl else fallbackUrl,
            contentDescription = med.name,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun MedBadge(text: String, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = color)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PharmacistFeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun NutrientMini(label: String, current: String, limit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Text(current, color = Color(0xFF0F172A), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text("/ $limit", color = Color(0xFF94A3B8), fontSize = 10.sp)
    }
}

@Composable
fun InfoCard(title: String, content: String, icon: ImageVector, tint: Color = Color(0xFF3B82F6)) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = tint)
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Bold, color = colorScheme.onSurface, fontSize = 15.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colorScheme.surface)
                .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Text(
                text = content,
                color = colorScheme.onSurfaceVariant,
                lineHeight = 22.sp,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ScheduleChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(if (selected) Color(0xFFDBEAFE) else Color(0xFFF1F5F9))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            color = if (selected) Color(0xFF1D4ED8) else Color(0xFF475569),
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun Modifier.shadowCustom(elevation: androidx.compose.ui.unit.Dp, shape: androidx.compose.ui.graphics.Shape) = this.then(
    Modifier.shadow(elevation, shape)
)
