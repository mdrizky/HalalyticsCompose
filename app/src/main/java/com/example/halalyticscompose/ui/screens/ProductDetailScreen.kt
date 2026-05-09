package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.halalyticscompose.data.model.Product
import com.example.halalyticscompose.data.model.HalalStatus
import com.example.halalyticscompose.data.model.NutrientLevels
import com.example.halalyticscompose.data.model.NutrientLevel
import com.example.halalyticscompose.data.model.HalalInfo
import com.example.halalyticscompose.data.model.AIConfidence
import com.example.halalyticscompose.ui.theme.*
import com.example.halalyticscompose.ui.viewmodel.MainViewModel
import com.example.halalyticscompose.ui.viewmodel.ProductUiState
import com.example.halalyticscompose.ui.viewmodel.AlternativesUiState
import com.example.halalyticscompose.ui.viewmodel.ProductViewModel
import com.example.halalyticscompose.ui.components.InfoRow
import com.example.halalyticscompose.ui.components.ConfidenceBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    barcode: String,
    mainViewModel: MainViewModel,
    viewModel: ProductViewModel = hiltViewModel(),
    compareViewModel: com.example.halalyticscompose.ui.viewmodel.CompareViewModel = hiltViewModel(),
    favoritesViewModel: com.example.halalyticscompose.ui.viewmodel.FavoritesViewModel = hiltViewModel()
) {
    val productState by viewModel.productState.collectAsState()
    val compareQueue by compareViewModel.comparisonQueue.collectAsState()
    val context = LocalContext.current
    val token by mainViewModel.accessToken.collectAsState()
    var scanSaved by remember { mutableStateOf(false) }

    LaunchedEffect(barcode, token) {
        val database = com.example.halalyticscompose.data.local.HalalyticsDatabase.getDatabase(context)
        viewModel.loadProduct(barcode)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Product Intelligence", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.padding(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(0.05f))) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }, modifier = Modifier.padding(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(0.05f))) {
                        Icon(Icons.Default.Share, "Share", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = productState) {
                is ProductUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is ProductUiState.Success -> {
                    // Auto save logic
                    LaunchedEffect(state.product.barcode) {
                        if (!scanSaved && state.product.barcode == barcode) {
                            val halalStatus = when (state.product.halalInfo?.halalStatus) {
                                HalalStatus.HALAL -> "halal"; HalalStatus.NON_HALAL -> "haram"; else -> "syubhat"
                            }
                            // Scan result saved to history via backend automatically
                            scanSaved = true
                        }
                    }

                    val alternativesState by viewModel.alternativesState.collectAsState()

                    ProductDetailContentPremium(
                        product = state.product,
                        mainViewModel = mainViewModel,
                        alternativesState = alternativesState,
                        onRecheckHalal = { viewModel.recheckHalalStatus(state.product) },
                        onSaveToFavorites = {
                            favoritesViewModel.toggleFavorite(barcode)
                        },
                        onAddToCompare = { 
                            compareViewModel.addToCompare(state.product.barcode)
                            android.widget.Toast.makeText(context, "Added to Contrast Analysis ⚖️", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        compareCount = compareQueue.size,
                        onViewComparison = { navController.navigate("compare_products") },
                        onReportIssue = { navController.navigate("report_issue/${state.product.id}/${state.product.name}") },
                        onLoadAlternatives = { viewModel.loadAlternatives(state.product.barcode) }
                    )
                }
                is ProductUiState.Error -> {
                    ProductNotFoundFallback(
                        message = state.message,
                        barcode = barcode,
                        onRetry = { viewModel.loadProduct(barcode) },
                        onOpenContribution = { navController.navigate("product_request/$barcode") },
                        onOpenOcr = { navController.navigate("enhanced_ocr") }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductNotFoundFallback(
    message: String,
    barcode: String,
    onRetry: () -> Unit,
    onOpenContribution: () -> Unit,
    onOpenOcr: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Produk belum ada di database",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (message.isNotBlank()) message else "Silakan kirim pengaduan agar admin bisa verifikasi.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            if (barcode.isNotBlank()) {
                Text(
                    "Barcode: $barcode",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onOpenContribution,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Laporkan & Verifikasi Admin")
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onOpenOcr,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Buka OCR (Foto Depan & Belakang)")
            }
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onRetry) {
                Text("Coba Lagi")
            }
        }
    }
}

@Composable
fun ProductDetailContentPremium(
    product: Product,
    mainViewModel: MainViewModel,
    alternativesState: AlternativesUiState,
    onRecheckHalal: () -> Unit,
    onSaveToFavorites: () -> Unit,
    onAddToCompare: () -> Unit,
    compareCount: Int,
    onViewComparison: () -> Unit,
    onReportIssue: () -> Unit,
    onLoadAlternatives: () -> Unit
) {
    val familyProfiles by mainViewModel.familyProfiles.collectAsState()
    val selectedProfile by mainViewModel.selectedFamilyProfile.collectAsState()
    val currentAllergies = selectedProfile?.allergies ?: ""
    val currentHealthName = selectedProfile?.name ?: "Myself"
    
    val allergyList = currentAllergies.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val foundAllergens = allergyList.filter { allergy ->
        product.ingredientsText?.contains(allergy, ignoreCase = true) == true ||
        product.allergens?.any { it.contains(allergy, ignoreCase = true) } == true
    }

    LaunchedEffect(product.halalInfo?.halalStatus) {
        if (product.halalInfo?.halalStatus != HalalStatus.HALAL) {
            onLoadAlternatives()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // Hero Image
        item {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                val fallbackUrl = "https://ui-avatars.com/api/?name=${product.name}&background=1E293B&color=3B82F6&size=400"
                AsyncImage(
                    model = if (!product.imageFrontUrl.isNullOrBlank()) product.imageFrontUrl else fallbackUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background))))
                
                // Status Badge overlay
                HalalStatusBadgePremium(
                    status = product.halalInfo?.halalStatus ?: HalalStatus.UNKNOWN,
                    modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)
                )
            }
        }

        // Profile Context & Allergies
        item {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text("ANALYSIS CONTEXT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { ProfileChipPremium("Myself", selectedProfile == null) { mainViewModel.selectFamilyProfile(null) } }
                    items(familyProfiles) { profile ->
                        ProfileChipPremium(profile.name, selectedProfile?.id == profile.id) { mainViewModel.selectFamilyProfile(profile) }
                    }
                }
                
                if (foundAllergens.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    AllergyAlertPremium(currentHealthName, foundAllergens)
                }
            }
        }

        // Product Identity
        item {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(product.brand.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), fontWeight = FontWeight.Bold)
                Text(product.name, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCode, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(product.barcode, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                }
            }
        }

        // Action Hub
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailActionButton(Icons.Default.FavoriteBorder, "Favorite", MaterialTheme.colorScheme.primary, Modifier.weight(1f)) { onSaveToFavorites() }
                DetailActionButton(Icons.AutoMirrored.Filled.CompareArrows, "Compare", Color(0xFF3B82F6), Modifier.weight(1f)) { onAddToCompare() }
                DetailActionButton(Icons.Default.Refresh, "Check", MaterialTheme.colorScheme.onSurface, Modifier.weight(1f)) { onRecheckHalal() }
            }
        }

        // Science & Health Section
        item {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("SCIENCE & HEALTH", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AttributeCardPremium("Nutri-Score", product.nutriScore ?: "N/A", Modifier.weight(1f))
                    AttributeCardPremium("NOVA Group", product.novaGroup?.toString() ?: "N/A", Modifier.weight(1f))
                }
                
                Spacer(Modifier.height(24.dp))
                if (product.halalInfo?.source != null) {
                    ConfidenceBadge(score = 0.9, level = "high", message = "Validated via ${product.halalInfo?.source?.replace("_", " ")}")
                }
            }
        }

        // Nutrition Insights
        product.nutrientLevels?.let { levels ->
            item {
                NutrientLevelSuitePremium(levels)
            }
        }

        // Ingredients Breakdown
        item {
            IngredientsCardPremium(product.ingredientsText ?: "Ingredients data unavailable.")
        }

        // AI Alternatives
        if (product.halalInfo?.halalStatus != HalalStatus.HALAL) {
            item {
                AlternativesSectionPremium(alternativesState)
            }
        }
        
        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Composable
fun HalalStatusBadgePremium(status: HalalStatus, modifier: Modifier = Modifier) {
    val (color, label, textColor) = when (status) {
        HalalStatus.HALAL -> Triple(MaterialTheme.colorScheme.primary, "HALAL CERTIFIED", MaterialTheme.colorScheme.onPrimary)
        HalalStatus.NON_HALAL -> Triple(MaterialTheme.colorScheme.error, "HARAM / NON-HALAL", MaterialTheme.colorScheme.onError)
        else -> Triple(MushboohYellow, "SYUBHAT / UNKNOWN", Color.Black)
    }
    Box(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(color).padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(label, color = textColor, fontWeight = FontWeight.Black, fontSize = 12.sp)
    }
}

@Composable
fun AllergyAlertPremium(name: String, allergens: List<String>) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.error.copy(0.1f)).border(1.dp, MaterialTheme.colorScheme.error.copy(0.3f), RoundedCornerShape(20.dp)).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(16.dp))
            Column {
                Text("ALLERGY DETECTED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Black)
                Text("$name is allergic to: ${allergens.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun AttributeCardPremium(label: String, value: String, modifier: Modifier) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.05f), RoundedCornerShape(24.dp)).padding(20.dp)
    ) {
        Column {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun DetailActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.05f), RoundedCornerShape(16.dp)).clickable { onClick() }.padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NutrientLevelSuitePremium(levels: NutrientLevels) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
        Text("NUTRIENT LEVELS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(16.dp))
        Box(modifier = Modifier.clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.05f), RoundedCornerShape(28.dp)).padding(24.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                levels.fat?.let { fatVal -> NutrientProgressPremium("Fat", fatVal) }
                levels.saturatedFat?.let { satFatVal -> NutrientProgressPremium("Saturated Fat", satFatVal) }
                levels.sugars?.let { sugarVal -> NutrientProgressPremium("Sugars", sugarVal) }
                levels.salt?.let { saltVal -> NutrientProgressPremium("Salt", saltVal) }
            }
        }
    }
}

@Composable
fun NutrientProgressPremium(label: String, level: NutrientLevel) {
    val color = when (level.level.lowercase()) {
        "low" -> MaterialTheme.colorScheme.primary; "moderate" -> MushboohYellow; else -> MaterialTheme.colorScheme.error
    }
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), fontSize = 12.sp)
            Text("${level.value} ${level.unit} (${level.level.uppercase()})", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { 0.7f }, // Placeholder logic
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(0.05f)
        )
    }
}

@Composable
fun IngredientsCardPremium(ingredients: String) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text("INGREDIENTS BREAKDOWN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(16.dp))
        Box(modifier = Modifier.clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.05f), RoundedCornerShape(28.dp)).padding(24.dp)) {
            Text(ingredients, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.7f), lineHeight = 24.sp)
        }
    }
}

@Composable
fun AlternativesSectionPremium(state: AlternativesUiState) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Science, contentDescription = "AI", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("AI HALAL ALTERNATIVES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(16.dp))

        when (state) {
            is AlternativesUiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
            }
            is AlternativesUiState.Success -> {
                val response = state.response
                
                // Show reasoning
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.error.copy(0.1f)).border(1.dp, MaterialTheme.colorScheme.error.copy(0.3f), RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Column {
                        Text("Concern detected:", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(response.problematic_ingredients_reason, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))
                
                // Show alternatives list
                LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(response.halal_alternatives) { alternative ->
                        Box(modifier = Modifier.width(220.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.05f), RoundedCornerShape(24.dp)).padding(20.dp)) {
                            Column {
                                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primary.copy(0.1f)).padding(12.dp), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CheckCircle, "Halal", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                }
                                Spacer(Modifier.height(16.dp))
                                Text(alternative.brand ?: alternative.manufacturer, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), fontWeight = FontWeight.Bold)
                                Text(alternative.name, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Black, fontSize = 16.sp, lineHeight = 20.sp)
                                Spacer(Modifier.height(12.dp))
                                Text(alternative.reason_it_is_better, color = MaterialTheme.colorScheme.primary.copy(alpha=0.8f), fontSize = 12.sp, lineHeight = 16.sp)
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                // Explanation
                Text(response.explanation, modifier = Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.onSurface.copy(0.6f), fontSize = 12.sp, style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
            }
            is AlternativesUiState.Error -> {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.onSurface.copy(0.05f)).padding(16.dp)) {
                    Text("Could not load AI alternatives: ${state.message}", color = MaterialTheme.colorScheme.onSurface.copy(0.6f), fontSize = 12.sp)
                }
            }
            else -> {}
        }
    }
}
