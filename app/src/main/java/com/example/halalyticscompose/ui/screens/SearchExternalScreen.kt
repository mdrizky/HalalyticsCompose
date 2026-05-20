package com.example.halalyticscompose.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.halalyticscompose.data.model.ProductItem
import com.example.halalyticscompose.ui.viewmodel.ProductExternalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchExternalScreen(
    navController: NavController,
    initialQuery: String = "",
    viewModel: ProductExternalViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current
    val colors = MaterialTheme.colorScheme
    
    // States from ViewModel
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchError by viewModel.searchError.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    
    // Local states
    var searchQuery by remember { mutableStateOf(initialQuery) }
    var selectedFilter by remember { mutableStateOf("All") }

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotEmpty()) {
            viewModel.applyFilter(selectedFilter, initialQuery)
        }
    }
    
    val filters = listOf("All", "Halal", "Vegetarian", "Vegan")
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Global Search",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.onBackground
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Result count badge
                if (totalCount > 0) {
                    Box(
                        modifier = Modifier
                            .background(colors.primary, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$totalCount found",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.onPrimary
                        )
                    }
                }
            }
            
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                placeholder = {
                    Text(
                        "Search products worldwide...",
                        color = colors.onSurfaceVariant.copy(0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = colors.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { 
                            searchQuery = ""
                            viewModel.clearSearchResults()
                        }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.outlineVariant,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    cursorColor = colors.primary,
                    focusedTextColor = colors.onSurface,
                    unfocusedTextColor = colors.onSurface
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        if (searchQuery.isNotEmpty()) {
                            viewModel.applyFilter(selectedFilter, searchQuery)
                        }
                    }
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = {
                            selectedFilter = filter
                            if (searchQuery.isNotEmpty()) {
                                viewModel.applyFilter(filter, searchQuery)
                            }
                        },
                        label = {
                            Text(
                                text = filter,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            when (filter) {
                                "Halal" -> Icon(
                                    Icons.Outlined.Verified,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                "Vegetarian" -> Icon(
                                    Icons.Outlined.Grass,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                "Vegan" -> Icon(
                                    Icons.Outlined.Eco,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                else -> null
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.primaryContainer,
                            selectedLabelColor = colors.onPrimaryContainer,
                            selectedLeadingIconColor = colors.onPrimaryContainer,
                            containerColor = colors.surfaceVariant,
                            labelColor = colors.onSurfaceVariant,
                            iconColor = colors.onSurfaceVariant
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Content Area
            when {
                // Loading state
                isSearching -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = colors.primary,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Searching products...",
                                fontSize = 14.sp,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Error state
                searchError.isNotEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = colors.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = searchError,
                                fontSize = 14.sp,
                                color = colors.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.applyFilter(selectedFilter, searchQuery) },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                            ) {
                                Text("Retry", color = colors.onPrimary)
                            }
                        }
                    }
                }
                
                // Empty state (no search yet)
                searchResults.isEmpty() && searchQuery.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Public,
                                contentDescription = null,
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Search Global Products",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Search millions of products from\nOpenFoodFacts database worldwide",
                                fontSize = 13.sp,
                                color = colors.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Popular searches
                            Text(
                                text = "Try searching:",
                                fontSize = 12.sp,
                                color = colors.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Coca Cola", "Indomie", "Nestlé").forEach { suggestion ->
                                    SuggestionChip(
                                        onClick = {
                                            searchQuery = suggestion
                                            viewModel.applyFilter(selectedFilter, suggestion)
                                        },
                                        label = { Text(suggestion, fontSize = 12.sp) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = colors.surfaceVariant,
                                            labelColor = colors.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                
                // No results found
                searchResults.isEmpty() && searchQuery.isNotEmpty() && !isSearching -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Outlined.SearchOff,
                                contentDescription = null,
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Produk tidak ditemukan",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onBackground
                            )
                            Text(
                                text = "Bantu kami dengan melaporkan produk ini.",
                                fontSize = 14.sp,
                                color = colors.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { 
                                    val encodedName = Uri.encode(searchQuery)
                                    navController.navigate("contribution?barcode=&name=$encodedName")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Laporkan Produk")
                            }
                        }
                    }
                }
                
                // Results list
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(searchResults) { product ->
                            ProductItemCard(
                                product = product,
                                onClick = {
                                    product.code?.let { barcode ->
                                        navController.navigate("product_external_detail/$barcode")
                                    }
                                }
                            )
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductItemCard(
    product: ProductItem,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val halalStatus = product.getHalalStatus()
    val statusColor = product.getHalalStatusColor()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val imageUrl = product.getBestImageUrl()
                val fallbackUrl = "https://ui-avatars.com/api/?name=${product.getDisplayName()}&background=1E293B&color=3B82F6&size=100"
                AsyncImage(
                    model = imageUrl ?: fallbackUrl,
                    contentDescription = product.getDisplayName(),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Nutriscore badge
                product.nutriscoreGrade?.let { grade ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .size(20.dp)
                            .background(
                                getNutriscoreColor(grade),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = grade.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Product Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.getDisplayName(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                product.brands?.let { brands ->
                    Text(
                        text = brands,
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Labels row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Halal status badge
                    Box(
                        modifier = Modifier
                            .background(
                                statusColor.copy(alpha = 0.15f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = halalStatus,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = statusColor
                        )
                    }
                    
                    // Vegetarian/Vegan badges
                    if (product.isVegetarian()) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Color(0xFF4CAF50).copy(alpha = 0.15f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "VEG",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                    
                    if (product.isVegan()) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Color(0xFF8BC34A).copy(alpha = 0.15f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "VEGAN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF8BC34A)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Arrow
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Get Nutriscore color based on grade
 */
private fun getNutriscoreColor(grade: String): Color {
    return when (grade.uppercase()) {
        "A" -> Color(0xFF00A651) // Dark Green
        "B" -> Color(0xFF85BB2F) // Light Green
        "C" -> Color(0xFFFECB00) // Yellow
        "D" -> Color(0xFFEF8200) // Orange
        "E" -> Color(0xFFE63E11) // Red
        else -> Color.Gray
    }
}
