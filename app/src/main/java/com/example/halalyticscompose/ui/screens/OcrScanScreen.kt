package com.example.halalyticscompose.ui.screens

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.halalyticscompose.data.ocr.DetectedIngredient
import com.example.halalyticscompose.data.ocr.OcrProcessor
import com.example.halalyticscompose.ui.theme.*
import com.example.halalyticscompose.ui.viewmodel.OcrScanViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OcrScanScreen(
    onNavigateBack: () -> Unit,
    viewModel: OcrScanViewModel = hiltViewModel(),
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) cameraPermission.launchPermissionRequest()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermission.status.isGranted) {
            CameraPreviewWithOcr(onTextDetected = viewModel::processText)
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Izin kamera diperlukan untuk scan komposisi", color = Slate700, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { cameraPermission.launchPermissionRequest() }, colors = ButtonDefaults.buttonColors(containerColor = Emerald)) {
                        Text("Izinkan Kamera")
                    }
                }
            }
        }

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            if (uiState.isSyncing) {
                Surface(color = Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(20.dp)) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Menganalisis...", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }

        // Center instruction
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("Arahkan ke daftar komposisi", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 6.dp))
        }

        // Scanner Frame
        ScannerFrameOverlay(
            hasWarning = uiState.maxSeverity >= 2,
            hasDanger = uiState.maxSeverity >= 3
        )

        // Bottom Panel
        AnimatedVisibility(
            visible = uiState.detectedIngredients.isNotEmpty() || uiState.rawText.isNotBlank(),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            OcrResultPanel(
                detectedIngredients = uiState.detectedIngredients,
                maxSeverity = uiState.maxSeverity,
                rawText = uiState.rawText
            )
        }

        if (uiState.showAlert && uiState.detectedIngredients.isNotEmpty()) {
            SmartAlertDialog(
                detectedIngredients = uiState.detectedIngredients,
                onDismiss = viewModel::dismissAlert
            )
        }
    }
}

@Composable
fun ScannerFrameOverlay(hasWarning: Boolean, hasDanger: Boolean) {
    val borderColor = when {
        hasDanger -> Error
        hasWarning -> Color(0xFFFFA000)
        else -> Color.White
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(width = 300.dp, height = 190.dp)
                .border(3.dp, borderColor, RoundedCornerShape(24.dp))
        )
        // Corner accents
        val cornerSize = 24.dp
        val lineWidth = 3.dp
        Box(modifier = Modifier.align(Alignment.TopStart).offset(x = (-8).dp, y = (-8).dp).size(cornerSize, lineWidth).background(borderColor, RoundedCornerShape(topStart = 8.dp)))
        Box(modifier = Modifier.align(Alignment.TopStart).offset(x = (-8).dp, y = (-8).dp).size(lineWidth, cornerSize).background(borderColor, RoundedCornerShape(topStart = 8.dp)))
        // ... (repeat for other corners)
    }
}

@Composable
fun OcrResultPanel(detectedIngredients: List<DetectedIngredient>, maxSeverity: Int, rawText: String) {
    val bgColor = when {
        maxSeverity >= 3 -> Error.copy(alpha = 0.95f)
        maxSeverity >= 2 -> Color(0xFFF9A825).copy(alpha = 0.95f)
        else -> Slate900.copy(alpha = 0.95f)
    }
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 16.dp
    ) {
        Column(modifier = Modifier.padding(20.dp).navigationBarsPadding()) {
            Text(
                text = when {
                    maxSeverity >= 3 -> "⚠️ Bahaya! Bahan sensitif"
                    maxSeverity >= 2 -> "⚠️ Perhatian! Cek bahan berikut"
                    detectedIngredients.isNotEmpty() -> "ℹ️ Bahan terdeteksi"
                    else -> "📝 Teks terbaca"
                },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (detectedIngredients.isEmpty()) {
                Text(rawText.take(200), color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 220.dp)) {
                    items(detectedIngredients.take(6)) { detected ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                when (detected.matchedIngredient.severity) {
                                    3 -> Icons.Default.Cancel
                                    2 -> Icons.Default.Warning
                                    else -> Icons.Default.Info
                                }, null, tint = Color.White, modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(detected.matchedIngredient.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text(
                                    if (detected.isAllergen) "Cocok dengan alergi/profil Anda" else detected.matchedIngredient.description ?: detected.matchedIngredient.category,
                                    color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmartAlertDialog(detectedIngredients: List<DetectedIngredient>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, null, tint = Color(0xFFFFA000), modifier = Modifier.size(40.dp)) },
        title = { Text("Peringatan Produk", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Produk mengandung bahan yang perlu dihindari:")
                detectedIngredients.take(5).forEach { detected ->
                    Surface(color = Color(0xFFFFF3E0), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(detected.matchedIngredient.name, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            Text(detected.matchedIngredient.description ?: detected.matchedIngredient.category, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Error)) {
                Text("Jangan Beli", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}