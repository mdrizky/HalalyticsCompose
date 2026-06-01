package com.example.halalyticscompose.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.halalyticscompose.data.model.ProductItem
import com.example.halalyticscompose.ui.theme.*
import com.example.halalyticscompose.ui.viewmodel.MainViewModel
import com.example.halalyticscompose.ui.viewmodel.ProductExternalViewModel
import com.example.halalyticscompose.data.model.ProductImageResult
import com.example.halalyticscompose.ui.components.ProductImagesSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductExternalDetailScreen(
    navController: NavController,
    barcode: String,
    externalViewModel: ProductExternalViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    val productDetail by externalViewModel.productDetail.collectAsState()
    val isLoading by externalViewModel.isLoadingDetail.collectAsState()
    val error by externalViewModel.detailError.collectAsState()
    val imageState by externalViewModel.productImageState.collectAsState()
    
    // Fetch product on launch
    LaunchedEffect(barcode) {
        externalViewModel.getProductDetail(barcode)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.navigateUp() }
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Text(
                text = "Product Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(onClick = { /* Share */ }) {
                Icon(
                    Icons.Outlined.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        
        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = HalalGreen,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Mencari produk di database internasional...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                        )
                    }
                }
            }
            
            error.isNotEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Product not in database",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Barcode: $barcode",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TealDark,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = { navController.navigate("contribution?barcode=$barcode") },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Report & Admin Verification", color = Color.White)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = { navController.navigate("ocr_scan") },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Open OCR (Front & Back Photo)")
                    }
                    
                    TextButton(
                        onClick = { externalViewModel.getProductDetail(barcode) },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Try Again", color = TealDark)
                    }
                }
            }
            
            productDetail != null -> {
                ProductDetailContent(
                    product = productDetail!!,
                    barcode = barcode,
                    imageState = imageState,
                    onSaveToHistory = {
                        mainViewModel.addScanToHistory(
                            productId = null,
                            barcode = barcode,
                            productName = productDetail!!.getDisplayName(),
                            productImage = productDetail!!.getBestImageUrl(),
                            halalStatus = productDetail!!.getHalalStatus(),
                            source = productDetail!!.source ?: "external"
                        )
                    },
                    modifier = Modifier.padding(top = 56.dp)
                )
            }
        }
    }
}

@Composable
private fun ProductDetailContent(
    product: ProductItem,
    barcode: String,
    imageState: ProductImageResult?,
    onSaveToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSaved by remember { mutableStateOf(false) }
    
    val halalStatus = product.getHalalStatus()
    val halalColor = product.getHalalStatusColor()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 100.dp)
    ) {
        // Image Gallery from ProductImagesSection if available, else simple fallback
        if (imageState != null) {
            ProductImagesSection(
                imageResult = imageState,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
        } else {
            // Product Image (Legacy)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val imageUrl = product.getBestImageUrl()
                    val fallbackUrl = "https://ui-avatars.com/api/?name=${product.getDisplayName()}&background=1E293B&color=3B82F6&size=400"
                    AsyncImage(
                        model = imageUrl ?: fallbackUrl,
                        contentDescription = product.getDisplayName(),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    
                    // Nutriscore badge
                    product.nutriscoreGrade?.let { grade ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .size(40.dp)
                                .background(getNutriscoreColor(grade), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = grade.uppercase(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Product Name
        Text(
            text = product.getDisplayName(),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        product.brands?.let { brands ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = brands,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Barcode
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.QrCode,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = barcode,
                fontSize = 12.sp,
                color = TextMuted,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }

        if (!product.source.isNullOrBlank() || !product.syncedAt.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = listOfNotNull(
                    product.source?.let { "Source: $it" },
                    product.syncedAt?.let { "Synced: $it" }
                ).joinToString(" • "),
                fontSize = 11.sp,
                color = TextMuted
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Halal Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = halalColor.copy(alpha = 0.12f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        when {
                            product.isHalal() -> Icons.Filled.Verified
                            product.halalAnalysis?.isPotentiallyHalal == true -> Icons.Filled.CheckCircle
                            product.halalAnalysis?.isPotentiallyHalal == false -> Icons.Filled.Warning
                            else -> Icons.AutoMirrored.Filled.Help
                        },
                        contentDescription = null,
                        tint = halalColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Halal Status",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = halalStatus,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = halalColor
                        )
                    }
                }
                
                // Suspicious ingredients warning
                product.halalAnalysis?.suspiciousIngredients?.let { suspicious ->
                    if (suspicious.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "⚠️ Suspicious ingredients: ${suspicious.joinToString(", ")}",
                            fontSize = 12.sp,
                            color = HaramColor
                        )
                    }
                }
                
                product.halalAnalysis?.recommendation?.let { rec ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = rec,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Labels Row
        if (product.isVegetarian() || product.isVegan() || product.isHalal()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (product.isHalal()) {
                    LabelBadge("HALAL", HalalColor)
                }
                if (product.isVegetarian()) {
                    LabelBadge("VEGETARIAN", Color(0xFF4CAF50))
                }
                if (product.isVegan()) {
                    LabelBadge("VEGAN", Color(0xFF8BC34A))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Nutriscore Card
        product.nutriscoreGrade?.let { grade ->
            InfoCard(
                title = "Nutri-Score",
                icon = Icons.Outlined.Favorite
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("A", "B", "C", "D", "E").forEach { letter ->
                        Box(
                            modifier = Modifier
                                .size(if (letter == grade.uppercase()) 40.dp else 32.dp)
                                .background(
                                    getNutriscoreColor(letter),
                                    RoundedCornerShape(
                                        topStart = if (letter == "A") 8.dp else 0.dp,
                                        bottomStart = if (letter == "A") 8.dp else 0.dp,
                                        topEnd = if (letter == "E") 8.dp else 0.dp,
                                        bottomEnd = if (letter == "E") 8.dp else 0.dp
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = letter,
                                fontSize = if (letter == grade.uppercase()) 16.sp else 12.sp,
                                fontWeight = if (letter == grade.uppercase()) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // Ingredients
        val ingredients = product.ingredientsText ?: product.ingredientsTextEn
        if (!ingredients.isNullOrBlank()) {
            InfoCard(
                title = "Ingredients",
                icon = Icons.Outlined.Science
            ) {
                Text(
                    text = ingredients,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        val energy = product.getNutrimentNumber("energy", "energy-kcal_100g", "energy-kcal", "energy_100g")
        val sugar = product.getNutrimentNumber("sugar", "sugars_100g", "sugars")
        val fat = product.getNutrimentNumber("fat", "fat_100g", "fat")
        val saturatedFat = product.getNutrimentNumber("saturated-fat", "saturated-fat_100g", "saturated_fat")
        val salt = product.getNutrimentNumber("salt", "salt_100g", "salt")
        val sodium = product.getNutrimentNumber("sodium", "sodium_100g")
        if (!energy.isNullOrBlank() || !sugar.isNullOrBlank() || !fat.isNullOrBlank() || !saturatedFat.isNullOrBlank() || !salt.isNullOrBlank() || !sodium.isNullOrBlank()) {
            InfoCard(
                title = "Nutrition Snapshot (per 100g/ml)",
                icon = Icons.Outlined.Favorite
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!energy.isNullOrBlank()) Text("Energy: $energy", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!sugar.isNullOrBlank()) Text("Sugar: $sugar g", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!fat.isNullOrBlank()) Text("Fat: $fat g", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!saturatedFat.isNullOrBlank()) Text("Saturated Fat: $saturatedFat g", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!salt.isNullOrBlank()) Text("Salt: $salt g", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!sodium.isNullOrBlank()) Text("Sodium: $sodium g", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        val nutrientLevels = buildList {
            fat?.toDoubleOrNull()?.let { add("Fat" to it) }
            saturatedFat?.toDoubleOrNull()?.let { add("Saturated Fat" to it) }
            sugar?.toDoubleOrNull()?.let { add("Sugars" to it) }
            salt?.toDoubleOrNull()?.let { add("Salt" to it) }
        }
        if (nutrientLevels.isNotEmpty()) {
            InfoCard(
                title = "Nutrient Levels",
                icon = Icons.Outlined.Analytics
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    nutrientLevels.forEach { (label, value) ->
                        val (levelText, levelColor) = classifyNutrientLevel(label, value)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("$label: $value g", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(levelText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = levelColor)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        val additives = product.labelsTags
            ?.filter { it.contains("additive", ignoreCase = true) || it.startsWith("en:e") }
            .orEmpty()
        if (additives.isNotEmpty()) {
            InfoCard(
                title = "Additives",
                icon = Icons.Outlined.Science
            ) {
                Text(
                    text = additives.joinToString(", "),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        InfoCard(
            title = "Matching with Your Preferences",
            icon = Icons.Outlined.VerifiedUser
        ) {
            val halalMatch = if (product.isHalal()) "Halal label ditemukan" else "Perlu verifikasi halal lanjutan"
            val sugarMatch = sugar?.toDoubleOrNull()?.let {
                if (it >= 10.0) "Kadar gula tinggi, batasi konsumsi" else "Kadar gula relatif aman"
            } ?: "Data gula belum lengkap"
            val allergenMatch = if (!product.allergens.isNullOrBlank()) "Ada alergen: ${product.allergens}" else "Tidak ada alergen utama terdeteksi"

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("• $halalMatch", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("• $sugarMatch", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("• $allergenMatch", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        // Allergens
        if (!product.allergens.isNullOrBlank()) {
            InfoCard(
                title = "Allergens",
                icon = Icons.Outlined.Warning
            ) {
                Text(
                    text = product.allergens,
                    fontSize = 13.sp,
                    color = HaramColor
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // Category
        if (!product.categories.isNullOrBlank()) {
            InfoCard(
                title = "Categories",
                icon = Icons.Outlined.Category
            ) {
                Text(
                    text = product.categories,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (!product.quantity.isNullOrBlank()) {
            InfoCard(
                title = "Quantity",
                icon = Icons.Outlined.Inventory
            ) {
                Text(
                    text = product.quantity,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (!product.packaging.isNullOrBlank()) {
            InfoCard(
                title = "Packaging",
                icon = Icons.Outlined.Inbox
            ) {
                Text(
                    text = product.packaging,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (!product.manufacturingPlaces.isNullOrBlank()) {
            InfoCard(
                title = "Manufacturing Places",
                icon = Icons.Outlined.Factory
            ) {
                Text(
                    text = product.manufacturingPlaces,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (!product.origin.isNullOrBlank() || !product.countries.isNullOrBlank()) {
            InfoCard(
                title = "Origin & Countries",
                icon = Icons.Outlined.Public
            ) {
                val value = listOfNotNull(product.origin, product.countries)
                    .filter { it.isNotBlank() }
                    .joinToString(" | ")
                Text(
                    text = value,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (!product.labels.isNullOrBlank()) {
            InfoCard(
                title = "Labels",
                icon = Icons.Outlined.Verified
            ) {
                Text(
                    text = product.labels,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (product.novaGroup != null) {
            InfoCard(
                title = "NOVA Group",
                icon = Icons.Outlined.Analytics
            ) {
                Text(
                    text = "Group ${product.novaGroup}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (!product.stores.isNullOrBlank()) {
            InfoCard(
                title = "Stores",
                icon = Icons.Outlined.Store
            ) {
                Text(
                    text = product.stores,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    onSaveToHistory()
                    isSaved = true
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSaved) MaterialTheme.colorScheme.surfaceVariant else HalalGreen
                ),
                enabled = !isSaved
            ) {
                Icon(
                    if (isSaved) Icons.Filled.Check else Icons.Outlined.BookmarkAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isSaved) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSaved) "Saved" else "Save to History",
                    color = if (isSaved) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                )
            }
            
            OutlinedButton(
                onClick = { /* Report */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = HaramColor
                ),
                border = BorderStroke(1.dp, HaramColor)
            ) {
                Icon(
                    Icons.Outlined.Report,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Report Issue")
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = HalalGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

private fun classifyNutrientLevel(label: String, value: Double): Pair<String, Color> {
    return when (label.lowercase()) {
        "salt" -> when {
            value >= 1.5 -> "High" to Color(0xFFDC2626)
            value >= 0.3 -> "Moderate" to Color(0xFFD97706)
            else -> "Low" to Color(0xFF16A34A)
        }
        "sugars" -> when {
            value >= 10.0 -> "High" to Color(0xFFDC2626)
            value >= 5.0 -> "Moderate" to Color(0xFFD97706)
            else -> "Low" to Color(0xFF16A34A)
        }
        else -> when {
            value >= 17.5 -> "High" to Color(0xFFDC2626)
            value >= 3.0 -> "Moderate" to Color(0xFFD97706)
            else -> "Low" to Color(0xFF16A34A)
        }
    }
}

@Composable
private fun LabelBadge(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

private fun getNutriscoreColor(grade: String): Color {
    return when (grade.uppercase()) {
        "A" -> Color(0xFF00A651)
        "B" -> Color(0xFF85BB2F)
        "C" -> Color(0xFFFECB00)
        "D" -> Color(0xFFEF8200)
        "E" -> Color(0xFFE63E11)
        else -> Color.Gray
    }
}
