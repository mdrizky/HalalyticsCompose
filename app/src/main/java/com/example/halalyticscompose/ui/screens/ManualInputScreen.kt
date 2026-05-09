package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.halalyticscompose.ui.viewmodel.MainViewModel

// ═══════════════════════════════════════════════════════════════════
// MANUAL INPUT SCREEN — PHARMACY CATALOG STYLE
// Search + Kategori + Featured Products seperti SS apotek
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualInputScreen(
    navController: NavController,
    initialCategory: String = "",
    viewModel: MainViewModel = hiltViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    var searchQuery by remember { mutableStateOf(initialCategory) }
    var errorMessage by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var showBarcodeDialog by remember { mutableStateOf(false) }
    var manualBarcode by remember { mutableStateOf("") }

    // Sample featured products for display
    val featuredProducts = remember {
        listOf(
            FeaturedProduct("Indomie Goreng", "Indofood", "8996001600016", "https://images.openfoodfacts.org/images/products/899/600/160/0016/front_en.6.400.jpg", "Halal"),
            FeaturedProduct("Oreo Original", "Mondelez", "7622210440532", "https://images.openfoodfacts.org/images/products/762/221/044/0532/front_en.12.400.jpg", "Halal"),
            FeaturedProduct("Pocari Sweat", "Otsuka", "8996001600603", "https://images.openfoodfacts.org/images/products/899/600/160/0603/front_en.3.400.jpg", "Halal"),
            FeaturedProduct("Teh Botol Sosro", "Sosro", "8886008101053", "https://images.openfoodfacts.org/images/products/888/600/810/1053/front_id.7.400.jpg", "Halal"),
            FeaturedProduct("Milo Active-Go", "Nestlé", "9556001068880", "https://images.openfoodfacts.org/images/products/955/600/106/8880/front_en.3.400.jpg", "Halal"),
            FeaturedProduct("Good Day Cappuccino", "Santos Jaya", "8991002105010", "https://images.openfoodfacts.org/images/products/899/100/210/5010/front_id.6.400.jpg", "Halal")
        )
    }

    val categories = remember {
        listOf(
            CategoryItem("Makanan", Icons.Default.Restaurant, Color(0xFFE8F5E9), Color(0xFF2E7D32)),
            CategoryItem("Minuman", Icons.Default.LocalDrink, Color(0xFFE3F2FD), Color(0xFF1565C0)),
            CategoryItem("Obat", Icons.Default.Medication, Color(0xFFFFF3E0), Color(0xFFE65100)),
            CategoryItem("Kosmetik", Icons.Default.Face, Color(0xFFFCE4EC), Color(0xFFAD1457)),
            CategoryItem("Snack", Icons.Default.Cookie, Color(0xFFF3E5F5), Color(0xFF7B1FA2)),
            CategoryItem("Susu", Icons.Default.LocalCafe, Color(0xFFE0F7FA), Color(0xFF00838F))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // ─── Top Header with Search ────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(colorScheme.primary, colorScheme.secondary)
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column {
                // Back + Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cari Obat, Food, Kosmetik",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        errorMessage = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    placeholder = {
                        Text("Cari nama produk atau barcode...", color = Color.White.copy(0.6f), fontSize = 14.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, null, tint = Color.White.copy(0.7f))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, null, tint = Color.White.copy(0.7f))
                            }
                        } else {
                            IconButton(onClick = { navController.navigate("scan") }) {
                                Icon(Icons.Default.QrCodeScanner, null, tint = Color.White)
                            }
                        }
                    },
                    shape = RoundedCornerShape(26.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White.copy(0.4f),
                        unfocusedBorderColor = Color.White.copy(0.2f),
                        focusedContainerColor = Color.White.copy(0.15f),
                        unfocusedContainerColor = Color.White.copy(0.1f),
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            if (searchQuery.isNotEmpty()) {
                                // Check if it looks like a barcode (digits only)
                                if (searchQuery.all { it.isDigit() } && searchQuery.length >= 8) {
                                    navController.navigate("product_detail/$searchQuery")
                                } else {
                                    navController.navigate("search_external?q=$searchQuery")
                                }
                            }
                        }
                    )
                )
            }
        }

        // ─── Content with scroll ────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // ─── Kategori Produk ──────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kategori Produk",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
                Text(
                    text = "Lihat Semua",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.primary,
                    modifier = Modifier.clickable { navController.navigate("search_external") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category chips - horizontal scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categories.forEach { cat ->
                    CategoryChip(
                        category = cat,
                        onClick = { navController.navigate("search_external?q=${cat.name}") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ─── Featured / Produk Populer ──────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Produk Populer",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
                Text(
                    text = "Lihat Semua",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.primary,
                    modifier = Modifier.clickable { navController.navigate("search_external") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Product Grid (2 columns)
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                featuredProducts.chunked(2).forEach { rowProducts ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowProducts.forEach { product ->
                            FeaturedProductCard(
                                product = product,
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate("product_detail/${product.barcode}") }
                            )
                        }
                        // Fill remaining space if odd number
                        if (rowProducts.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ─── Quick Actions ──────────────────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Aksi Cepat",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                QuickActionRow(
                    title = "Riwayat Scan",
                    subtitle = "Akses kembali produk yang pernah dicari",
                    icon = Icons.Default.History,
                    iconTint = colorScheme.primary,
                    onClick = { navController.navigate("history") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                QuickActionRow(
                    title = "Database Global",
                    subtitle = "Cari jutaan produk dari seluruh dunia",
                    icon = Icons.Default.Language,
                    iconTint = colorScheme.secondary,
                    onClick = { navController.navigate("search_external") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                QuickActionRow(
                    title = "Input Barcode Manual",
                    subtitle = "Masukkan nomor barcode langsung",
                    icon = Icons.Default.Keyboard,
                    iconTint = Color(0xFFE65100),
                    onClick = {
                        showBarcodeDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showBarcodeDialog) {
            AlertDialog(
                onDismissRequest = { showBarcodeDialog = false },
                title = { Text("Input Barcode Manual", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Masukkan nomor barcode produk (EAN/UPC)", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = manualBarcode,
                            onValueChange = { if (it.all { char -> char.isDigit() }) manualBarcode = it },
                            label = { Text("Nomor Barcode") },
                            placeholder = { Text("Contoh: 8996001600016") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = {
                                if (manualBarcode.length >= 8) {
                                    showBarcodeDialog = false
                                    navController.navigate("product_detail/$manualBarcode")
                                }
                            }),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (manualBarcode.length >= 8) {
                                showBarcodeDialog = false
                                navController.navigate("product_detail/$manualBarcode")
                            }
                        },
                        enabled = manualBarcode.length >= 8
                    ) {
                        Text("Cari Produk")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBarcodeDialog = false }) {
                        Text("Batal")
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

// ─── Data Classes ────────────────────────────────
data class FeaturedProduct(
    val name: String,
    val brand: String,
    val barcode: String,
    val imageUrl: String,
    val halalStatus: String
)

data class CategoryItem(
    val name: String,
    val icon: ImageVector,
    val bgColor: Color,
    val iconColor: Color
)

// ─── Category Chip Component ────────────────────
@Composable
private fun CategoryChip(
    category: CategoryItem,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(4.dp)
            .width(68.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(category.bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(category.icon, null, tint = category.iconColor, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = category.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// ─── Featured Product Card ────────────────────
@Composable
private fun FeaturedProductCard(
    product: FeaturedProduct,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentScale = ContentScale.Fit
                )

                // Halal badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            colorScheme.primary.copy(alpha = 0.9f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = product.halalStatus,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Product Info
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.brand,
                    fontSize = 11.sp,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Verified badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Verified,
                        null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Terverifikasi",
                        fontSize = 11.sp,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ─── Quick Action Row ────────────────────────────
@Composable
private fun QuickActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
