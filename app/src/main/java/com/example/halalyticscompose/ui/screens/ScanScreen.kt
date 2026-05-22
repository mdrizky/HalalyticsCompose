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
    
    LaunchedEffect(Unit) {
        isScanning = false
        scannedCode = ""
        showFlash = false
        selectedTab = 0
    }
    
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
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar Premium
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate800)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Pemindaian Cerdas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        val userData by authViewModel.userData.collectAsState()
                        val userName = userData?.fullName ?: userData?.username ?: ""
                        if (userName.isNotEmpty()) {
                            Text(
                                text = userName,
                                fontSize = 11.sp,
                                color = Emerald,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    IconButton(onClick = { showFlash = !showFlash }) {
                        Icon(
                            if (showFlash) Icons.Filled.FlashOn else Icons.Outlined.FlashOff,
                            contentDescription = "Flash",
                            tint = if (showFlash) Emerald else Slate400
                        )
                    }
                }
            }
            
            // Tab Switcher Premium
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PremiumTabButton(
                    text = "Barcode",
                    icon = Icons.Outlined.QrCodeScanner,
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                PremiumTabButton(
                    text = "OCR Teks",
                    icon = Icons.Outlined.CameraAlt,
                    isSelected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        navController.navigate("enhanced_ocr")
                    }
                )
            }
            
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
                                    1 -> navController.navigate("enhanced_ocr?scannedText=${Uri.encode(result)}")
                                    else -> navController.navigate("product_detail/$result")
                                }
                            }
                        },
                        showFlash = showFlash
                    )
                } else if (!hasCameraPermission) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.CameraAlt, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Izin kamera diperlukan", color = Color.White, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }, colors = ButtonDefaults.buttonColors(containerColor = Emerald)) {
                                Text("Berikan Izin")
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(if (selectedTab == 0) Icons.Outlined.QrCodeScanner else Icons.Outlined.Description, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(56.dp))
                            Text("Tekan tombol Mulai", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        }
                    }
                }
                
                // Overlay Frame & Scan Line
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (isScanning) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .background(Error, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(Color.White, CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("LIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                    
                    Box(modifier = Modifier.size(280.dp), contentAlignment = Alignment.Center) {
                        if (isScanning) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .offset(y = (scanLinePosition * 220 - 110).dp)
                                    .background(Brush.horizontalGradient(listOf(Color.Transparent, Emerald, Emerald, Color.Transparent)))
                            )
                        }
                        CornerBracket(Alignment.TopStart)
                        CornerBracket(Alignment.TopEnd)
                        CornerBracket(Alignment.BottomStart)
                        CornerBracket(Alignment.BottomEnd)
                        
                        if (!isScanning) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(if (selectedTab == 0) Icons.Outlined.QrCodeScanner else Icons.Outlined.Description, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (selectedTab == 0) "Arahkan kamera ke barcode" else "Arahkan ke teks komposisi",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bottom Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                                        android.widget.Toast.makeText(context, "Tidak ada barcode terdeteksi", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener {
                                    android.widget.Toast.makeText(context, "Gagal membaca gambar", android.widget.Toast.LENGTH_SHORT).show()
                                }
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Error loading image", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, Slate300),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate700)
                ) {
                    Icon(Icons.Outlined.PhotoLibrary, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pilih dari Galeri", fontSize = 14.sp)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = { isScanning = true; scannedCode = "" },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Icon(if (selectedTab == 0) Icons.Outlined.QrCodeScanner else Icons.Outlined.CameraAlt, null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Mulai Scan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Masukkan manual",
                    fontSize = 13.sp,
                    color = Emerald,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { navController.navigate("manual_input") }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Laporkan produk tidak ditemukan",
                    fontSize = 12.sp,
                    color = Slate500,
                    modifier = Modifier.clickable { navController.navigate("product_request/unknown") }
                )
            }
        }
    }
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