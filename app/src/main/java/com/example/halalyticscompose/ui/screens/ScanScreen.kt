package com.example.halalyticscompose.ui.screens

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import com.example.halalyticscompose.ui.theme.*
import com.example.halalyticscompose.ui.viewmodel.ScanViewModel
import com.example.halalyticscompose.utils.RetailBarcodeAnalyzer
import com.example.halalyticscompose.utils.TextRecognitionAnalyzer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    navController: NavController,
    viewModel: ScanViewModel = hiltViewModel(),
    authViewModel: com.example.halalyticscompose.ui.viewmodel.AuthViewModel = hiltViewModel(),
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Barcode, 1 = Ingredients
    var isScanning by remember { mutableStateOf(false) }
    var scannedCode by remember { mutableStateOf("") }
    var showFlash by remember { mutableStateOf(false) }
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
    
    // Reset state when screen is opened
    LaunchedEffect(Unit) {
        isScanning = false
        scannedCode = ""
        showFlash = false
        selectedTab = 0
    }
    
    // Animation for scan line
    val infiniteTransition = rememberInfiniteTransition()
    val scanLinePosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigateUp() }
                ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(26.dp)
                        )
                }
                
                Column {
                    Text(
                        text = stringResource(R.string.scan_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    val userData by authViewModel.userData.collectAsState()
                    val userName = userData?.fullName ?: userData?.username ?: ""
                    if (userName.isNotEmpty()) {
                        Text(
                            text = userName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Flash toggle
                IconButton(
                    onClick = { showFlash = !showFlash }
                ) {
                    Icon(
                        if (showFlash) Icons.Filled.FlashOn else Icons.Outlined.FlashOff,
                        contentDescription = stringResource(R.string.scan_flash_toggle),
                        tint = if (showFlash) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            
            // Tab Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TabButton(
                    text = stringResource(R.string.scan_tab_barcode),
                    icon = Icons.Outlined.QrCodeScanner,
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                TabButton(
                    text = stringResource(R.string.scan_tab_ocr),
                    icon = Icons.Outlined.CameraAlt,
                    isSelected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                        navController.navigate("enhanced_ocr")
                    }
                )


            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
                // Camera preview area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (isScanning && hasCameraPermission) {
                    CameraPreview(
                        scanMode = selectedTab, // 0 = Barcode, 1 = OCR
                        onResultDetected = { result ->
                            if (isScanning && scannedCode.isEmpty()) {
                                println("📸 Code/Text detected: $result")
                                scannedCode = result
                                isScanning = false
                                
                                when (selectedTab) {
                                    1 -> {
                                        // OCR Text Detected -> Send to Enhanced OCR Screen
                                        navController.navigate("enhanced_ocr?scannedText=${Uri.encode(result)}")
                                    }

                                    else -> {
                                        // Regular Barcode
                                        navController.navigate("product_detail/$result")
                                    }
                                }
                            }
                        },
                        showFlash = showFlash
                    )
                } else if (!hasCameraPermission) {
                     Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             Icon(
                                Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(48.dp)
                            )
                             Spacer(modifier = Modifier.height(16.dp))
                             Text(stringResource(R.string.scan_permission_required), color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                             Spacer(modifier = Modifier.height(8.dp))
                             Text(stringResource(R.string.scan_permission_explanation), color = MaterialTheme.colorScheme.onSurface.copy(0.6f), fontSize = 12.sp)
                             Spacer(modifier = Modifier.height(16.dp))
                             Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                                 Text(stringResource(R.string.scan_give_permission))
                             }
                         }
                    }
                } else {
                    // Placeholder when not scanning
                     Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             Icon(
                                if (selectedTab == 0) Icons.Outlined.QrCodeScanner else Icons.Outlined.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                                modifier = Modifier.size(48.dp)
                            )
                             Spacer(modifier = Modifier.height(8.dp))
                             Text(stringResource(R.string.scan_start_instruction), color = MaterialTheme.colorScheme.onSurface.copy(0.6f), fontSize = 12.sp)
                         }
                    }
                }

                // Overlay UI (Scan Frame, etc)
                // LIVE badge etc... (Keeping existing overlay logic visually)
                   Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                   ) {
                       // ... (Reuse existing overlay components if possible, or re-declare them here for clarity)
                       // Moving the overlay components outside this conditional in the next logical block or keeping them superimposed
                       
                    // LIVE badge
                    if (isScanning) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .background(
                                    MaterialTheme.colorScheme.error,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(MaterialTheme.colorScheme.onError, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.scan_live_badge),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onError
                                )
                            }
                        }
                    }

                     // Scanning frame
                    Box(
                        modifier = Modifier.size(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Scan line
                        if (isScanning) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .offset(y = (scanLinePosition * 200 - 100).dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.primary,
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                        
                        // Corner brackets
                        CornerBracket(Alignment.TopStart)
                        CornerBracket(Alignment.TopEnd)
                        CornerBracket(Alignment.BottomStart)
                        CornerBracket(Alignment.BottomEnd)
                        
                        // Instructions
                         if (!isScanning) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Outlined.QrCodeScanner
                                    else Icons.Outlined.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                 Text(
                                    text = if (selectedTab == 0)
                                        stringResource(R.string.scan_frame_barcode_instruction)
                                    else
                                        stringResource(R.string.scan_frame_ocr_instruction),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                   }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bottom buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Gallery Picker
                val galleryLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri ->
                    uri?.let {
                        try {
                            val image = InputImage.fromFilePath(context, it)
                            val scanner = BarcodeScanning.getClient()
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    val barcode = barcodes.firstOrNull()?.rawValue
                                    if (barcode != null) {
                                        scannedCode = barcode
                                        navController.navigate("product_detail/$barcode")
                                    } else {
                                        // Show toast or error: No barcode found
                                        android.widget.Toast.makeText(context, context.getString(R.string.scan_no_barcode_found), android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    android.widget.Toast.makeText(context, context.getString(R.string.scan_failed, e.message ?: "Unknown error"), android.widget.Toast.LENGTH_SHORT).show()
                                }
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, context.getString(R.string.scan_error_loading), android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                // Gallery button
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        Icons.Outlined.PhotoLibrary,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                     Text(
                        text = stringResource(R.string.scan_import_gallery),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Scan button
                Button(
                    onClick = { 
                        isScanning = true
                        scannedCode = "" // Reset scanned code when restarting scan
                        // No logic to navigate immediately, let the scanner detect text/barcode first
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (selectedTab == 0) Icons.Outlined.QrCodeScanner
                        else Icons.Outlined.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                     Text(
                        text = stringResource(R.string.scan_start_button),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Manual input link
                Text(
                    text = stringResource(R.string.scan_manual_input),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        navController.navigate("manual_input")
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // NEW: Report Link
                Text(
                    text = stringResource(R.string.scan_report_missing),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                    modifier = Modifier.clickable {
                        navController.navigate("product_request/unknown")
                    }
                )
            }
        }
    }
}

@Composable
fun CameraPreview(
    scanMode: Int, // 0 = Barcode, 1 = OCR (Text)
    onResultDetected: (String) -> Unit,
    showFlash: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    // Disposable to clean up camera when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            // Clean up camera resources
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
            } catch (e: Exception) {
                println("Error cleaning up camera: ${e.message}")
            }
        }
    }
    
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)
            
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val resolutionSelector = ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            android.util.Size(1280, 720),
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                        )
                    )
                    .build()

                val preview = Preview.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                
                val imageAnalysis = ImageAnalysis.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        val executor = Executors.newSingleThreadExecutor()
                        val analyzer = if (scanMode == 1) {
                            TextRecognitionAnalyzer(onResultDetected)
                        } else {
                            RetailBarcodeAnalyzer(onResultDetected)
                        }
                        analysis.setAnalyzer(executor, analyzer)
                    }
                
                // Robust Camera Selection logic for Emulator Support
                var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                // Try to find a valid camera
                try {
                    if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    } else if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                    }
                } catch (e: Exception) {
                    // Fallback to back camera if check fails (some emulators throw here)
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                }
                
                try {
                    cameraProvider.unbindAll()
                    
                    // Attempt to bind
                    try {
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                        
                        // Enable flash if requested (check if available first)
                        if (camera.cameraInfo.hasFlashUnit()) {
                            camera.cameraControl.enableTorch(showFlash)
                        }
                    } catch (e: Exception) {
                        println("Binding failed: ${e.message}")
                    }
                    
                } catch (exc: Exception) {
                    println("Error binding camera: ${exc.message}")
                    // If binding failed with default selector, try front camera as last resort if not already tried
                    if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_FRONT_CAMERA,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            println("Fallback to front camera also failed: ${e.message}")
                        }
                    }
                }
            }, executor)
            
            previewView
        },
        update = { view ->
             // If we wanted to update flash dynamically without rebinding, we'd need a reference to the camera control
             // For simplicity, we rely on the bind check in factory, but for dynamic toggle we might trigger a recomposition effect.
             // Given the complexity of sharing CameraControl state in a simple composable, we'll accept basic toggle behavior 
             // or could rebind. For now, this is sufficient.
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun TabButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(40.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun BoxScope.CornerBracket(alignment: Alignment) {
    val isTop = alignment == Alignment.TopStart || alignment == Alignment.TopEnd
    val isStart = alignment == Alignment.TopStart || alignment == Alignment.BottomStart
    
    val xOffset = if (isStart) (-8).dp else 8.dp
    val yOffset = if (isTop) (-8).dp else 8.dp
    
    // Horizontal bar
    Box(
        modifier = Modifier
            .align(alignment)
            .offset(x = xOffset, y = yOffset)
            .size(36.dp, 3.dp)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
    )
    
    // Vertical bar
    Box(
        modifier = Modifier
            .align(alignment)
            .offset(x = xOffset, y = yOffset)
            .size(3.dp, 36.dp)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
    )
}
