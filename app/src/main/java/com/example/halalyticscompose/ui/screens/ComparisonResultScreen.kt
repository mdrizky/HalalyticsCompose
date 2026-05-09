package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.halalyticscompose.data.model.ProductComparison
import com.example.halalyticscompose.data.model.StandardizedProduct
import com.example.halalyticscompose.ui.theme.HalalGreen
import com.example.halalyticscompose.ui.viewmodel.CompareViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonResultScreen(
    navController: NavController,
    viewModel: CompareViewModel = hiltViewModel()
) {
    val result by viewModel.comparisonResult.collectAsState()
    val products by viewModel.standardizedProducts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hasil Perbandingan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (result == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Tidak ada data hasil perbandingan.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Better Choice Highlight
                BetterChoiceCard(
                    recommended = result!!.betterChoice,
                    reason = result!!.reason,
                    summary = result!!.summary
                )

                if (!result!!.similarities.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Kesamaan Antar Produk",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            result!!.similarities!!.forEach { similarity ->
                                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(16.dp), tint = HalalGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = similarity, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Analisis Berdampingan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Side-by-Side Comparison (Horizontal Row of Cards)
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(result!!.comparison) { comp ->
                        val productDetail = products.find { it.name.contains(comp.productName, ignoreCase = true) || comp.productName.contains(it.name, ignoreCase = true) }
                        ComparisonCard(comp, productDetail)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun BetterChoiceCard(recommended: String, reason: String, summary: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HalalGreen.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, HalalGreen)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = HalalGreen, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Pilihan Terbaik AI", style = MaterialTheme.typography.labelMedium, color = HalalGreen)
                    Text(text = recommended, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(text = reason, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = summary, fontSize = 13.sp, color = Color.DarkGray)
        }
    }
}

@Composable
fun ComparisonCard(comp: ProductComparison, product: StandardizedProduct?) {
    Card(
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column {
            // Product Image/Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.LightGray.copy(alpha = 0.1f))
            ) {
                if (!product?.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = product!!.imageUrl,
                        contentDescription = comp.productName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Inside
                    )
                } else {
                    Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.align(Alignment.Center).size(48.dp), tint = Color.LightGray)
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = comp.productName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    modifier = Modifier.height(48.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scores
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ScoreIndicator("Halal", comp.halalScore, HalalGreen)
                    ScoreIndicator("Keamanan", comp.safetyScore, Color(0xFF3B82F6))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pros & Cons
                Text(text = "Kelebihan:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                comp.pros.forEach { pro ->
                    Text(text = "• $pro", fontSize = 11.sp, color = Color(0xFF15803D))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Kekurangan:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                comp.cons.forEach { con ->
                    Text(text = "• $con", fontSize = 11.sp, color = Color(0xFFB91C1C))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Suitability
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = comp.suitabilityNotes,
                        modifier = Modifier.padding(8.dp),
                        fontSize = 11.sp,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun ScoreIndicator(label: String, score: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(50.dp)) {
            CircularProgressIndicator(
                progress = { score / 100f },
                color = color,
                strokeWidth = 4.dp,
                modifier = Modifier.fillMaxSize()
            )
            Text(text = score.toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Text(text = label, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
    }
}
