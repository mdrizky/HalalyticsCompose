package com.example.halalyticscompose.ui.screens

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.halalyticscompose.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors
import androidx.compose.ui.draw.shadow
import com.example.halalyticscompose.ui.theme.*
import com.example.halalyticscompose.ui.viewmodel.ScanViewModel

@Composable
fun ScanScreen(
    navController: NavController,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var scannedCode by remember { mutableStateOf("") }
    var isScanning by remember { mutableStateOf(true) }
    var showFlash by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showFlash = !showFlash },
                containerColor = if (showFlash) Emerald else Color.White,
                contentColor = if (showFlash) Color.White else Slate700,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Icon(if (showFlash) Icons.Default.FlashOn else Icons.Default.FlashOff, "Flash")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.verticalGradient(listOf(Slate50, Color.White))),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Tab Selector
            Surface(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(modifier = Modifier.padding(6.dp)) {
                    PremiumTabButton(
                        text = "Barcode",
                        icon = Icons.Default.QrCodeScanner,
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    PremiumTabButton(
                        text = "Komposisi",
                        icon = Icons.Default.TextFields,
                        isSelected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = if (selectedTab == 0) "Scan Barcode Produk" else "Scan Daftar Komposisi",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            
            Text(
                text = if (selectedTab == 0) "Arahkan kamera ke barcode di kemasan" else "Arahkan kamera ke tulisan komposisi",
                fontSize = 14.sp,
                color = Slate500,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Camera Preview Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .shadow(16.dp, RoundedCornerShape(32.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (isScanning && hasCameraPermission) {
                    CameraPreview(
                        scanMode = selectedTab,
                        onResultDetected = { result ->
                            if (isScanning && scannedCode.isEmpty()) {
                                scannedCode = result
                                isScanning = false
                                when (selectedTab) {
                                    1 -> navController.navigate("ai_analysis?ingredients=${Uri.encode(result)}")
                                    else -> navController.navigate("product_detail/$result")
                                }
                            }
                        },
                        showFlash = showFlash
                    )
                } else if (!hasCameraPermission) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Izin kamera ditolak", color = Color.White)
                    }
                }
                
                // Scanner Frame Overlay
                Box(
                    modifier = Modifier
                        .size(width = 280.dp, height = if (selectedTab == 0) 180.dp else 220.dp)
                        .border(
                            BorderStroke(2.dp, Emerald.copy(alpha = 0.5f)),
                            RoundedCornerShape(24.dp)
                        )
                ) {
                    CornerBracket(Alignment.TopStart)
                    CornerBracket(Alignment.TopEnd)
                    CornerBracket(Alignment.BottomStart)
                    CornerBracket(Alignment.BottomEnd)
                    
                    // Scanning line animation
                    val infiniteTransition = rememberInfiniteTransition(label = "")
                    val offset by infiniteTransition.animateFloat(
                        initialValue = 0.1f,
                        targetValue = 0.9f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = ""
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.005f)
                            .align(Alignment.TopCenter)
                            .offset(y = 180.dp * offset)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, Emerald, Color.Transparent)
                                )
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Manual Input helper
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Gagal scan barcode? Input Manual",
                    fontSize = 14.sp,
                    color = Emerald,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { navController.navigate("manual_barcode") }
                )
            }
        }
    }
}

@Composable
fun CameraPreview(
    scanMode: Int,
    onResultDetected: (String) -> Unit,
    showFlash: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    var camera: Camera? by remember { mutableStateOf(null) }

    LaunchedEffect(showFlash) {
        camera?.cameraControl?.enableTorch(showFlash)
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(executor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        if (scanMode == 0) { // Barcode
                            val scanner = BarcodeScanning.getClient()
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        barcode.rawValue?.let { onResultDetected(it) }
                                    }
                                }
                                .addOnCompleteListener { imageProxy.close() }
                        } else { // Text/OCR
                            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                            recognizer.process(image)
                                .addOnSuccessListener { visionText ->
                                    val fullText = visionText.text
                                    if (fullText.length > 10) { // Minimum length to avoid noise
                                        onResultDetected(fullText)
                                    }
                                }
                                .addOnCompleteListener { imageProxy.close() }
                        }
                    } else {
                        imageProxy.close()
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    cameraProvider.unbindAll()
                    camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, imageAnalysis
                    )
                    camera?.cameraControl?.enableTorch(showFlash)
                } catch (exc: Exception) {
                    android.util.Log.e("CameraPreview", "Binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun PremiumTabButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(44.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Emerald else Color.White,
            contentColor = if (isSelected) Color.White else Slate700
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun BoxScope.CornerBracket(alignment: Alignment) {
    val isTop = alignment == Alignment.TopStart || alignment == Alignment.TopEnd
    val isStart = alignment == Alignment.TopStart || alignment == Alignment.BottomStart
    val xOffset = if (isStart) (-8).dp else 8.dp
    val yOffset = if (isTop) (-8).dp else 8.dp
    
    Box(modifier = Modifier.align(alignment).offset(x = xOffset, y = yOffset).size(36.dp, 3.dp).background(Emerald, RoundedCornerShape(2.dp)))
    Box(modifier = Modifier.align(alignment).offset(x = xOffset, y = yOffset).size(3.dp, 36.dp).background(Emerald, RoundedCornerShape(2.dp)))
}
