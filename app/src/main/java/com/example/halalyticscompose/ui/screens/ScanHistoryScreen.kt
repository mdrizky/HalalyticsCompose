package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.halalyticscompose.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.halalyticscompose.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.halalyticscompose.data.model.ScanHistoryItem
import com.example.halalyticscompose.ui.viewmodel.ScanHistoryViewModel
import com.example.halalyticscompose.utils.SessionManager
import com.example.halalyticscompose.utils.toRelativeTime
import android.widget.Toast
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.halalyticscompose.utils.ImageUtils
import com.example.halalyticscompose.ui.components.*


// ═══════════════════════════════════════════════════════════════════
// COLOR CONSTANTS — Emerald Forest
// ═══════════════════════════════════════════════════════════════════
// Color Constants moved to theme-aware components

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ScanHistoryScreen(
    navController: NavController,
    viewModel: ScanHistoryViewModel = hiltViewModel(),
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context) }
    val token = sessionManager.getAuthToken() ?: ""
    val userId = sessionManager.getUserId()

    val history by viewModel.history.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedFilter by remember { mutableStateOf(context.getString(R.string.history_filter_all)) }
    var deleteId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(token, userId) {
        if (token.isNotBlank()) {
            viewModel.loadHistory(token, userId)
        }
    }

    val filterAll = stringResource(R.string.history_filter_all)
    val filteredHistory = remember(history, selectedFilter, filterAll) {
        if (selectedFilter == filterAll) history
        else history.filter { (it.halalStatus ?: "unknown").equals(selectedFilter, ignoreCase = true) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.history_title),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── STATS SUMMARY ──
            item {
                StatsHeader(
                    total = stats?.totalScans ?: history.size,
                    halal = stats?.halalCount ?: history.count { it.halalStatus.equals("halal", true) },
                    haram = history.count { it.halalStatus.equals("haram", true) },
                    today = stats?.todayScans ?: 0
                )
            }

            // ── FILTER CHIPS ──
            item {
                FilterRow(
                    selected = selectedFilter,
                    onSelect = { selectedFilter = it }
                )
            }

            // ── ERROR MESSAGE ──
            if (!errorMessage.isNullOrBlank()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            errorMessage ?: "",
                            color = HaramRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // ── LOADING / EMPTY / LIST ──
            if (loading && filteredHistory.isEmpty()) {
                items(5) {
                    com.example.halalyticscompose.ui.components.ShimmerProductItem()
                }
            } else if (filteredHistory.isEmpty()) {
                item {
                    EmptyHistoryView(onScan = { navController.navigate("scan") })
                }
            } else {
                items(filteredHistory) { item ->
                    HistoryCard(
                        item = item,
                        onClick = {
                            if (item.id > 0) {
                                navController.navigate("scan_history_detail/${item.id}")
                            } else {
                                Toast.makeText(context, "Detail riwayat tidak tersedia", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDelete = { deleteId = item.id }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }

    if (deleteId != null) {
        AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text(stringResource(R.string.history_delete_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.history_delete_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteHistory(token, deleteId!!)
                    deleteId = null
                }) { Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteId = null }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// STATS HEADER — Premium Emerald Gradient
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun StatsHeader(total: Int, halal: Int, haram: Int, today: Int) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.secondary
                        )
                    ),
                    RoundedCornerShape(18.dp)
                )
                .padding(18.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem(total.toString(), stringResource(R.string.history_total), Icons.Default.QrCodeScanner)
                StatItem(halal.toString(), stringResource(R.string.halal_status_halal), Icons.Default.CheckCircle)
                StatItem(haram.toString(), stringResource(R.string.halal_status_haram), Icons.Default.Cancel)
                StatItem(today.toString(), stringResource(R.string.history_today), null)
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, icon: ImageVector? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (icon != null) {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(2.dp))
        }
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// ═══════════════════════════════════════════════════════════════════
// FILTER ROW
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun FilterRow(selected: String, onSelect: (String) -> Unit) {
    val filters = listOf(
        stringResource(R.string.history_filter_all),
        stringResource(R.string.halal_status_halal),
        stringResource(R.string.halal_status_syubhat),
        stringResource(R.string.halal_status_haram)
    )
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = selected == filter
            val (bg, textColor) = when {
                isSelected && filter == stringResource(R.string.halal_status_halal) -> Pair(HalalGreen.copy(alpha = 0.15f), HalalGreen)
                isSelected && filter == stringResource(R.string.halal_status_haram) -> Pair(HaramRed.copy(alpha = 0.15f), HaramRed)
                isSelected && filter == stringResource(R.string.halal_status_syubhat) -> Pair(MushboohYellow.copy(alpha = 0.15f), MushboohYellow)
                isSelected -> Pair(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), MaterialTheme.colorScheme.primary)
                else -> Pair(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(bg)
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    filter,
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// HISTORY CARD — with status bar indicator
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun HistoryCard(item: ScanHistoryItem, onClick: () -> Unit, onDelete: () -> Unit) {
    val status = (item.halalStatus ?: "unknown").lowercase()
    val statusColor = when (status) {
        "halal" -> HalalGreen
        "haram" -> HaramRed
        else -> MushboohYellow
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Status indicator bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(72.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(statusColor)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    val finalImageUrl = ImageUtils.normalizeUrl(item.productImage)
                    if (finalImageUrl != null) {
                        AsyncImage(
                            model = finalImageUrl,
                            contentDescription = item.productName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("📦", fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.productName ?: stringResource(R.string.empty_products),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        item.createdAt.toRelativeTime(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(statusColor.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            status.uppercase(),
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}


