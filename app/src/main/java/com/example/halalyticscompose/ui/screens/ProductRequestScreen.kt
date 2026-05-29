package com.example.halalyticscompose.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.halalyticscompose.R
import com.example.halalyticscompose.ui.theme.*
import com.example.halalyticscompose.ui.viewmodel.ProductRequestViewModel
import com.example.halalyticscompose.utils.ImageUtils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductRequestScreen(
    navController: NavController,
    barcode: String,
    viewModel: ProductRequestViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var productName by remember { mutableStateOf("") }
    var ocrText by remember { mutableStateOf("") }

    var frontImageUri by remember { mutableStateOf<Uri?>(null) }
    var backImageUri by remember { mutableStateOf<Uri?>(null) }

    var pendingFrontCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingBackCameraUri by remember { mutableStateOf<Uri?>(null) }

    val uploadStatus by viewModel.uploadStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, context.getString(R.string.product_request_camera_permission), Toast.LENGTH_SHORT).show()
        }
    }

    val takeFrontPicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) frontImageUri = pendingFrontCameraUri
    }
    val takeBackPicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) backImageUri = pendingBackCameraUri
    }

    val galleryFrontLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        frontImageUri = uri
    }
    val galleryBackLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        backImageUri = uri
    }

    LaunchedEffect(uploadStatus) {
        uploadStatus?.let { result ->
            if (result.isSuccess) {
                Toast.makeText(context, result.getOrNull(), Toast.LENGTH_LONG).show()
                navController.popBackStack()
            } else {
                Toast.makeText(context, result.exceptionOrNull()?.message ?: context.getString(R.string.product_request_upload_failed), Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.product_request_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Emerald,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Slate50
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.dp, Emerald.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = Emerald)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.product_request_not_found),
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700
                    )
                }
            }
            
            if (barcode.isNotBlank()) {
                Text(stringResource(R.string.product_barcode_label, barcode), color = Emerald, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }

            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text(stringResource(R.string.product_request_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            ImagePickerSection(
                title = stringResource(R.string.product_request_photo_front),
                imageUri = frontImageUri,
                onCamera = {
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    val uri = ImageUtils.createTempImageUri(context, "front")
                    pendingFrontCameraUri = uri
                    takeFrontPicture.launch(uri)
                },
                onGallery = { galleryFrontLauncher.launch("image/*") }
            )

            ImagePickerSection(
                title = stringResource(R.string.product_request_photo_back),
                imageUri = backImageUri,
                onCamera = {
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    val uri = ImageUtils.createTempImageUri(context, "back")
                    pendingBackCameraUri = uri
                    takeBackPicture.launch(uri)
                },
                onGallery = { galleryBackLauncher.launch("image/*") }
            )

            OutlinedTextField(
                value = ocrText,
                onValueChange = { ocrText = it },
                label = { Text(stringResource(R.string.product_request_ocr_label)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    if (frontImageUri == null || backImageUri == null || productName.isBlank()) {
                        Toast.makeText(context, context.getString(R.string.product_request_validation_error), Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val frontFile = ImageUtils.reduceFileImage(ImageUtils.uriToFile(frontImageUri!!, context))
                    val backFile = ImageUtils.reduceFileImage(ImageUtils.uriToFile(backImageUri!!, context))

                    val reqFront = frontFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val reqBack = backFile.asRequestBody("image/jpeg".toMediaTypeOrNull())

                    val bodyFront = MultipartBody.Part.createFormData("image_front", frontFile.name, reqFront)
                    val bodyBack = MultipartBody.Part.createFormData("image_back", backFile.name, reqBack)

                    viewModel.uploadProductRequest(
                        imageFront = bodyFront,
                        imageBack = bodyBack,
                        barcode = barcode.toRequestBody("text/plain".toMediaType()),
                        productName = productName.toRequestBody("text/plain".toMediaType()),
                        ocrText = ocrText.toRequestBody("text/plain".toMediaType())
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text(stringResource(R.string.product_request_submit), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ImagePickerSection(
    title: String,
    imageUri: Uri?,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, fontWeight = FontWeight.Bold, color = Slate800, fontSize = 14.sp)
        if (imageUri == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate100),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PhotoLibrary, null, tint = Slate400, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.product_request_no_photo), color = Slate400, fontSize = 12.sp)
                }
            }
        } else {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCamera,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Emerald)
            ) {
                Icon(Icons.Default.CameraAlt, null, tint = Emerald, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.product_request_camera), color = Emerald)
            }
            OutlinedButton(
                onClick = onGallery,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Emerald)
            ) {
                Icon(Icons.Default.PhotoLibrary, null, tint = Emerald, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.product_request_gallery), color = Emerald)
            }
        }
    }
}
