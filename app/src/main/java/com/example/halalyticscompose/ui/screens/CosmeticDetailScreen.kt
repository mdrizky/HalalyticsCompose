package com.example.halalyticscompose.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.halalyticscompose.data.api.BeautyProduct
import com.example.halalyticscompose.data.api.bestIngredientsText
import com.example.halalyticscompose.ui.viewmodel.SkincareViewModel
import com.google.gson.Gson

// ═══════════════════════════════════════════════════════════════════
// COSMETIC DETAIL SCREEN — PREMIUM REDESIGN
// Logo Halal menonjol, kartu info terstruktur, tombol analisis AI
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosmeticDetailScreen(
    navController: NavController,
    productId: String? = null,
    viewModel: SkincareViewModel = hiltViewModel()
) {
    val selected by viewModel.selectedProduct.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val decodedProductId = productId?.let { Uri.decode(it) }
    val cachedCosmeticJson = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<String>("selected_cosmetic_json")
    var isBookmarked by remember { mutableStateOf(false) }

    LaunchedEffect(decodedProductId, cachedCosmeticJson, selected) {
        if (selected == null && !cachedCosmeticJson.isNullOrBlank()) {
            val cachedProduct = runCatching {
                Gson().fromJson(cachedCosmeticJson, BeautyProduct::class.java)
            }.getOrNull()
            if (cachedProduct != null) {
                viewModel.selectProduct(cachedProduct)
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.remove<String>("selected_cosmetic_json")
                return@LaunchedEffect
            }
        }
        if (selected == null && !decodedProductId.isNullOrBlank()) {
            viewModel.selectProductById(decodedProductId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFEC4899),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Detail Kosmetik", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { isBookmarked = !isBookmarked }) {
                        Icon(
                            if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) Color(0xFFF59E0B) else colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = {
                        selected?.let { product ->
                            navController.navigate("report_issue/0/${Uri.encode(product.productName ?: "Cosmetic")}")
                        }
                    }) {
                        Icon(Icons.Default.Flag, contentDescription = "Laporkan", tint = colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    titleContentColor = colorScheme.onBackground,
                    navigationIconContentColor = colorScheme.onBackground
                )
            )
        },
        containerColor = colorScheme.background
    ) { padding ->
        if (selected == null) {
            // Loading / Error state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!decodedProductId.isNullOrBlank() && isLoading) {
                    CircularProgressIndicator(color = Color(0xFFEC4899))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Memuat detail kosmetik...", color = colorScheme.onSurfaceVariant)
                } else {
                    Text(error ?: "Data kosmetik belum dipilih", color = colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!decodedProductId.isNullOrBlank()) {
                            Button(onClick = { viewModel.selectProductById(decodedProductId) }) {
                                Text("Coba Lagi")
                            }
                        }
                        OutlinedButton(onClick = { navController.navigateUp() }) {
                            Text("Kembali")
                        }
                    }
                }
            }
            return@Scaffold
        }

        val product = selected!!
        val ingredients = product.bestIngredientsText

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ─── Hero Image ─────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colorScheme.surfaceVariant)
            ) {
                val fallbackUrl = "https://ui-avatars.com/api/?name=${product.productName ?: "Kosmetik"}&background=EC4899&color=fff&size=400"
                AsyncImage(
                    model = if (!product.imageUrl.isNullOrBlank()) product.imageUrl else fallbackUrl,
                    contentDescription = product.productName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient overlay bawah
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                                startY = 120f
                            )
                        )
                )
            }

            // ─── Nama Produk & Brand ────────────────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    product.productName ?: "Kosmetik Tidak Dikenal",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorScheme.onSurface,
                    lineHeight = 28.sp
                )
                if (!product.brands.isNullOrBlank()) {
                    Text(
                        "oleh ${product.brands}",
                        fontSize = 14.sp,
                        color = colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ─── Logo Halal Besar (Prominence) ──────────────
            HalalProminenceBadge(modifier = Modifier.padding(horizontal = 20.dp))

            // ─── Info Grid ──────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CosmeticInfoTile(
                        label = "Kategori",
                        value = product.categories ?: "-",
                        icon = Icons.Default.Category,
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f)
                    )
                    CosmeticInfoTile(
                        label = "Negara",
                        value = product.countries ?: "-",
                        icon = Icons.Default.Public,
                        color = Color(0xFF0EA5E9),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CosmeticInfoTile(
                        label = "Kuantitas",
                        value = product.quantity ?: "-",
                        icon = Icons.Default.ShoppingBag,
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                    CosmeticInfoTile(
                        label = "Kemasan",
                        value = product.packaging ?: "-",
                        icon = Icons.Default.Description,
                        color = Color(0xFF14B8A6),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ─── Ingredients Card ───────────────────────────
            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Science,
                            contentDescription = null,
                            tint = Color(0xFFEC4899),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Komposisi / Ingredients",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        ingredients ?: "Data ingredients tidak tersedia dari OpenBeautyFacts.",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            // ─── Aksi Analisis AI ───────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "🔬 Analisis Cerdas",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = colorScheme.onSurface
                )
                Text(
                    "Periksa keamanan dan status halal bahan produk ini",
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )

                // Tombol Analisis Halal & Safety
                Button(
                    onClick = {
                        if (!ingredients.isNullOrBlank()) {
                            viewModel.analyzeIngredients(
                                ingredientsText = ingredients,
                                productName = product.productName ?: "Cosmetic"
                            )
                            navController.navigate("skincare_scanner")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                    enabled = !ingredients.isNullOrBlank()
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analisis Halal & Safety", fontWeight = FontWeight.Bold)
                }

                // Tombol Cek Keamanan Ingredients
                OutlinedButton(
                    onClick = {
                        if (!ingredients.isNullOrBlank()) {
                            viewModel.checkSafety(ingredients)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !ingredients.isNullOrBlank()
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cek Keamanan Bahan", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// SUB-COMPOSABLES
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun HalalProminenceBadge(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge ikon halal besar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E).copy(alpha = 0.15f))
                    .border(2.dp, Color(0xFF22C55E), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Status Halal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF166534)
                )
                Text(
                    "Gunakan tombol 'Analisis Halal' untuk memeriksa status halal bahan produk ini secara detail dengan AI.",
                    color = Color(0xFF4ADE80),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun CosmeticInfoTile(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                label,
                color = colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                value,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
