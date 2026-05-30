package com.example.halalyticscompose.ui.screens

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.halalyticscompose.R
import com.example.halalyticscompose.ui.viewmodel.MedicineViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Color Constants moved to theme-aware components
private val EmeraldDark = Color(0xFF004D40)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineReminderScreen(
    navController: NavController,
    viewModel: MedicineViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Get selected medicine from search
    val selectedName = navController.currentBackStackEntry
        ?.savedStateHandle?.get<String>("selected_medicine_name") ?: ""
    val selectedId = navController.currentBackStackEntry
        ?.savedStateHandle?.get<Int>("selected_medicine_id") ?: 0
    val selectedDose = navController.currentBackStackEntry
        ?.savedStateHandle?.get<String>("selected_medicine_dose") ?: "1.0"
    val selectedDoseUnit = navController.currentBackStackEntry
        ?.savedStateHandle?.get<String>("selected_medicine_dose_unit") ?: "tablet"

    var medicineName by remember { mutableStateOf(selectedName) }
    var medicineId by remember { mutableIntStateOf(selectedId) }
    var frequencyPerDay by remember { mutableIntStateOf(3) }
    var durationDays by remember { mutableIntStateOf(7) }
    var doseAmount by remember { mutableStateOf(selectedDose) }
    var doseUnit by remember { mutableStateOf(selectedDoseUnit) }
    var customInstruction by remember { mutableStateOf("") }
    var showDosageAccordion by remember { mutableStateOf(false) }
    var showInstructionAccordion by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Listen for save result
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Auto-calculate notification times
    val notificationTimes = remember(frequencyPerDay) {
        mutableStateListOf<String>().apply {
            clear()
            when (frequencyPerDay) {
                1 -> addAll(listOf("08:00"))
                2 -> addAll(listOf("08:00", "20:00"))
                3 -> addAll(listOf("07:00", "13:00", "19:00"))
                4 -> addAll(listOf("07:00", "11:00", "15:00", "19:00"))
            }
        }
    }

    // Refresh name & ID when returning from search — using DisposableEffect to avoid memory leak
    DisposableEffect(Unit) {
        val nameHandle = navController.currentBackStackEntry?.savedStateHandle
        val nameObserver = Observer<String> { name ->
            if (!name.isNullOrBlank()) medicineName = name
        }
        val idObserver = Observer<Int> { id ->
            if (id > 0) medicineId = id
        }
        nameHandle?.getLiveData<String>("selected_medicine_name")?.observeForever(nameObserver)
        nameHandle?.getLiveData<Int>("selected_medicine_id")?.observeForever(idObserver)

        onDispose {
            nameHandle?.getLiveData<String>("selected_medicine_name")?.removeObserver(nameObserver)
            nameHandle?.getLiveData<Int>("selected_medicine_id")?.removeObserver(idObserver)
        }
    }

    // Navigate back after successful save
    LaunchedEffect(errorMessage, isLoading) {
        if (isSaving && !isLoading && errorMessage != null) {
            if (errorMessage!!.contains("successfully", ignoreCase = true)) {
                Toast.makeText(context, context.getString(R.string.medicine_reminder_save_success), Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            } else {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                isSaving = false
            }
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
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
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
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
                    Text(
                        stringResource(R.string.medicine_reminder_add_title),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.size(36.dp))
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                // ── Medicine Name Card ──
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💊", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        if (medicineName.isNotBlank()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    medicineName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 2,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { navController.navigate("medicine_search") }) {
                                Icon(
                                    Icons.Default.Edit, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            TextButton(
                                onClick = { navController.navigate("medicine_search") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Search, null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.medicine_reminder_search_placeholder),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Frekuensi ──
                Text(
                    stringResource(R.string.medicine_reminder_freq_label),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(stringResource(R.string.medicine_reminder_freq_hint), fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    (1..4).forEach { freq ->
                        FilterChip(
                            selected = frequencyPerDay == freq,
                            onClick = { frequencyPerDay = freq },
                            label = {
                                Text(
                                    "${freq}x",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Notification preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Notifications, null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.medicine_reminder_notif_at, notificationTimes.joinToString(", ")),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Editable times
                Spacer(modifier = Modifier.height(8.dp))
                notificationTimes.forEachIndexed { index, time ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                val parts = time.split(":")
                                TimePickerDialog(context, { _, h, m ->
                                    notificationTimes[index] = String.format("%02d:%02d", h, m)
                                }, parts[0].toInt(), parts[1].toInt(), true).show()
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏰", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.medicine_reminder_schedule_label, index + 1),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            time,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Edit, null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Durasi ──
                Text(
                    stringResource(R.string.medicine_reminder_duration_label),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(stringResource(R.string.medicine_reminder_freq_hint), fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(3, 5, 7, 14, 30).forEach { days ->
                        FilterChip(
                            selected = durationDays == days,
                            onClick = { durationDays = days },
                            label = {
                                Text(
                                    "${days}d",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldDark,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Text(
                    stringResource(R.string.medicine_reminder_duration_desc, durationDays),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Accordion: Petunjuk Penggunaan ──
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDosageAccordion = !showDosageAccordion },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                 modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📋", fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                             Text(
                                stringResource(R.string.medicine_reminder_usage_guide),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                             Icon(
                                if (showDosageAccordion) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                null, tint = MaterialTheme.colorScheme.outline
                            )
                        }
                        AnimatedVisibility(visible = showDosageAccordion) {
                            Column(modifier = Modifier.padding(top = 14.dp)) {
                                 Text(
                                    stringResource(R.string.medicine_reminder_dose_label),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = doseAmount,
                                        onValueChange = { doseAmount = it },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true
                                    )
                                     Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                             doseUnit,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Accordion: Cara Penggunaan ──
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showInstructionAccordion = !showInstructionAccordion },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("❓", fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                             Text(
                                stringResource(R.string.medicine_reminder_usage_method),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                             Icon(
                                if (showInstructionAccordion) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                null, tint = MaterialTheme.colorScheme.outline
                            )
                        }
                        AnimatedVisibility(visible = showInstructionAccordion) {
                            Column(modifier = Modifier.padding(top = 14.dp)) {
                                 Text(
                                    stringResource(R.string.medicine_reminder_instruction_hint),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = customInstruction,
                                    onValueChange = { customInstruction = it },
                                     placeholder = {
                                        Text(
                                            stringResource(R.string.medicine_reminder_instruction_placeholder),
                                            fontSize = 13.sp
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    maxLines = 3
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ── SAVE BUTTON ──
                Button(
                    onClick = {
                        isSaving = true
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val startDate = dateFormat.format(Date())
                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.DAY_OF_YEAR, durationDays)
                        val endDate = dateFormat.format(calendar.time)

                        viewModel.createReminder(
                            medicineId = medicineId,
                            symptoms = null,
                            frequencyPerDay = frequencyPerDay,
                            startDate = startDate,
                            endDate = endDate,
                            notes = customInstruction.takeIf { it.isNotBlank() }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                     colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    enabled = medicineName.isNotBlank() && !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(
                            Icons.Default.Save, null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                         Text(
                            stringResource(R.string.medicine_reminder_save_button),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
