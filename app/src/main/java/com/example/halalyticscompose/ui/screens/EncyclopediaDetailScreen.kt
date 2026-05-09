package com.example.halalyticscompose.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.halalyticscompose.data.model.HealthEncyclopedia
import com.example.halalyticscompose.ui.theme.*
import com.example.halalyticscompose.utils.TextToSpeechHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncyclopediaDetailScreen(
    navController: NavController,
    item: HealthEncyclopedia
) {
    val context = LocalContext.current
    var isSpeaking by remember { mutableStateOf(false) }
    val ttsHelper = remember {
        TextToSpeechHelper(context).also { helper ->
            helper.onSpeakingStarted = { isSpeaking = true }
            helper.onSpeakingDone = { isSpeaking = false }
        }
    }

    DisposableEffect(Unit) {
        onDispose { ttsHelper.shutdown() }
    }

    // Type config
    val typeColor = when (item.type.lowercase()) {
        "obat" -> Color(0xFF1E88E5)
        "penyakit" -> Color(0xFF43A047)
        "hidup_sehat" -> Color(0xFFFF8F00)
        "keluarga" -> Color(0xFFE91E63)
        else -> MaterialTheme.colorScheme.primary
    }
    val typeLabel = when (item.type.lowercase()) {
        "obat" -> "Obat"
        "penyakit" -> "Penyakit"
        "hidup_sehat" -> "Hidup Sehat"
        "keluarga" -> "Keluarga"
        else -> item.type
    }
    val typeIcon = when (item.type.lowercase()) {
        "obat" -> Icons.Default.Medication
        "penyakit" -> Icons.Default.LocalHospital
        "hidup_sehat" -> Icons.Default.FavoriteBorder
        "keluarga" -> Icons.Default.FamilyRestroom
        else -> Icons.Default.Info
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(typeLabel, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(0.05f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // TTS toggle
                    IconButton(
                        onClick = {
                            if (isSpeaking) {
                                ttsHelper.stop()
                                isSpeaking = false
                            } else {
                                val speech = buildString {
                                    append("${item.title}. ")
                                    item.summary?.let { append("$it ") }
                                    item.content?.take(500)?.let { append(it) }
                                }
                                ttsHelper.speak(speech)
                            }
                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSpeaking) typeColor.copy(0.15f)
                                else MaterialTheme.colorScheme.onSurface.copy(0.05f)
                            )
                    ) {
                        Icon(
                            if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            "Read aloud",
                            tint = if (isSpeaking) typeColor else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(typeColor, typeColor.copy(alpha = 0.7f))
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    // Type badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                typeIcon, null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                typeLabel,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        item.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    item.summary?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(0.85f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TTS indicator
            if (isSpeaking) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(typeColor.copy(0.08f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.VolumeUp, null,
                        tint = typeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Sedang membacakan artikel...",
                        fontSize = 12.sp,
                        color = typeColor
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Content card
            item.content?.let { content ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface.copy(0.05f),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp)
                ) {
                    // Parse and render content sections
                    val sections = content.split("\n\n")
                    Column {
                        sections.forEachIndexed { index, section ->
                            if (section.startsWith("**") && section.contains(":**")) {
                                // Section header
                                val headerText = section
                                    .substringBefore(":**")
                                    .removePrefix("**")
                                Text(
                                    headerText,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = typeColor,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                // Section body
                                val body = section
                                    .substringAfter(":**")
                                    .trim()
                                if (body.isNotEmpty()) {
                                    val lines = body.split("\n")
                                    lines.forEach { line ->
                                        val trimmedLine = line.trim()
                                        if (trimmedLine.startsWith("- ")) {
                                            Row(
                                                modifier = Modifier.padding(
                                                    start = 8.dp,
                                                    bottom = 4.dp
                                                )
                                            ) {
                                                Text(
                                                    "•",
                                                    color = typeColor,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(end = 8.dp)
                                                )
                                                Text(
                                                    trimmedLine.removePrefix("- "),
                                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                                        0.8f
                                                    ),
                                                    fontSize = 14.sp,
                                                    lineHeight = 20.sp
                                                )
                                            }
                                        } else if (trimmedLine.isNotEmpty()) {
                                            Text(
                                                trimmedLine,
                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                    0.8f
                                                ),
                                                fontSize = 14.sp,
                                                lineHeight = 20.sp,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Regular text paragraph
                                Text(
                                    section.replace("**", "").trim(),
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.85f),
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            if (index < sections.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.04f),
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Personalization button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .clickable {
                        // Navigate to AI analysis with this topic pre-filled
                        val encodedSymptom = java.net.URLEncoder.encode("Saya ingin tahu lebih lanjut tentang ${item.title}", "UTF-8")
                        navController.navigate("health_assistant?symptom=$encodedSymptom")
                    }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome, null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "Tanya AI tentang ${item.title}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Dapatkan saran personal berdasarkan profil kesehatan Anda",
                            color = MaterialTheme.colorScheme.onPrimary.copy(0.8f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Source link
            item.source_link?.let { link ->
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(0.03f))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                            context.startActivity(intent)
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.OpenInNew, null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Sumber: Alodokter.com",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                }
            }

            // Disclaimer
            Text(
                "Disclaimer: Informasi ini bersifat edukasi dan bukan pengganti konsultasi medis profesional.",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
