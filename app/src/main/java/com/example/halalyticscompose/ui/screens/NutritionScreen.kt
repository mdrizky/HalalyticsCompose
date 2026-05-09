package com.example.halalyticscompose.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.halalyticscompose.data.model.DailyNutritionLog
import com.example.halalyticscompose.data.model.NutritionHistoryItem
import com.example.halalyticscompose.ui.viewmodel.NutritionUiState
import com.example.halalyticscompose.ui.viewmodel.NutritionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    onNavigateBack: () -> Unit,
    viewModel: NutritionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showMealPicker by remember { mutableStateOf(false) }
    var selectedMealType by remember { mutableStateOf("makan_siang") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let { selected ->
            viewModel.logMeal(selected, selectedMealType, context)
        }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            viewModel.loadHistory()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catatan Nutrisi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        selectedTab = 1
                        viewModel.loadHistory()
                    }) {
                        Text("Riwayat")
                    }
                },
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = { showMealPicker = true }) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = "Tambah Foto Makanan")
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Hari Ini") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Kalender") })
            }

            when (selectedTab) {
                0 -> DailyNutritionTab(
                    uiState = uiState,
                    onLogMeal = { showMealPicker = true },
                )

                else -> NutritionHistoryTab(history = uiState.history)
            }
        }

        if (uiState.isAnalyzing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                Card(shape = RoundedCornerShape(24.dp)) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Menganalisis foto makananmu...")
                        Text(
                            "AI sedang menghitung kalori dan makro",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }

    if (showMealPicker) {
        MealTypePickerSheet(
            onSelect = { mealType ->
                selectedMealType = mealType
                showMealPicker = false
                imagePickerLauncher.launch("image/*")
            },
            onDismiss = { showMealPicker = false },
        )
    }
}

@Composable
private fun DailyNutritionTab(
    uiState: NutritionUiState,
    onLogMeal: () -> Unit,
) {
    val summary = uiState.dailySummary
    val goal = summary?.goal

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(4.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Kalori Hari Ini",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val consumed = (summary?.totalCalories ?: 0).toFloat()
                    val target = (goal?.dailyCalories ?: 2000).toFloat()
                    val progress = if (target > 0) (consumed / target).coerceIn(0f, 1f) else 0f

                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(140.dp),
                            strokeWidth = 12.dp,
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${consumed.toInt()}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("/ ${target.toInt()} kkal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        MacroItem("Karbo", summary?.totalCarbs?.toFloat() ?: 0f, goal?.dailyCarbs?.toFloat() ?: 250f, Color(0xFF1976D2))
                        MacroItem("Protein", summary?.totalProtein?.toFloat() ?: 0f, goal?.dailyProtein?.toFloat() ?: 60f, Color(0xFF2E7D32))
                        MacroItem("Lemak", summary?.totalFat?.toFloat() ?: 0f, goal?.dailyFat?.toFloat() ?: 65f, Color(0xFFF57C00))
                    }
                }
            }
        }

        if (summary?.logs?.isNullOrEmpty() != false) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Belum ada catatan makan hari ini", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onLogMeal,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Foto Makanan")
                        }
                    }
                }
            }
        } else {
            item {
                Text("Makanan Hari Ini", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(summary.logs) { log ->
                MealLogCard(log)
            }
        }
    }
}

@Composable
private fun MacroItem(
    label: String,
    current: Float,
    target: Float,
    color: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("${current.toInt()}g", fontWeight = FontWeight.Bold, color = color)
        LinearProgressIndicator(
            progress = { if (target > 0) (current / target).coerceIn(0f, 1f) else 0f },
            modifier = Modifier
                .width(68.dp)
                .height(6.dp),
            color = color,
            trackColor = color.copy(alpha = 0.18f),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MealLogCard(log: DailyNutritionLog) {
    val mealLabel = when (log.mealType) {
        "sarapan" -> "Sarapan"
        "makan_siang" -> "Makan Siang"
        "makan_malam" -> "Makan Malam"
        "camilan" -> "Camilan"
        else -> log.mealType
    }

    Card(shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(14.dp)) {
            AsyncImage(
                model = log.imagePath,
                contentDescription = log.mealType,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(mealLabel, fontWeight = FontWeight.Bold)
                    Text("${log.totalCalories} kkal", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                log.foodItems.orEmpty().forEach { item ->
                    Text("• ${item.name} · ${item.weightGram}g", style = MaterialTheme.typography.bodySmall)
                    if (!item.isHalal && !item.halalNote.isNullOrBlank()) {
                        Text("Perhatian: ${item.halalNote}", color = Color(0xFFF57C00), style = MaterialTheme.typography.labelSmall)
                    }
                }
                if (!log.analysisNote.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(log.analysisNote, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun NutritionHistoryTab(history: List<NutritionHistoryItem>) {
    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Belum ada riwayat nutrisi.")
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(history) { item ->
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(item.date, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("${item.totalCalories} kkal · ${item.mealCount} kali makan")
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Karbo ${item.totalCarbs.toInt()}g • Protein ${item.totalProtein.toInt()}g • Lemak ${item.totalFat.toInt()}g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealTypePickerSheet(
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val mealTypes = listOf(
        "sarapan" to "Sarapan",
        "makan_siang" to "Makan Siang",
        "makan_malam" to "Makan Malam",
        "camilan" to "Camilan",
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Pilih waktu makan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            mealTypes.forEach { (value, label) ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(value) },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(8.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(label, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
