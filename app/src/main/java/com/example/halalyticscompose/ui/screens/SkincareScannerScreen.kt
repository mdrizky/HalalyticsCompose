package com.example.halalyticscompose.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.halalyticscompose.data.api.BeautyProduct
import com.example.halalyticscompose.data.api.bestIngredientsText
import com.example.halalyticscompose.data.model.*
import com.example.halalyticscompose.ui.theme.*
import com.example.halalyticscompose.ui.viewmodel.MainViewModel
import com.example.halalyticscompose.ui.viewmodel.SkincareViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkincareScannerScreen(
    navController: NavController,
    viewModel: SkincareViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    val familyProfiles by mainViewModel.familyProfiles.collectAsState()
    val selectedProfile by mainViewModel.selectedFamilyProfile.collectAsState()

    LaunchedEffect(Unit) {
        mainViewModel.fetchFamilyProfiles()
    }

    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()

    val analysisResult by viewModel.analysisResult.collectAsState()
    val ingredientIndicators by viewModel.ingredientIndicators.collectAsState()
    val analysisSummary by viewModel.analysisSummary.collectAsState()
    val analysisDisclaimer by viewModel.analysisDisclaimer.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var selectedProduct by remember { mutableStateOf<BeautyProduct?>(null) }
    
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(analysisResult) {
        if (analysisResult != null) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Beauty & Skincare AI", style = MaterialTheme.typography.titleLarge)
                        Text("Global OpenBeautyFacts Database", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(0.05f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF8B5CF6), Color(0xFFD946EF)) // Purple-Pink Gradient
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Icon(Icons.Default.FaceRetouchingNatural, null, tint = Color.White, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Safe Beauty Analysis", 
                            style = MaterialTheme.typography.headlineSmall, 
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Scan or search beauty products to verify dermatological safety and halal status.", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = Color.White.copy(0.8f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            
            // Results or Search
            if (analysisResult != null && !isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(0.05f))
                            .clickable { viewModel.clearResults() }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Back to Search", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
                
                item {
                    SkincareAnalysisResultPremium(
                        analysis = analysisResult!!,
                        summaryOverride = analysisSummary,
                        disclaimerOverride = analysisDisclaimer
                    )
                }

                if (ingredientIndicators.isNotEmpty() || !analysisResult!!.bahanTerdeteksi.isNullOrEmpty()) {
                    item {
                        Text(
                            "Ingredient Intelligence",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }
                    if (ingredientIndicators.isNotEmpty()) {
                        items(ingredientIndicators) { ingredient ->
                            SkincareIngredientIndicatorItem(ingredient = ingredient)
                        }
                    } else {
                        items(analysisResult!!.bahanTerdeteksi!!) { ingredient ->
                            SkincareIngredientItem(ingredient = ingredient)
                        }
                    }
                }
            } else {
                // Profile & Search UI
                item {
                    Text(
                        "Analysis Context",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        fontWeight = FontWeight.Bold
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        item {
                            ProfileChipPremium(
                                name = "Personal",
                                isSelected = selectedProfile == null,
                                onClick = { mainViewModel.selectFamilyProfile(null) }
                            )
                        }
                        items(familyProfiles.size) { index ->
                            val profile = familyProfiles[index]
                            ProfileChipPremium(
                                name = profile.name,
                                isSelected = selectedProfile?.id == profile.id,
                                onClick = { mainViewModel.selectFamilyProfile(profile) }
                            )
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.05f), RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search brand or product name...", color = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(0.1f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { if (searchQuery.isNotBlank()) viewModel.searchSkincare(searchQuery) })
                            )

                            Spacer(Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (searchQuery.isNotBlank() && !isLoading) 
                                            Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                                        else 
                                            Brush.linearGradient(listOf(MaterialTheme.colorScheme.onSurface.copy(0.05f), MaterialTheme.colorScheme.onSurface.copy(0.05f)))
                                    )
                                    .clickable(enabled = searchQuery.isNotBlank() && !isLoading) { viewModel.searchSkincare(searchQuery) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                } else {
                                    Text("Find Product", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    item {
                        ErrorMessageCardSmall(errorMessage!!)
                    }
                }

                if (searchResults.isEmpty() && !isLoading && errorMessage == null) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.1f), modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Access over 100k+ global skincare database", color = MaterialTheme.colorScheme.onSurface.copy(0.6f), textAlign = TextAlign.Center)
                        }
                    }
                } else if (!isLoading) {
                    item {
                        Text(
                            "Search Results (${searchResults.size})", 
                            style = MaterialTheme.typography.titleMedium, 
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }
                    items(searchResults) { product ->
                        BeautyProductPremiumCard(
                            product = product,
                            onClick = {
                                selectedProduct = product
                            }
                        )
                    }
                }
            }
        }
    }

    if (selectedProduct != null) {
        val product = selectedProduct!!
        val ingredients = product.bestIngredientsText
        ModalBottomSheet(
            onDismissRequest = { selectedProduct = null },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = product.productName ?: "Beauty Product",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (!product.brands.isNullOrBlank()) {
                    Text(
                        text = product.brands ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                    )
                }
                Spacer(Modifier.height(12.dp))

                ProductInfoRow("Kategori", product.categories)
                ProductInfoRow("Negara", product.countries)
                ProductInfoRow("Kemasan", product.packaging)
                ProductInfoRow("Jumlah", product.quantity)
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Ingredients",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = ingredients ?: "Data ingredients belum tersedia dari OpenBeautyFacts untuk produk ini.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.8f)
                )
                Spacer(Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (!ingredients.isNullOrBlank()) {
                            viewModel.analyzeIngredients(
                                ingredientsText = ingredients,
                                productName = product.productName ?: "Beauty Product",
                                familyId = selectedProfile?.id
                            )
                            selectedProduct = null
                        }
                    },
                    enabled = !ingredients.isNullOrBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Analyze with AI")
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
fun SkincareAnalysisResultPremium(
    analysis: SkincareAnalysis,
    summaryOverride: String? = null,
    disclaimerOverride: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.05f), RoundedCornerShape(28.dp))
            .padding(24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Safety Score", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    Text(
                        if (analysis.skorKeamanan != null) "${analysis.skorKeamanan}/100" else "N/A",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if ((analysis.skorKeamanan ?: 0) > 70) MaterialTheme.colorScheme.primary else if ((analysis.skorKeamanan ?: 0) > 40) MushboohYellow else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Black
                    )
                }
                
                // Status Badges
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusBadge(
                        label = analysis.statusHalal ?: "Syubhat",
                        color = when (analysis.statusHalal?.lowercase()) {
                            "halal" -> MaterialTheme.colorScheme.primary; "haram" -> MaterialTheme.colorScheme.error; else -> MushboohYellow
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(0.05f))
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("AI Dermatologist Summary", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                summaryOverride ?: analysis.ringkasan ?: "Detailed analysis under process...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(0.8f),
                lineHeight = 22.sp
            )

            if (!disclaimerOverride.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    disclaimerOverride,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )
            }
            
            if (!analysis.bahanBerbahaya.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.error.copy(0.1f))
                        .border(1.dp, MaterialTheme.colorScheme.error.copy(0.2f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Harmful Red Flags", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                        analysis.bahanBerbahaya.forEach { Text("• $it", color = MaterialTheme.colorScheme.onSurface.copy(0.8f), fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun SkincareIngredientIndicatorItem(ingredient: IngredientIndicator) {
    val level = ingredient.safetyLevel?.lowercase() ?: "safe"
    val color = when (ingredient.colorCode?.lowercase()) {
        "red" -> MaterialTheme.colorScheme.error
        "yellow" -> MushboohYellow
        else -> MaterialTheme.colorScheme.primary
    }
    val label = when (level) {
        "danger" -> "Berisiko"
        "warning" -> "Perlu Perhatian"
        else -> "Aman"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, color.copy(0.18f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    ingredient.name ?: "-",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(label = label, color = color)
            }
            if (!ingredient.halalStatus.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Halal: ${ingredient.halalStatus}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                )
            }
            if (!ingredient.reason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    ingredient.reason ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(0.1f))
            .border(1.dp, color.copy(0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label.uppercase(), color = color, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun ProfileChipPremium(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(0.05f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(name, color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface.copy(0.6f), fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun BeautyProductPremiumCard(product: BeautyProduct, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.05f), RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.onSurface.copy(0.05f))
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(model = product.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Image, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.2f))
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(product.productName ?: "Unknown Product", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(product.brands ?: "Various Brands", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (product.bestIngredientsText.isNullOrBlank()) {
                    Text("Detail only (no ingredients)", color = MushboohYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Ready", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.2f))
        }
    }
}

@Composable
private fun ProductInfoRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(84.dp)
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
fun SkincareIngredientItem(ingredient: IngredientInfo) {
    val level = ingredient.tingkatBahaya ?: 1
    val color = when {
        level > 7 -> MaterialTheme.colorScheme.error
        level > 4 -> MushboohYellow
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, color.copy(0.1f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                Spacer(modifier = Modifier.width(12.dp))
                Text(ingredient.nama ?: "Secret Ingredient", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                Text("Lvl $level", color = color, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
            if (!ingredient.fungsi.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(ingredient.fungsi!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            }
        }
    }
}

@Composable
fun ErrorMessageCardSmall(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.error.copy(0.1f)).padding(12.dp)
    ) {
        Text(message, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}
