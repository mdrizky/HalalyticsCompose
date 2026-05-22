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
import com.example.halalyticscompose.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanHubScreen(navController: NavController) {
    val scanOptions = listOf(
        ScanOption("Scan Barcode", "Cek kehalalan produk", Icons.Default.QrCodeScanner, "scan", EmeraldLight),
        ScanOption("Foto Makanan", "AI deteksi gizi", Icons.Default.CameraAlt, "meal_scan", Color(0xFFFFF3E0)),
        ScanOption("Scan Tabel Gizi", "Ekstrak data gizi", Icons.Default.Description, "ocr_scan", Color(0xFFE3F2FD)),
        ScanOption("Menu Restoran", "Rekomendasi sehat", Icons.Default.RestaurantMenu, "ai_analysis", Color(0xFFF3E5F5)),
        ScanOption("Cek Obat", "Identifikasi interaksi", Icons.Default.Medication, "pill_scanner", Color(0xFFFFEBEE)),
        ScanOption("Voice Log", "Catat dengan suara", Icons.Default.Mic, "voice_logging", Color(0xFFF1F8E9))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pusat Pemindaian AI", fontWeight = FontWeight.Bold, color = Slate900) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate800)
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
                .background(Slate50)
        ) {
            // Premium Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.verticalGradient(listOf(Emerald, TealDark))
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Text("Pilih Metode Scan", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Gunakan AI untuk data akurat", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                }
            }

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

            // Tips Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = Emerald)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Pastikan pencahayaan cukup untuk hasil terbaik", style = MaterialTheme.typography.bodySmall, color = Slate600)
                }
            }
        }
    }
}

data class ScanOption(val title: String, val description: String, val icon: ImageVector, val route: String, val bgColor: Color)

@Composable
fun ScanOptionCard(option: ScanOption, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(option.bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(option.icon, null, tint = Emerald, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(option.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900, textAlign = TextAlign.Center)
            Text(option.description, fontSize = 12.sp, color = Slate500, textAlign = TextAlign.Center, maxLines = 2)
        }
    }
}