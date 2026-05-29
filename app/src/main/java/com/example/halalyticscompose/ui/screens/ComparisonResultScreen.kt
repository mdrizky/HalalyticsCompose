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
import com.example.halalyticscompose.ui.theme.*

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
                title = { Text("Hasil Perbandingan", fontWeight = FontWeight.Bold, color = Slate900) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate800)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        if (result == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Emerald)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Memuat hasil analisis AI...", color = Slate600)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Slate50)
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
                        color = Slate800
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            result!!.similarities!!.forEach { similarity ->
                                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 6.dp)) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp), tint = HalalGreen)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = similarity, fontSize = 14.sp, color = Slate700)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Analisis Head-to-Head",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Side-by-Side Comparison
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(result!!.comparison) { comp ->
                        val productDetail = products.find { 
                            it.name.equals(comp.productName, ignoreCase = true) || 
                            it.barcode == comp.productName // Handle cases where AI might return barcode
                        }
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
        colors = CardDefaults.cardColors(containerColor = EmeraldLighter),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, Emerald.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Emerald),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "Rekomendasi Halalytics", style = MaterialTheme.typography.labelLarge, color = Emerald, fontWeight = FontWeight.Bold)
                    Text(text = recommended, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Slate900)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(text = reason, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate800, lineHeight = 22.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = summary, fontSize = 14.sp, color = Slate600, lineHeight = 20.sp)
        }
    }
}

@Composable
fun ComparisonCard(comp: ProductComparison, product: StandardizedProduct?) {
    Card(
        modifier = Modifier.width(300.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate100)
    ) {
        Column {
            // Product Image/Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                if (!product?.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = product!!.imageUrl,
                        contentDescription = comp.productName,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(Slate50),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(48.dp), tint = Slate200)
                    }
                }
                
                // Halal Badge Overlay
                if (product?.statusHalal?.lowercase() == "halal") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(8.dp))
                            .background(HalalGreen)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("HALAL", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = product?.name ?: comp.productName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Slate900,
                    maxLines = 2,
                    modifier = Modifier.height(48.dp)
                )
                
                if (product?.brand != null) {
                    Text(text = product.brand, style = MaterialTheme.typography.bodySmall, color = Slate500)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Scores
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ScoreIndicator("Halal", comp.halalScore, HalalGreen, Modifier.weight(1f))
                    ScoreIndicator("Aman", comp.safetyScore, Info, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Pros
                if (comp.pros.isNotEmpty()) {
                    Text(text = "KELEBIHAN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Emerald)
                    Spacer(modifier = Modifier.height(8.dp))
                    comp.pros.take(3).forEach { pro ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = HalalGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = pro, fontSize = 12.sp, color = Slate700, maxLines = 1)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cons
                if (comp.cons.isNotEmpty()) {
                    Text(text = "KEKURANGAN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Error)
                    Spacer(modifier = Modifier.height(8.dp))
                    comp.cons.take(2).forEach { con ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp), tint = Error)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = con, fontSize = 12.sp, color = Slate700, maxLines = 1)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                
                // Suitability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate50)
                        .padding(12.dp)
                ) {
                    Text(
                        text = comp.suitability_notes,
                        fontSize = 11.sp,
                        color = Slate600,
                        lineHeight = 16.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
fun ScoreIndicator(label: String, score: Int, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500)
            Text(text = "$score%", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = score / 100f,
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )
    }
}
