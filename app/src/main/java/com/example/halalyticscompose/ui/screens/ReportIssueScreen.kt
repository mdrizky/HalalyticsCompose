@file:Suppress("DEPRECATION")

package com.example.halalyticscompose.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.halalyticscompose.ui.theme.HalalGreen
import com.example.halalyticscompose.ui.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportIssueScreen(
    navController: NavController,
    productId: Int,
    productName: String,
    viewModel: ReportViewModel = hiltViewModel()
) {
    var selectedReason by remember { mutableStateOf("fake_forgery") }
    var details by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val reportSuccess by viewModel.reportSuccess.collectAsState()
    val isSuspicious by viewModel.isSuspiciousResult.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> 
        imageUri = uri
    }

    LaunchedEffect(reportSuccess) {
        if (reportSuccess == true) {
            // Success dialog or auto-back handled below with content switch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporkan Kejanggalan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (reportSuccess == true) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = HalalGreen
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Terima Kasih!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Laporan Anda telah berhasil dikirim. Tim admin kami akan segera meninjau kebenaran informasi ini.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color.Gray
                    )
                    
                    if (isSuspicious) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF97316))
                        ) {
                            Text(
                                text = "WASPADA: Laporan Anda memicu status WASPADA otomatis karena banyaknya laporan serupa untuk produk ini.",
                                modifier = Modifier.padding(16.dp),
                                color = Color(0xFF9A3412),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { 
                            viewModel.resetState()
                            navController.popBackStack() 
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HalalGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Kembali")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Laporan untuk: $productName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HalalGreen
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("ai_report") },
                    colors = CardDefaults.cardColors(containerColor = HalalGreen.copy(alpha = 0.05f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HalalGreen.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoGraph, contentDescription = null, tint = HalalGreen)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Lihat Smart Report AI", fontWeight = FontWeight.Bold, color = HalalGreen)
                            Text("Dapatkan analisis kesehatan mendalam dari AI", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = HalalGreen)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Jenis Masalah:",
                    style = MaterialTheme.typography.titleSmall,
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                ReasonOption(
                    title = "Produk Palsu / Forgery",
                    description = "Kemasan mencurigakan, nomor BPOM tidak terdaftar.",
                    isSelected = selectedReason == "fake_forgery",
                    onClick = { selectedReason = "fake_forgery" }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ReasonOption(
                    title = "Data Salah",
                    description = "Informasi komposisi atau status halal tidak sesuai label.",
                    isSelected = selectedReason == "incorrect_data",
                    onClick = { selectedReason = "incorrect_data" }
                )

                Spacer(modifier = Modifier.height(8.dp))
                
                ReasonOption(
                    title = "Sertifikat Expired",
                    description = "Sertifikat halal sudah tidak berlaku lagi.",
                    isSelected = selectedReason == "expired_cert",
                    onClick = { selectedReason = "expired_cert" }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Bukti Foto (Opsional):",
                    style = MaterialTheme.typography.titleSmall,
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray.copy(alpha = 0.2f))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Evidence",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { imageUri = null },
                            modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Klik untuk unggah foto bukti", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Detail Laporan:",
                    style = MaterialTheme.typography.titleSmall,
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("Ceritakan kejanggalan yang Anda temukan...") },
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = errorMessage!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.submitReport(productId, selectedReason, details, imageUri) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HalalGreen),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Kirim Laporan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun ReasonOption(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) HalalGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) HalalGreen else Color.LightGray.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = HalalGreen))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
