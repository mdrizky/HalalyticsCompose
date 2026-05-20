package com.example.halalyticscompose.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

// ═══════════════════════════════════════════════════════════════════
// COLOR CONSTANTS — Emerald Forest Premium
// ═══════════════════════════════════════════════════════════════════
private val EmeraldDark = Color(0xFF004D40)
private val EmeraldMedium = Color(0xFF00695C)
private val EmeraldLight = Color(0xFF26A69A)
private val SageBg = Color(0xFFF4F9F8)
private val SoftSage = Color(0xFFE0F2F1)
private val CardBg = Color(0xFFFFFFFF)
private val TextDark = Color(0xFF212121)
private val TextMedium = Color(0xFF757575)
private val TextLight = Color(0xFF9E9E9E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalInfoScreen(
    navController: NavController,
    viewModel: com.example.halalyticscompose.ui.viewmodel.MedicalInfoViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val profileData by viewModel.profileData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var weightKg by remember { mutableStateOf("") }
    var heightCm by remember { mutableStateOf("") }
    var chronicDiseases by remember { mutableStateOf("") }
    var hasGerd by remember { mutableStateOf<Boolean?>(null) }
    var bloodType by remember { mutableStateOf("") }
    var additionalNotes by remember { mutableStateOf("") }
    val selectedAllergies = remember { mutableStateListOf<String>() }
    
    var showAllergyModal by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    // Load profile on start
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    // Update state when profile data is loaded
    LaunchedEffect(profileData) {
        profileData?.let { data ->
            weightKg = (data["weight_kg"] ?: "").toString()
            heightCm = (data["height_cm"] ?: "").toString()
            chronicDiseases = (data["chronic_diseases"] ?: "").toString()
            hasGerd = data["has_gerd"] as? Boolean
            bloodType = (data["blood_type"] ?: "").toString()
            additionalNotes = (data["additional_notes"] ?: "").toString()
            
            val allergies = data["drug_allergies"] as? List<*>
            selectedAllergies.clear()
            allergies?.forEach { it?.let { a -> selectedAllergies.add(a.toString()) } }
        }
    }

    // BMI Calculation
    val bmi = remember(weightKg, heightCm) {
        val w = weightKg.toDoubleOrNull()
        val h = heightCm.toDoubleOrNull()
        if (w != null && h != null && h > 0) {
            val hm = h / 100.0
            w / (hm * hm)
        } else null
    }

    val bmiCategory = when {
        bmi == null -> null
        bmi < 18.5 -> "Kurus"
        bmi < 25.0 -> "Normal"
        bmi < 30.0 -> "Gemuk"
        else -> "Obesitas"
    }

    val bmiColor = when (bmiCategory) {
        "Kurus" -> Color(0xFF3B82F6)
        "Normal" -> EmeraldDark
        "Gemuk" -> Color(0xFFF57C00)
        "Obesitas" -> Color(0xFFD32F2F)
        else -> TextLight
    }

    Scaffold(containerColor = SageBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── EMERALD GRADIENT HEADER ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(EmeraldDark, EmeraldMedium, EmeraldLight)
                        )
                    )
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 28.dp)
            ) {
                Column {
                    // Back + Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable { navController.popBackStack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack, null,
                                tint = Color.White, modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Informasi Medis",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Hero illustration
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.15f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🩺", fontSize = 42.sp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    "Data untuk Perawatan Maksimal",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Digunakan untuk personalisasi AI\ndan tidak dibagikan ke pihak lain.",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(20.dp))

                // ── Berat & Tinggi Badan (side by side) ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Berat Badan",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = TextDark
                        )
                        Text("(kg)", fontSize = 11.sp, color = TextLight)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = weightKg,
                            onValueChange = { if (it.length <= 5) weightKg = it },
                            placeholder = { Text("55", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Tinggi Badan",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = TextDark
                        )
                        Text("(cm)", fontSize = 11.sp, color = TextLight)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = heightCm,
                            onValueChange = { if (it.length <= 3) heightCm = it },
                            placeholder = { Text("165", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }

                // ── BMI Display ──
                if (bmi != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = bmiColor.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, bmiColor.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(bmiColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "%.1f".format(bmi),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = bmiColor
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "BMI Anda: ${"%.1f".format(bmi)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextDark
                                )
                                Text(
                                    "Kategori: $bmiCategory",
                                    fontSize = 12.sp,
                                    color = bmiColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Golongan Darah ──
                Text(
                    "Golongan Darah",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextDark
                )
                Text("Opsional", fontSize = 11.sp, color = TextLight)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("A", "B", "AB", "O").forEach { type ->
                        FilterChip(
                            selected = bloodType == type,
                            onClick = { bloodType = if (bloodType == type) "" else type },
                            label = {
                                Text(
                                    type,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldDark,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Alergi Obat ──
                Text(
                    "Ada Alergi Obat?",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextDark
                )
                Text(
                    "Opsional · Penting agar AI tidak merekomendasikan obat yang Anda alergi",
                    fontSize = 11.sp,
                    color = TextLight
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAllergyModal = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SoftSage),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Search, null,
                                tint = EmeraldDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        if (selectedAllergies.isEmpty()) {
                            Text(
                                "Contoh: Ibuprofen atau Bodrex",
                                color = TextLight,
                                fontSize = 13.sp
                            )
                        } else {
                            Text(
                                selectedAllergies.joinToString(", "),
                                fontSize = 13.sp,
                                color = EmeraldDark,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Penyakit Kronis ──
                Text(
                    "Ada Penyakit Kronis?",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextDark
                )
                Text(
                    "Opsional · Jika tidak ada, isi tanda strip (-)",
                    fontSize = 11.sp,
                    color = TextLight
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = chronicDiseases,
                    onValueChange = { if (it.length <= 2000) chronicDiseases = it },
                    placeholder = { Text("Contoh: Diabetes, Hipertensi", fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3,
                    supportingText = { Text("${chronicDiseases.length}/2000") }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ── Riwayat GERD ──
                Text(
                    "Ada Riwayat GERD?",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextDark
                )
                Text(
                    "Opsional · AI akan merekomendasikan obat aman lambung",
                    fontSize = 11.sp,
                    color = TextLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(true to "Ya", false to "Tidak").forEach { (value, label) ->
                        FilterChip(
                            selected = hasGerd == value,
                            onClick = { hasGerd = if (hasGerd == value) null else value },
                            label = { Text(label, fontWeight = FontWeight.Medium) },
                            leadingIcon = if (hasGerd == value) {
                                {
                                    Icon(
                                        Icons.Default.Check, null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldDark,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Catatan Tambahan ──
                Text(
                    "Catatan Tambahan",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextDark
                )
                Text("Opsional", fontSize = 11.sp, color = TextLight)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = additionalNotes,
                    onValueChange = { if (it.length <= 2000) additionalNotes = it },
                    placeholder = {
                        Text(
                            "Informasi lain yang perlu diketahui...",
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Success Banner ──
                AnimatedVisibility(visible = showSuccess) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftSage),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, EmeraldLight.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldDark.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✅", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Informasi medis berhasil disimpan!",
                                fontWeight = FontWeight.Medium,
                                color = EmeraldDark,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // ── Submit Button ──
                Button(
                    onClick = {
                        val data = mapOf(
                            "weight_kg" to (weightKg.toDoubleOrNull() ?: 0.0),
                            "height_cm" to (heightCm.toDoubleOrNull() ?: 0.0),
                            "blood_type" to bloodType,
                            "chronic_diseases" to chronicDiseases,
                            "has_gerd" to (hasGerd ?: false),
                            "additional_notes" to additionalNotes,
                            "drug_allergies" to selectedAllergies.toList()
                        )
                        
                        viewModel.updateProfile(data) {
                            showSuccess = true
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(3000)
                                showSuccess = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Simpan", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Allergy Modal
        if (showAllergyModal) {
            DrugAllergyModalSheet(
                selectedAllergies = selectedAllergies,
                onDismiss = { showAllergyModal = false },
                onSave = { allergies ->
                    selectedAllergies.clear()
                    selectedAllergies.addAll(allergies)
                    showAllergyModal = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugAllergyModalSheet(
    selectedAllergies: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val tempSelected = remember { mutableStateListOf<String>().apply { addAll(selectedAllergies) } }

    val allDrugs = listOf(
        "Tidak ada alergi", "Ibuprofen", "Aspirin", "Paracetamol", "Amoxicillin",
        "Penisilin", "Sulfonamida", "Cephalosporin", "Codeine", "Naproxen",
        "Diclofenac", "Piroxicam", "Mefenamic Acid", "Tramadol", "Morphine",
        "Metformin", "Glibenclamide", "Captopril", "Amlodipine", "Simvastatin",
        "Omeprazole", "Ranitidine", "Ciprofloxacin", "Azithromycin", "Dexamethasone",
        "Prednisone", "Cetirizine", "Loratadine", "Diphenhydramine", "Salbutamol",
        "Tolak Angin", "Bodrex", "Mixagrip", "Decolgen", "Promag",
        "Antangin", "Komix", "OBH Combi", "Vicks Formula 44", "Neurobion"
    )

    val filteredDrugs = if (searchQuery.isBlank()) allDrugs
    else allDrugs.filter { it.contains(searchQuery, ignoreCase = true) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                "Ada Alergi Obat?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama obat...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextLight) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .heightIn(max = 350.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                filteredDrugs.forEach { drug ->
                    val isSelected = tempSelected.contains(drug)
                    val isNone = drug == "Tidak ada alergi"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isNone) {
                                    tempSelected.clear()
                                    tempSelected.add(drug)
                                } else {
                                    tempSelected.remove("Tidak ada alergi")
                                    if (isSelected) tempSelected.remove(drug) else tempSelected.add(drug)
                                }
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(checkedColor = EmeraldDark)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            drug,
                            fontSize = 14.sp,
                            fontWeight = if (isNone) FontWeight.Bold else FontWeight.Normal,
                            color = if (isNone) TextLight else TextDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Batal", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        val result = if (tempSelected.contains("Tidak ada alergi")) emptyList() else tempSelected.toList()
                        onSave(result)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark)
                ) {
                    Text("Simpan & Tutup", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
