package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.halalyticscompose.viewmodel.AnalysisResult
import com.example.halalyticscompose.viewmodel.ManualInputViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualInputScreen(
    navController: NavController,
    viewModel: ManualInputViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var productName by remember { mutableStateOf("") }
    var brandName by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Makanan") }
    var expanded by remember { mutableStateOf(false) }

    val categories = listOf("Makanan", "Minuman", "Kosmetik", "Obat", "Suplemen")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Input Manual Produk") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Nama Produk
            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Nama Produk *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Nama Brand
            OutlinedTextField(
                value = brandName,
                onValueChange = { brandName = it },
                label = { Text("Nama Brand") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Kategori Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                category = item
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Komposisi / Ingredients
            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                label = { Text("Komposisi / Ingredients") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 8,
                placeholder = { Text("Contoh: Air, Gula, Tepung, E621, ...") }
            )

            // Loading atau Error State
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.error ?: "",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                uiState.result != null -> {
                    // Tampilkan hasil analisis AI
                    AIResultCard(result = uiState.result!!)
                }
            }

            // Tombol Analisis
            Button(
                onClick = {
                    if (productName.isNotBlank()) {
                        viewModel.analyzeProduct(
                            name = productName,
                            brand = brandName,
                            ingredients = ingredients,
                            category = category
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = productName.isNotBlank() && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Analisis dengan AI", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        } // end Column
    } // end Scaffold content
} // end ManualInputScreen

@Composable
private fun AIResultCard(result: AnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Hasil Analisis AI",
                style = MaterialTheme.typography.titleMedium
            )
            HorizontalDivider()
            Text(text = "Status Halal: ${result.halalStatus}")
            Text(text = "Halal Score: ${result.halalScore}/100")
            Text(text = "Status Kesehatan: ${result.healthStatus}")
            Text(text = "Health Score: ${result.healthScore}/100")
            if (result.personalizedMessage.isNotBlank()) {
                Text(
                    text = result.personalizedMessage,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
