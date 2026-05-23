package com.example.halalyticscompose.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.halalyticscompose.data.model.MealData
import com.example.halalyticscompose.ui.theme.*
import com.example.halalyticscompose.ui.viewmodel.ScanViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

@Composable
fun MealScanScreen(navController: NavController, viewModel: ScanViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val analysisState by viewModel.mealAnalysisState.collectAsState()
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { granted -> hasCameraPermission = granted }
    LaunchedEffect(Unit) { if (!hasCameraPermission) launcher.launch(android.Manifest.permission.CAMERA) }

    val cameraController = remember { LifecycleCameraController(context).apply { bindToLifecycle(lifecycleOwner); setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE) } }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (analysisState.data != null) {
            MealResultCard(data = analysisState.data!!, onClose = { navController.navigateUp() })
        } else {
            if (hasCameraPermission) {
                AndroidView(modifier = Modifier.fillMaxSize(), factory = { ctx -> PreviewView(ctx).apply { controller = cameraController; scaleType = PreviewView.ScaleType.FILL_CENTER } })
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Izin kamera diperlukan", color = Color.White) }
            }

            Column(modifier = Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.navigateUp() }, modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape).size(40.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    Surface(color = Emerald.copy(alpha = 0.9f), shape = RoundedCornerShape(20.dp)) {
                        Text("AI Meal Scanner", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.size(40.dp))
                }

                Box(modifier = Modifier.fillMaxWidth(0.8f).aspectRatio(1f).padding(20.dp)) {
                    val stroke = 3.dp; val length = 30.dp
                    listOf(Alignment.TopStart, Alignment.TopEnd, Alignment.BottomStart, Alignment.BottomEnd).forEach { align ->
                        Box(modifier = Modifier.align(align).size(length, stroke).background(Color.White.copy(alpha = 0.8f)))
                        Box(modifier = Modifier.align(align).size(stroke, length).background(Color.White.copy(alpha = 0.8f)))
                    }
                }

                if (analysisState.isLoading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Emerald, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Menganalisis makanan...", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Foto makanan Anda", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, modifier = Modifier.padding(bottom = 24.dp))
                        Button(
                            onClick = { takePhoto(context, cameraController) { bitmap -> viewModel.analyzeMealImage(saveBitmapToFile(context, bitmap)) } },
                            modifier = Modifier.size(84.dp).clip(CircleShape),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald),
                            elevation = ButtonDefaults.buttonElevation(8.dp)
                        ) {
                            Icon(Icons.Default.Camera, null, tint = Color.White, modifier = Modifier.size(36.dp))
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
        modifier = Modifier.fillMaxWidth().padding(24.dp).wrapContentHeight(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(data.mealName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                val badgeColor = when (data.halalAnalysis.status.lowercase()) {
                    "safe", "halal" -> Emerald
                    "haram" -> Error
                    else -> Color(0xFFFFCC00)
                }
                Surface(color = badgeColor.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
                    Text(data.halalAnalysis.status.uppercase(), modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = badgeColor, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                NutritionItem("Kalori", "${data.nutrition.calories}", "kcal")
                NutritionItem("Protein", "%.1f".format(data.nutrition.protein), "g")
                NutritionItem("Lemak", "%.1f".format(data.nutrition.fat), "g")
                NutritionItem("Gula", "%.1f".format(data.nutrition.sugar), "g")
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onClose, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Emerald), shape = RoundedCornerShape(28.dp)) {
                Text("Tambahkan ke Catatan Harian", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun NutritionItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
        Text(unit, fontSize = 12.sp, color = Color.Gray)
        Text(label, fontSize = 12.sp, color = Emerald)
    }
}
private fun takePhoto(
    context: android.content.Context,
    controller: androidx.camera.view.LifecycleCameraController,
    onPhotoTaken: (android.graphics.Bitmap) -> Unit
) {
    controller.takePicture(
        androidx.core.content.ContextCompat.getMainExecutor(context),
        object : androidx.camera.core.ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                super.onCaptureSuccess(image)
                val matrix = android.graphics.Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }

                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.capacity())
                buffer.get(bytes)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                val rotatedBitmap = android.graphics.Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )
                onPhotoTaken(rotatedBitmap)
                image.close()
            }

            override fun onError(exception: androidx.camera.core.ImageCaptureException) {
                super.onError(exception)
                android.util.Log.e("Camera", "Couldn't take photo: ", exception)
            }
        }
    )
}

private fun saveBitmapToFile(context: android.content.Context, bitmap: android.graphics.Bitmap): java.io.File {
    val file = java.io.File(context.cacheDir, "meal_scan_${System.currentTimeMillis()}.jpg")
    java.io.FileOutputStream(file).use { out ->
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
    }
    return file
}