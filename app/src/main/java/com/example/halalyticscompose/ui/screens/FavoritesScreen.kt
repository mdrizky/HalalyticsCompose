package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.halalyticscompose.ui.viewmodel.FavoritesViewModel
import com.example.halalyticscompose.ui.components.*
import androidx.compose.ui.res.stringResource
import com.example.halalyticscompose.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favoriteProducts by viewModel.favoriteProducts.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Semua") }
    
    val filterOptions = listOf("Semua", "Halal", "Haram", "Syubhat")
    
    val filteredProducts = favoriteProducts.filter { product ->
        val matchesSearch = product.name.contains(searchQuery, ignoreCase = true) ||
            product.brand?.contains(searchQuery, ignoreCase = true) == true
        val matchesFilter = when (selectedFilter) {
            "Semua" -> true
            else -> product.status.contains(selectedFilter, ignoreCase = true)
        }
        matchesSearch && matchesFilter
    }
    
    // Filter Dialog
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text(stringResource(R.string.favorites_filter_title)) },
            text = {
                Column {
                    filterOptions.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { 
                                selectedFilter = filter
                                showFilterDialog = false
                            },
                            label = { Text(filter) },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text(stringResource(R.string.common_close))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.favorites_title),
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(stringResource(R.string.favorites_search_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.bottom_nav_search))
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Statistics Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.favorites_stats_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.favorites_stats_total, favoriteProducts.size),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                filteredProducts.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (searchQuery.isEmpty()) {
                            EmptyFavoritesView(
                                onBrowse = { navController.navigate("home") }
                            )
                        } else {
                            EmptySearchView()
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        items(
                            items = filteredProducts,
                            key = { it.barcode }
                        ) { product ->
                            SwipeableProductCard(
                                product = product,
                                onDelete = { viewModel.deleteProduct(product.barcode) },
                                onFavoriteClick = { viewModel.toggleFavorite(product.barcode) },
                                onCardClick = { 
                                    navController.navigate("product_detail/${product.barcode}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
