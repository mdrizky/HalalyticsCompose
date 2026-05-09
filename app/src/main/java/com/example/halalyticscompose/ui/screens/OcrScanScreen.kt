package com.example.halalyticscompose.ui.screens

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.halalyticscompose.data.ocr.DetectedIngredient
import com.example.halalyticscompose.data.ocr.OcrProcessor
import com.example.halalyticscompose.ui.viewmodel.OcrScanViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalPermissionsApi::class, ExperimentalAnimationApi::class)
@Composable
fun OcrScanScreen(
    onNavigateBack: () -> Unit,
    viewModel: OcrScanViewModel = hiltViewModel(),
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermission.status.isGranted) {
            CameraPreviewWithOcr(
                onTextDetected = viewModel::processText,
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Izin kamera diperlukan untuk scan komposisi produk.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                        Text("Izinkan Kamera")
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape),
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                }

                if (uiState.isSyncing) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sinkron bahan...", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            val authViewModel: com.example.halalyticscompose.ui.viewmodel.AuthViewModel = hiltViewModel()
            val userData by authViewModel.userData.collectAsState()
            val userName = userData?.fullName ?: userData?.username ?: "User"
            
            Text(
                text = "Halo, $userName",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Text(
                text = "Arahkan kamera ke daftar komposisi produk",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }

        ScannerFrameOverlay(
            hasWarning = uiState.maxSeverity >= 2,
            hasDanger = uiState.maxSeverity >= 3,
        )

        AnimatedVisibility(
            visible = uiState.detectedIngredients.isNotEmpty() || uiState.rawText.isNotBlank(),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            OcrResultPanel(
                detectedIngredients = uiState.detectedIngredients,
                maxSeverity = uiState.maxSeverity,
                rawText = uiState.rawText,
            )
        }

        if (uiState.showAlert && uiState.detectedIngredients.isNotEmpty()) {
            SmartAlertDialog(
                detectedIngredients = uiState.detectedIngredients,
                onDismiss = viewModel::dismissAlert,
            )
        }
    }
}

@Composable
fun CameraPreviewWithOcr(
    onTextDetected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val ocrProcessor = remember { OcrProcessor() }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        modifier = modifier.fillMaxSize(),
        update = { previewView ->
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            val detectedText = runBlocking {
                                ocrProcessor.processImage(imageProxy)
                            }
                            if (detectedText.isNotBlank()) {
                                onTextDetected(detectedText)
                            }
                        }
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis,
                    )
                } catch (_: Exception) {
                }
            }, ContextCompat.getMainExecutor(context))
        },
    )
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onTextDetected: (String) -> Unit,
) {
    CameraPreviewWithOcr(
        onTextDetected = onTextDetected,
        modifier = modifier,
    )
}

@Composable
fun ScannerFrameOverlay(
    hasWarning: Boolean,
    hasDanger: Boolean,
) {
    val borderColor = when {
        hasDanger -> Color.Red
        hasWarning -> Color(0xFFFFA000)
        else -> Color.White
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 300.dp, height = 190.dp)
                .border(3.dp, borderColor, RoundedCornerShape(18.dp)),
        )
    }
}

@Composable
fun OcrResultPanel(
    detectedIngredients: List<DetectedIngredient>,
    maxSeverity: Int,
    rawText: String,
) {
    val backgroundColor = when {
        maxSeverity >= 3 -> Color(0xFFD32F2F).copy(alpha = 0.94f)
        maxSeverity >= 2 -> Color(0xFFF9A825).copy(alpha = 0.94f)
        else -> Color(0xFF263238).copy(alpha = 0.92f)
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .navigationBarsPadding(),
        ) {
            Text(
                text = when {
                    maxSeverity >= 3 -> "Bahaya! Bahan sensitif terdeteksi"
                    maxSeverity >= 2 -> "Perhatian! Ada bahan yang perlu dicek"
                    detectedIngredients.isNotEmpty() -> "Info bahan terdeteksi"
                    else -> "Teks berhasil dibaca"
                },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (detectedIngredients.isEmpty()) {
                Text(
                    text = rawText.take(180),
                    color = Color.White.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 4.dp),
                    modifier = Modifier.heightIn(max = 220.dp),
                ) {
                    items(detectedIngredients.take(6)) { detected ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val icon = when (detected.matchedIngredient.severity) {
                                3 -> Icons.Default.Cancel
                                2 -> Icons.Default.Warning
                                else -> Icons.Default.Info
                            }
                            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = detected.matchedIngredient.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = if (detected.isAllergen) {
                                        "Cocok dengan alergi/profil kesehatanmu"
                                    } else {
                                        detected.matchedIngredient.description ?: detected.matchedIngredient.category
                                    },
                                    color = Color.White.copy(alpha = 0.82f),
                                    style = MaterialTheme.typography.labelMedium,
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
fun SmartAlertDialog(
    detectedIngredients: List<DetectedIngredient>,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFFFA000),
                modifier = Modifier.size(40.dp),
            )
        },
        title = { Text("Peringatan Produk") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Produk ini mengandung bahan yang perlu kamu hindari:")
                detectedIngredients.take(5).forEach { detected ->
                    Surface(
                        color = Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = detected.matchedIngredient.name,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100),
                            )
                            Text(
                                text = detected.matchedIngredient.description ?: detected.matchedIngredient.category,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            ) {
                Text("Jangan Beli", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Tutup")
            }
        },
    )
}
