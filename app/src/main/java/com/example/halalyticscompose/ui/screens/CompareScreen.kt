package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.halalyticscompose.ui.theme.*
import com.example.halalyticscompose.ui.viewmodel.CompareViewModel
import com.example.halalyticscompose.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    viewModel: CompareViewModel = hiltViewModel()
) {
    val queue by viewModel.comparisonQueue.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val result by viewModel.comparisonResult.collectAsState()
    val familyProfiles by mainViewModel.familyProfiles.collectAsState()
    val selectedProfile by mainViewModel.selectedFamilyProfile.collectAsState()

    LaunchedEffect(result) {
        if (result != null) navController.navigate("comparison_result")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bandingkan Produk", fontWeight = FontWeight.Bold, color = Slate900) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate800)
                    }
                },
                actions = {
                    if (queue.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearQueue() }) {
                            Text("Bersihkan", color = Error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (queue.size >= 2) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Button(
                        onClick = { viewModel.startComparison(selectedProfile?.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald),
                        shape = RoundedCornerShape(28.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Compare, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mulai Perbandingan (${queue.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Profile Selector
            Text("Pilih profil kesehatan:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Slate700)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                item {
                    FilterChip(
                        selected = selectedProfile == null,
                        onClick = { mainViewModel.selectFamilyProfile(null) },
                        label = { Text("Saya") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldLight.copy(alpha = 0.3f), selectedLabelColor = Emerald)
                    )
                }
                items(familyProfiles.size) { index ->
                    val profile = familyProfiles[index]
                    FilterChip(
                        selected = selectedProfile?.id == profile.id,
                        onClick = { mainViewModel.selectFamilyProfile(profile) },
                        label = { Text(profile.name) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldLight.copy(alpha = 0.3f), selectedLabelColor = Emerald)
                    )
                }
            }

            if (queue.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.CompareArrows, null, modifier = Modifier.size(80.dp), tint = Slate300)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Belum ada produk dipilih", style = MaterialTheme.typography.titleMedium, color = Slate600)
                        Text("Tambahkan dari detail produk atau cari barcode", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { navController.navigate("manual_input") }, colors = ButtonDefaults.buttonColors(containerColor = Emerald)) {
                            Text("Cari Produk")
                        }
                    }
                }
            } else {
                Text("Produk Terpilih (${queue.size}/5)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900, modifier = Modifier.padding(bottom = 16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(queue) { barcode ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(EmeraldLight.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.QrCode, null, tint = Emerald, modifier = Modifier.size(28.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Kode Produk", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                    Text(barcode, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = Slate900)
                                }
                                IconButton(onClick = { viewModel.removeFromCompare(barcode) }) {
                                    Icon(Icons.Default.Delete, null, tint = Error.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                    if (queue.size < 5) {
                        item {
                            OutlinedButton(
                                onClick = { navController.navigate("search_hub") },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Slate300)
                            ) {
                                Icon(Icons.Default.Add, null, tint = Emerald)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tambah Produk Lain", color = Slate700)
                            }
                        }
                    }
                }
            }
            
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null, tint = Error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(errorMessage!!, color = Error, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}