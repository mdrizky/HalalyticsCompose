package com.example.halalyticscompose.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.halalyticscompose.ui.theme.*
import com.example.halalyticscompose.ui.components.MedicalAiDisclaimerBanner
import com.example.halalyticscompose.ui.viewmodel.MedicineViewModel
import com.example.halalyticscompose.utils.VoiceRecognitionHelper
import com.example.halalyticscompose.utils.TextToSpeechHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.res.stringResource
import com.example.halalyticscompose.R
import com.example.halalyticscompose.data.model.MedicineData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthAssistantScreen(
    navController: NavController,
    initialSymptom: String? = null
) {
    val context = LocalContext.current
    val viewModel: MedicineViewModel = hiltViewModel()
    var symptoms by remember { mutableStateOf(initialSymptom ?: "") }
    var showResults by remember { mutableStateOf(false) }
    var selectedMedicine by remember { mutableStateOf<MedicineData?>(null) }
    var showReminderDialog by remember { mutableStateOf(false) }

    // Voice Note STT/TTS state
    var isListening by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var voiceStatus by remember { mutableStateOf("") }
    val voiceHelper = remember { VoiceRecognitionHelper(context) }
    val ttsHelper = remember {
        TextToSpeechHelper(context).also { helper ->
            helper.onSpeakingStarted = { isSpeaking = true }
            helper.onSpeakingDone = { isSpeaking = false }
        }
    }

    // Mic pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.35f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Runtime permission launcher for RECORD_AUDIO
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isListening = true
            voiceStatus = context.getString(R.string.assistant_mic_listening)
            voiceHelper.startListening(
                onResult = { result ->
                    symptoms = result
                    isListening = false
                    voiceStatus = context.getString(R.string.assistant_voice_detected)
                    // Auto-trigger analysis for better UX
                    if (result.isNotBlank()) {
                        viewModel.analyzeSymptoms(result)
                        showResults = true
                    }
                },
                onError = { msg ->
                    isListening = false
                    voiceStatus = msg
                },
                onPartial = { partial -> symptoms = partial },
                onListeningStarted = { isListening = true },
                onListeningEnded = { isListening = false }
            )
        } else {
            Toast.makeText(context, context.getString(R.string.assistant_mic_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    // Auto-read AI response with TTS
    val symptomsAnalysis by viewModel.symptomsAnalysis.collectAsState()
    LaunchedEffect(symptomsAnalysis) {
        symptomsAnalysis?.let { analysis ->
            ttsHelper.speakDiagnosisReport(
                condition = analysis.condition,
                severity = analysis.severity,
                causes = analysis.possible_causes,
                recommendation = analysis.lifestyle_advice ?: analysis.recommendation,
                medicines = analysis.recommended_medicines_list,
                shouldSeeDoctor = analysis.should_seek_doctor
            )
        }
    }

    val recommendedMedicines by viewModel.recommendedMedicines.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            voiceHelper.destroy()
            ttsHelper.shutdown()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadUserReminders()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.assistant_title), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier.padding(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(0.05f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate("medicine_reminders") },
                        modifier = Modifier.padding(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(0.05f))
                    ) {
                        Icon(Icons.Outlined.NotificationsActive, "Reminders", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            // Sticky bottom bar — Chat Dokter
            if (showResults && symptomsAnalysis != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 12.dp,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(
                            stringResource(R.string.assistant_chat_doctor_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            stringResource(R.string.assistant_chat_doctor_desc),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { navController.navigate("halocode") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00897B)
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.assistant_chat_doctor_button),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Icon(
                        Icons.Default.MedicalServices, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.onPrimary, 
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.assistant_hero_question), 
                        style = MaterialTheme.typography.headlineSmall, 
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.health_assistant_welcome),
                        style = MaterialTheme.typography.bodySmall, 
                        color = MaterialTheme.colorScheme.onPrimary.copy(0.8f),
                        lineHeight = 18.sp
                    )
                }
            }

            MedicalAiDisclaimerBanner(
                modifier = Modifier.padding(horizontal = 24.dp),
                compact = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Input Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.05f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    OutlinedTextField(
                        value = symptoms,
                        onValueChange = { symptoms = it },
                        placeholder = { Text(stringResource(R.string.health_assistant_placeholder), color = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 6,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(0.1f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    // Voice status indicator
                    if (voiceStatus.isNotEmpty()) {
                        Text(
                            text = voiceStatus,
                            fontSize = 11.sp,
                            color = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(0.5f),
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Voice Note + Analyze buttons row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 🎤 Mic Button with pulse animation
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .scale(if (isListening) pulseScale else 1f)
                                .clip(CircleShape)
                                .background(
                                    if (isListening) Color(0xFFE53935)
                                    else MaterialTheme.colorScheme.primary.copy(0.15f)
                                )
                                .clickable {
                                    if (isListening) {
                                        voiceHelper.stopListening()
                                        isListening = false
                                        voiceStatus = context.getString(R.string.assistant_mic_stopped)
                                    } else {
                                        // Stop TTS if it's speaking
                                        ttsHelper.stop()
                                        // Check permission first
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                            isListening = true
                                            voiceStatus = context.getString(R.string.assistant_mic_listening)
                                            voiceHelper.startListening(
                                                onResult = { result ->
                                                    symptoms = result
                                                    isListening = false
                                                    voiceStatus = context.getString(R.string.assistant_voice_detected)
                                                    // Auto-trigger analysis for better UX
                                                    if (result.isNotBlank()) {
                                                        viewModel.analyzeSymptoms(result)
                                                        showResults = true
                                                    }
                                                },
                                                onError = { msg ->
                                                    isListening = false
                                                    voiceStatus = msg
                                                },
                                                onPartial = { partial -> symptoms = partial },
                                                onListeningStarted = { isListening = true },
                                                onListeningEnded = { isListening = false }
                                            )
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isListening) "Stop" else "Voice Note",
                                tint = if (isListening) Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Analyze button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (symptoms.isNotBlank() && !isLoading) 
                                        Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                                    else 
                                        Brush.linearGradient(listOf(MaterialTheme.colorScheme.onSurface.copy(0.1f), MaterialTheme.colorScheme.onSurface.copy(0.05f)))
                                )
                                .clickable(enabled = symptoms.isNotBlank() && !isLoading) {
                                    ttsHelper.stop()
                                    viewModel.analyzeSymptoms(symptoms)
                                    showResults = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(stringResource(R.string.common_analyze), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // 🔊 TTS Toggle button
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSpeaking) MaterialTheme.colorScheme.secondary.copy(0.2f)
                                    else MaterialTheme.colorScheme.onSurface.copy(0.05f)
                                )
                                .clickable {
                                    if (isSpeaking) {
                                        ttsHelper.stop()
                                        isSpeaking = false
                                    } else {
                                        // Re-read the last analysis (full report)
                                        symptomsAnalysis?.let { analysis ->
                                            ttsHelper.speakDiagnosisReport(
                                                condition = analysis.condition,
                                                severity = analysis.severity,
                                                causes = analysis.possible_causes,
                                                recommendation = analysis.lifestyle_advice ?: analysis.recommendation,
                                                medicines = analysis.recommended_medicines_list,
                                                shouldSeeDoctor = analysis.should_seek_doctor
                                            )
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isSpeaking) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "TTS",
                                tint = if (isSpeaking) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(0.5f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.10f))
                        .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Results Section
            val analysis = symptomsAnalysis
            if (showResults && analysis != null) {
                // TTS indicator
                if (isSpeaking) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.assistant_tts_reading), fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    // Emergency Alert
                    if (analysis.emergency_warning != null || analysis.severity.equals("emergency", ignoreCase = true)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.error.copy(0.1f))
                                .border(1.dp, MaterialTheme.colorScheme.error.copy(0.3f), RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.GppBad, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(stringResource(R.string.assistant_urgent_warning), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                    Text(analysis.emergency_warning ?: stringResource(R.string.assistant_doctor_advice), color = MaterialTheme.colorScheme.onSurface.copy(0.8f), fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { navController.navigate("emergency_p3k") },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Default.LocalHospital, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.assistant_open_first_aid))
                                    }
                                }
                            }
                        }
                    }

                    // Analysis Report Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.05f), RoundedCornerShape(28.dp))
                            .padding(24.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(stringResource(R.string.assistant_diagnosis_report), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(analysis.condition, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val severityColor = when (analysis.severity.lowercase()) {
                                "mild" -> MaterialTheme.colorScheme.primary
                                "moderate" -> MushboohYellow
                                else -> MaterialTheme.colorScheme.error
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(severityColor.copy(0.1f))
                                    .border(1.dp, severityColor.copy(0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    analysis.severity_label ?: analysis.severity.uppercase(),
                                    color = severityColor,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            if (!analysis.confidence_level.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    stringResource(R.string.assistant_confidence_level) + " ${analysis.confidence_level}",
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.65f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // === Profil Pasien yang Dibaca AI ===
                            if (!analysis.profil_pasien_dibaca.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF00897B).copy(0.08f))
                                        .border(1.dp, Color(0xFF00897B).copy(0.16f), RoundedCornerShape(14.dp))
                                        .padding(14.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.PersonSearch, null, tint = Color(0xFF00897B), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                stringResource(R.string.assistant_profile_read_title),
                                                color = Color(0xFF00897B),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            analysis.profil_pasien_dibaca!!,
                                            color = MaterialTheme.colorScheme.onSurface.copy(0.85f),
                                            fontSize = 13.sp,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }

                            // === Catatan Lokasi ===
                            if (!analysis.catatan_lokasi.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF5C6BC0).copy(0.08f))
                                        .border(1.dp, Color(0xFF5C6BC0).copy(0.16f), RoundedCornerShape(14.dp))
                                        .padding(14.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocationOn, null, tint = Color(0xFF5C6BC0), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                stringResource(R.string.assistant_location_note_title),
                                                color = Color(0xFF5C6BC0),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            analysis.catatan_lokasi!!,
                                            color = MaterialTheme.colorScheme.onSurface.copy(0.85f),
                                            fontSize = 13.sp,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }

                            if (!analysis.ringkasan_keluhan.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.secondary.copy(0.08f))
                                        .border(1.dp, MaterialTheme.colorScheme.secondary.copy(0.16f), RoundedCornerShape(14.dp))
                                        .padding(14.dp)
                                ) {
                                    Column {
                                        Text(
                                            stringResource(R.string.assistant_symptom_summary),
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            analysis.ringkasan_keluhan!!,
                                            color = MaterialTheme.colorScheme.onSurface.copy(0.85f),
                                            fontSize = 13.sp,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.05f))
                            Spacer(modifier = Modifier.height(24.dp))

                            // 1. Akar Masalah (Why it happened)
                            if (!analysis.why_it_happened.isNullOrBlank()) {
                                SectionTitle(stringResource(R.string.assistant_why_happened), Icons.Outlined.Science, MaterialTheme.colorScheme.primary)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(0.05f))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = analysis.why_it_happened!!,
                                        color = MaterialTheme.colorScheme.onSurface.copy(0.85f),
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                            }

                            // 2. Gejala Terkait
                            if (analysis.gejala_terkait.isNotEmpty() || analysis.possible_causes.isNotEmpty()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    if (analysis.gejala_terkait.isNotEmpty()) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            SectionTitle(stringResource(R.string.assistant_related_symptoms), Icons.Outlined.MonitorHeart, MaterialTheme.colorScheme.secondary)
                                            analysis.gejala_terkait.forEach { symptom ->
                                                Text("• $symptom", color = MaterialTheme.colorScheme.onSurface.copy(0.75f), fontSize = 13.sp, lineHeight = 20.sp)
                                            }
                                        }
                                    }
                                    if (analysis.possible_causes.isNotEmpty()) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            SectionTitle(stringResource(R.string.assistant_potential_causes), Icons.Outlined.Lightbulb, MaterialTheme.colorScheme.primary)
                                            analysis.possible_causes.forEach { cause ->
                                                Text("• $cause", color = MaterialTheme.colorScheme.onSurface.copy(0.75f), fontSize = 13.sp, lineHeight = 20.sp)
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.05f))
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            if (analysis.possible_causes_detailed.isNotEmpty()) {
                                SectionTitle(stringResource(R.string.assistant_cause_analysis), Icons.Outlined.Science, MaterialTheme.colorScheme.primary)
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    analysis.possible_causes_detailed.forEach { cause ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(0.04f))
                                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(0.10f), RoundedCornerShape(14.dp))
                                                .padding(14.dp)
                                        ) {
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        cause.name,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    cause.percentage?.let {
                                                        Text(
                                                            "$it%",
                                                            color = MaterialTheme.colorScheme.primary,
                                                            fontWeight = FontWeight.Black
                                                        )
                                                    }
                                                }
                                                if (!cause.reason.isNullOrBlank()) {
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        cause.reason!!,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(0.75f),
                                                        fontSize = 13.sp,
                                                        lineHeight = 19.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.05f))
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            if (analysis.disease_explanations.isNotEmpty()) {
                                SectionTitle(stringResource(R.string.assistant_main_disease_explanation), Icons.Outlined.Description, MaterialTheme.colorScheme.primary)
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    analysis.disease_explanations.forEach { disease ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(MaterialTheme.colorScheme.onSurface.copy(0.03f))
                                                .padding(14.dp)
                                        ) {
                                            Column {
                                                Text(disease.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                if (!disease.description.isNullOrBlank()) {
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(disease.description!!, color = MaterialTheme.colorScheme.onSurface.copy(0.8f), fontSize = 13.sp, lineHeight = 19.sp)
                                                }
                                                if (!disease.relation_to_case.isNullOrBlank()) {
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(stringResource(R.string.assistant_relation_to_case) + " ${disease.relation_to_case}", color = MaterialTheme.colorScheme.onSurface.copy(0.7f), fontSize = 12.sp, lineHeight = 18.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.05f))
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            if (analysis.trigger_factors.isNotEmpty()) {
                                SectionTitle(stringResource(R.string.assistant_personal_triggers), Icons.Outlined.WarningAmber, MaterialTheme.colorScheme.error)
                                BulletListCard(items = analysis.trigger_factors)
                                Spacer(modifier = Modifier.height(24.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.05f))
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            // 3. Saran Gaya Hidup & Pencegahan
                            if (!analysis.lifestyle_advice.isNullOrBlank() || !analysis.future_prevention.isNullOrBlank()) {
                                SectionTitle(stringResource(R.string.assistant_handling_prevention), Icons.Outlined.Spa, MaterialTheme.colorScheme.secondary)
                                if (!analysis.lifestyle_advice.isNullOrBlank()) {
                                    Text(stringResource(R.string.assistant_initial_handling), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                    Text(analysis.lifestyle_advice!!, color = MaterialTheme.colorScheme.onSurface.copy(0.75f), fontSize = 13.sp, lineHeight = 20.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                if (!analysis.future_prevention.isNullOrBlank()) {
                                    Text(stringResource(R.string.assistant_future_prevention), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                    Text(analysis.future_prevention!!, color = MaterialTheme.colorScheme.onSurface.copy(0.75f), fontSize = 13.sp, lineHeight = 20.sp)
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.05f))
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            // === OBAT HERBAL & TRADISIONAL (SEBELUM OBAT APOTEK) ===
                            if (analysis.herbal_remedies.isNotEmpty()) {
                                SectionTitle(stringResource(R.string.assistant_herbal_title), Icons.Outlined.Spa, Color(0xFF2E7D32))
                                Text(
                                    stringResource(R.string.assistant_herbal_subtitle),
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    analysis.herbal_remedies.forEach { herbal ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color(0xFF2E7D32).copy(0.06f))
                                                .border(1.dp, Color(0xFF2E7D32).copy(0.14f), RoundedCornerShape(14.dp))
                                                .padding(14.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Eco, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(herbal.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                                                }
                                                if (!herbal.description.isNullOrBlank()) {
                                                    Text(herbal.description!!, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.8f), lineHeight = 18.sp)
                                                }
                                                if (!herbal.how_to_prepare.isNullOrBlank()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(stringResource(R.string.assistant_herbal_prepare), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                                                    Text(herbal.how_to_prepare!!, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.8f), lineHeight = 18.sp)
                                                }
                                                if (!herbal.how_to_use.isNullOrBlank()) {
                                                    Text(stringResource(R.string.assistant_herbal_use), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                                                    Text(herbal.how_to_use!!, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.8f), lineHeight = 18.sp)
                                                }
                                                if (!herbal.frequency.isNullOrBlank()) {
                                                    Row {
                                                        Icon(Icons.Default.AccessTime, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(stringResource(R.string.assistant_herbal_frequency) + " ${herbal.frequency}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
                                                    }
                                                }
                                                if (!herbal.duration.isNullOrBlank()) {
                                                    Row {
                                                        Icon(Icons.Default.DateRange, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(stringResource(R.string.assistant_herbal_duration) + " ${herbal.duration}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
                                                    }
                                                }
                                                if (!herbal.precautions.isNullOrBlank()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Row(verticalAlignment = Alignment.Top) {
                                                        Icon(Icons.Outlined.WarningAmber, null, tint = Color(0xFFE65100), modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(stringResource(R.string.assistant_herbal_precaution) + " ${herbal.precautions}", fontSize = 11.sp, color = Color(0xFFE65100), lineHeight = 16.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.05f))
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            // 4. Rekomendasi Terapi (Obat Paten & Generik)
                            if (analysis.recommended_medicines_list.isNotEmpty() || analysis.alternative_medicines.isNotEmpty()) {
                                SectionTitle(stringResource(R.string.assistant_therapy_recommendation), Icons.Outlined.Medication, MaterialTheme.colorScheme.primary)
                                
                                if (analysis.medicine_categories.isNotEmpty()) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                        analysis.medicine_categories.forEach { category ->
                                            Box(
                                                modifier = Modifier
                                                    .padding(end = 8.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.primary)
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(category, color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    if (analysis.recommended_medicines_list.isNotEmpty()) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(stringResource(R.string.assistant_pharmacy_medicine), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, modifier = Modifier.padding(bottom = 4.dp))
                                            analysis.recommended_medicines_list.forEach { med ->
                                                Text("• $med", color = MaterialTheme.colorScheme.onSurface.copy(0.8f), fontSize = 13.sp)
                                            }
                                        }
                                    }
                                    if (analysis.alternative_medicines.isNotEmpty()) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(stringResource(R.string.assistant_herbal_alternative), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, modifier = Modifier.padding(bottom = 4.dp))
                                            analysis.alternative_medicines.forEach { alt ->
                                                Text("• $alt", color = MaterialTheme.colorScheme.onSurface.copy(0.8f), fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }

                                if (analysis.recommended_medicine_details.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        analysis.recommended_medicine_details.forEach { med ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(MaterialTheme.colorScheme.primary.copy(0.04f))
                                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(0.10f), RoundedCornerShape(14.dp))
                                                    .padding(14.dp)
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(med.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                    if (!med.function.isNullOrBlank()) {
                                                        Text(stringResource(R.string.assistant_med_function) + " ${med.function}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
                                                    }
                                                    if (!med.dosage.isNullOrBlank()) {
                                                        Text(stringResource(R.string.assistant_med_dosage) + " ${med.dosage}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
                                                    }
                                                    if (!med.when_to_take.isNullOrBlank()) {
                                                        Text(stringResource(R.string.assistant_med_time) + " ${med.when_to_take}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
                                                    }
                                                    if (!med.how_to_take.isNullOrBlank()) {
                                                        Text(stringResource(R.string.assistant_med_how_to_take) + " ${med.how_to_take}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
                                                    }
                                                    if (!med.duration.isNullOrBlank()) {
                                                        Text(stringResource(R.string.assistant_med_duration) + " ${med.duration}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
                                                    }
                                                    if (!med.halal_status.isNullOrBlank()) {
                                                        Text(stringResource(R.string.assistant_med_halal_status) + " ${med.halal_status}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                                    }
                                                    if (!med.price_range.isNullOrBlank()) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Default.Payments, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(stringResource(R.string.assistant_med_price_est) + " ${med.price_range}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                                        }
                                                    }
                                                    if (!med.safety_note.isNullOrBlank()) {
                                                        Text(stringResource(R.string.assistant_med_safety_note) + " ${med.safety_note}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.75f))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // Halal Verification Sub-box
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (analysis.halal_check?.status?.lowercase() == "halal") MaterialTheme.colorScheme.secondary.copy(0.1f) else MushboohYellow.copy(0.1f))
                                        .border(1.dp, if (analysis.halal_check?.status?.lowercase() == "halal") MaterialTheme.colorScheme.secondary.copy(0.3f) else MushboohYellow.copy(0.3f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.VerifiedUser, null, tint = if (analysis.halal_check?.status?.lowercase() == "halal") MaterialTheme.colorScheme.secondary else MushboohYellow, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(stringResource(R.string.assistant_halal_verification) + " ${analysis.halal_check?.status?.uppercase() ?: "UNKNOWN"}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                            Text(analysis.halal_check?.notes ?: "...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            // 5. Kartu Dosis & Efek Samping
                            if (!analysis.dosage_guidelines.isNullOrBlank() || !analysis.when_to_take_and_frequency.isNullOrBlank() || analysis.side_effects.isNotEmpty()) {
                                SectionTitle(stringResource(R.string.assistant_usage_rules), Icons.Outlined.Info, MaterialTheme.colorScheme.primary)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.onSurface.copy(0.03f))
                                        .padding(16.dp)
                                ) {
                                    Column {
                                        if (!analysis.dosage_guidelines.isNullOrBlank()) {
                                            Row(modifier = Modifier.padding(bottom = 8.dp)) {
                                                Icon(Icons.Outlined.Scale, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(stringResource(R.string.assistant_med_dosage) + " ${analysis.dosage_guidelines}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.9f))
                                            }
                                        }
                                        if (!analysis.when_to_take_and_frequency.isNullOrBlank()) {
                                            Row(modifier = Modifier.padding(bottom = 8.dp)) {
                                                Icon(Icons.Default.AccessTime, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Waktu: ${analysis.when_to_take_and_frequency}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.9f), fontWeight = FontWeight.Medium)
                                            }
                                        }
                                        if (!analysis.usage_instructions.isNullOrBlank()) {
                                            Row(modifier = Modifier.padding(bottom = 8.dp)) {
                                                Icon(Icons.Outlined.IntegrationInstructions, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(stringResource(R.string.assistant_med_instructions) + " ${analysis.usage_instructions}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.9f))
                                            }
                                        }
                                        if (analysis.side_effects.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.Top) {
                                                Icon(Icons.Outlined.WarningAmber, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(stringResource(R.string.assistant_side_effects), fontSize = 13.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                                                    analysis.side_effects.forEach { effect ->
                                                        Text("• $effect", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            if (!analysis.drug_mechanism.isNullOrBlank()) {
                                SectionTitle(stringResource(R.string.assistant_drug_mechanism), Icons.Outlined.Science, MaterialTheme.colorScheme.primary)
                                Text(analysis.drug_mechanism!!, color = MaterialTheme.colorScheme.onSurface.copy(0.8f), fontSize = 13.sp, lineHeight = 20.sp)
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            if (analysis.first_aid_steps.isNotEmpty()) {
                                SectionTitle(stringResource(R.string.assistant_immediate_action), Icons.Default.LocalHospital, MaterialTheme.colorScheme.error)
                                BulletListCard(items = analysis.first_aid_steps)
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            if (analysis.prevention.isNotEmpty()) {
                                SectionTitle(stringResource(R.string.assistant_future_prevention_title), Icons.Outlined.Spa, MaterialTheme.colorScheme.secondary)
                                BulletListCard(items = analysis.prevention)
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            if (analysis.follow_up_questions.isNotEmpty()) {
                                SectionTitle(stringResource(R.string.assistant_follow_up_questions), Icons.AutoMirrored.Outlined.HelpOutline, MaterialTheme.colorScheme.primary)
                                BulletListCard(items = analysis.follow_up_questions)
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            if (!analysis.tldr.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(0.08f))
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(0.16f), RoundedCornerShape(14.dp))
                                        .padding(14.dp)
                                ) {
                                    Column {
                                        Text(stringResource(R.string.assistant_tldr), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(analysis.tldr!!, color = MaterialTheme.colorScheme.onSurface.copy(0.82f), fontSize = 13.sp, lineHeight = 19.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.05f))
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // 6. Penutup / Rekomendasi Dokter
                            if (!analysis.triage_action.isNullOrBlank() || analysis.should_seek_doctor) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (analysis.should_seek_doctor) MaterialTheme.colorScheme.error.copy(0.10f)
                                            else MaterialTheme.colorScheme.primary.copy(0.08f)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(
                                            if (analysis.should_seek_doctor) stringResource(R.string.assistant_doctor_advice) else stringResource(R.string.assistant_condition_monitoring),
                                            fontWeight = FontWeight.Bold,
                                            color = if (analysis.should_seek_doctor) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            analysis.doctor_recommendation ?: analysis.triage_action ?: analysis.recommendation,
                                            color = MaterialTheme.colorScheme.onSurface.copy(0.8f),
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (analysis.should_seek_doctor) {
                                                Button(
                                                    onClick = { navController.navigate("emergency_p3k") },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                                ) {
                                                    Icon(Icons.Default.LocalHospital, null)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(stringResource(R.string.assistant_go_to_doctor))
                                                }
                                                
                                                Button(
                                                    onClick = { navController.navigate("halocode") },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                                ) {
                                                    Icon(Icons.AutoMirrored.Filled.Chat, null)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(stringResource(R.string.assistant_chat_expert))
                                                }
                                            } else {
                                                OutlinedButton(onClick = { navController.navigate("medicine_reminders") }) {
                                                    Icon(Icons.Default.Alarm, null)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(stringResource(R.string.assistant_set_reminder))
                                                }
                                                
                                                OutlinedButton(onClick = { navController.navigate("halocode") }) {
                                                    Icon(Icons.AutoMirrored.Filled.Chat, null)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(stringResource(R.string.assistant_ask_expert))
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = stringResource(R.string.assistant_disclaimer),
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    if (recommendedMedicines.isNotEmpty()) {
                        Text(stringResource(R.string.assistant_recommended_medicines), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        recommendedMedicines.forEach { medicine ->
                            MedicineCardPremium(
                                medicine = medicine,
                                onSetReminder = {
                                    selectedMedicine = medicine
                                    showReminderDialog = true
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }

    // Reuse or update ReminderDialog
    if (showReminderDialog && selectedMedicine != null) {
        ReminderDialogPremium(
            medicine = selectedMedicine!!,
            onDismiss = { showReminderDialog = false },
            onConfirm = { freq, start, end, notes ->
                viewModel.createReminder(selectedMedicine!!.idMedicine ?: 0, symptoms, freq, start, end, notes)
                showReminderDialog = false
                Toast.makeText(context, "Reminder set for ${selectedMedicine!!.name}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun SectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BulletListCard(items: List<String>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(0.03f))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { item ->
                Row(verticalAlignment = Alignment.Top) {
                    Text("•", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        item,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface.copy(0.8f),
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MedicineCardPremium(
    medicine: com.example.halalyticscompose.data.model.MedicineData,
    onSetReminder: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.05f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Medication, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(medicine.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(medicine.brandName ?: "Generic", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(0.1f))
                        .clickable { onSetReminder() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddAlarm, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.assistant_set_reminder), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("HALAL", color = MaterialTheme.colorScheme.onPrimary, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDialogPremium(
    medicine: com.example.halalyticscompose.data.model.MedicineData,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, String?, String?) -> Unit
) {
    var frequency by remember { mutableStateOf("3") }
    var startDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var notes by remember { mutableStateOf("") }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.05f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(stringResource(R.string.assistant_schedule_dose), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(medicine.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = frequency,
                    onValueChange = { frequency = it },
                    label = { Text(stringResource(R.string.assistant_doses_per_day)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(0.1f))
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text(stringResource(R.string.assistant_start_date)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(0.1f))
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onConfirm(frequency.toIntOrNull() ?: 3, startDate, null, notes) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.assistant_save_reminder), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
