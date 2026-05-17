package com.example.halalyticscompose.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.halalyticscompose.data.model.MealData
import com.example.halalyticscompose.ui.theme.HalalGreen
import com.example.halalyticscompose.ui.viewmodel.ScanViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

@Composable
fun MealScanScreen(
    navController: NavController,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val analysisState by viewModel.mealAnalysisState.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { granted: Boolean ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(android.Manifest.permission.CAMERA)
        }
    }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            bindToLifecycle(lifecycleOwner)
            setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (analysisState.data != null) {
            MealResultCard(
                data = analysisState.data!!,
                onClose = { 
                    navController.navigateUp()
                }
            )
        } else {
            if (hasCameraPermission) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            controller = cameraController
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                    }
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Camera permission required", color = Color.White)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close", tint = Color.White)
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(HalalGreen.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "AI Meal Scanner",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.size(40.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1f)
                        .padding(20.dp)
                ) {
                    val strokeWidth = 4.dp
                    val length = 30.dp
                    val color = Color.White.copy(alpha = 0.8f)
                    
                    Box(Modifier.align(Alignment.TopStart).size(length, strokeWidth).background(color))
                    Box(Modifier.align(Alignment.TopStart).size(strokeWidth, length).background(color))
                    Box(Modifier.align(Alignment.TopEnd).size(length, strokeWidth).background(color))
                    Box(Modifier.align(Alignment.TopEnd).size(strokeWidth, length).background(color))
                    Box(Modifier.align(Alignment.BottomStart).size(length, strokeWidth).background(color))
                    Box(Modifier.align(Alignment.BottomStart).size(strokeWidth, length).background(color))
                    Box(Modifier.align(Alignment.BottomEnd).size(length, strokeWidth).background(color))
                    Box(Modifier.align(Alignment.BottomEnd).size(strokeWidth, length).background(color))
                }

                if (analysisState.isLoading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = HalalGreen, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Analyzing Meal...", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Take a photo of your food", 
                            color = Color.White.copy(alpha = 0.8f), 
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(4.dp)
                        ) {
                            Button(
                                onClick = {
                                    takePhoto(context, cameraController) { bitmap ->
                                        val file = saveBitmapToFile(context, bitmap)
                                        viewModel.analyzeMealImage(file)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                colors = ButtonDefaults.buttonColors(containerColor = HalalGreen),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    Icons.Default.Camera, 
                                    contentDescription = "Capture", 
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
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
fun MealResultCard(data: MealData, onClose: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = data.mealName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                val badgeColor = when(data.halalAnalysis.status.lowercase()) {
                    "safe", "halal" -> HalalGreen
                    "haram" -> Color.Red
                    else -> Color(0xFFFFCC00)
                }
                
                Surface(
                    color = badgeColor.copy(alpha=0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = data.halalAnalysis.status.uppercase(),
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                NutritionItem("Calories", "${data.nutrition.calories}", "kcal")
                NutritionItem("Protein", "%.1f".format(data.nutrition.protein), "g")
                NutritionItem("Fat", "%.1f".format(data.nutrition.fat), "g")
                NutritionItem("Sugar", "%.1f".format(data.nutrition.sugar), "g")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = HalalGreen)
            ) {
                Text("Add to Daily Log")
            }
        }
    }
}

@Composable
fun NutritionItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
        Text(unit, fontSize = 12.sp, color = Color.Gray)
        Text(label, fontSize = 12.sp, color = HalalGreen)
    }
}

private fun takePhoto(
    context: android.content.Context,
    cameraController: LifecycleCameraController,
    onPhotoTaken: (Bitmap) -> Unit
) {
    try {
        val executor = ContextCompat.getMainExecutor(context)
        cameraController.takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        val bitmap = imageProxyToBitmap(image)
                        onPhotoTaken(bitmap)
                    } catch (e: Exception) {
                        android.util.Log.e("MealScanScreen", "Error processing image: ${e.message}")
                    } finally {
                        image.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    android.util.Log.e("MealScanScreen", "Camera capture error: ${exception.message}")
                }
            }
        )
    } catch (e: Exception) {
        android.util.Log.e("MealScanScreen", "Error taking photo: ${e.message}")
    }
}

fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val buffer: ByteBuffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    
    val matrix = Matrix()
    matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    
    return bitmap
}

private fun saveBitmapToFile(context: android.content.Context, bitmap: Bitmap): File {
    val file = File(context.cacheDir, "meal_scan_${System.currentTimeMillis()}.jpg")
    file.createNewFile()
    val bos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos)
    val bitmapdata = bos.toByteArray()
    val fos = FileOutputStream(file)
    fos.write(bitmapdata)
    fos.flush()
    fos.close()
    return file
}
