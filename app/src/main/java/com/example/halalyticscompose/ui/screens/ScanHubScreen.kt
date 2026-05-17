package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanHubScreen(navController: NavController) {
    val primaryColor = Color(0xFF004D40) // Deep Teal
    val accentColor = Color(0xFF26A69A) // Mint

    val scanOptions = listOf(
        ScanOption(
            title = "Scan Barcode",
            description = "Cek kehalalan produk kemasan.",
            icon = Icons.Default.QrCodeScanner,
            route = "scan",
            color = Color(0xFFE0F2F1)
        ),
        ScanOption(
            title = "Foto Makanan",
            description = "AI Deteksi Gizi dari foto piring.",
            icon = Icons.Default.CameraAlt,
            route = "meal_scan",
            color = Color(0xFFFFF3E0)
        ),
        ScanOption(
            title = "Scan Tabel Gizi",
            description = "Ekstrak data gizi dari label.",
            icon = Icons.Default.Description,
            route = "ocr_scan",
            color = Color(0xFFE3F2FD)
        ),
        ScanOption(
            title = "Menu Restoran",
            description = "Rekomendasi sehat dari buku menu.",
            icon = Icons.Default.RestaurantMenu,
            route = "ai_analysis", // Redirect to AI Hub for now
            color = Color(0xFFF3E5F5)
        ),
        ScanOption(
            title = "Cek Obat",
            description = "Identifikasi obat & interaksi.",
            icon = Icons.Default.Medication,
            route = "pill_scanner",
            color = Color(0xFFFFEBEE)
        ),
        ScanOption(
            title = "Voice Log",
            description = "Catat makanan dengan suara.",
            icon = Icons.Default.Mic,
            route = "voice_logging",
            color = Color(0xFFF1F8E9)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pusat Pemindaian AI", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.verticalGradient(listOf(primaryColor, accentColor))
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    Text(
                        "Pilih Metode Scan",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "Gunakan kecerdasan AI untuk data yang akurat.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }

            // Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(scanOptions) { option ->
                    ScanOptionCard(option = option) {
                        navController.navigate(option.route)
                    }
                }
            }

            // Hint
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = accentColor)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Tips: Pastikan pencahayaan cukup saat mengambil foto untuk hasil yang lebih valid.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

data class ScanOption(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String,
    val color: Color
)

@Composable
fun ScanOptionCard(option: ScanOption, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(option.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    option.icon,
                    contentDescription = null,
                    tint = Color(0xFF004D40),
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                option.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            Text(
                option.description,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}
