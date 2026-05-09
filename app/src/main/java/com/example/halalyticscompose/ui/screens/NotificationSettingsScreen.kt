package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class NotifCategory(
    val key: String,
    val icon: ImageVector,
    val title: String,
    val description: String,
    val default: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    navController: NavController,
    initialPrefs: Map<String, Boolean> = emptyMap(),
    onSave: (Map<String, Boolean>) -> Unit = {}
) {
    val categories = listOf(
        NotifCategory("medication_reminders", Icons.Default.Medication, "Pengingat Obat", "Notifikasi jadwal minum obat"),
        NotifCategory("promo_deals", Icons.Default.LocalOffer, "Promo & Diskon", "Info promo produk halal"),
        NotifCategory("weekly_report", Icons.Default.Assessment, "Laporan Mingguan AI", "Ringkasan kesehatan mingguan"),
        NotifCategory("favorite_updates", Icons.Default.FavoriteBorder, "Update Produk Favorit", "Perubahan status halal produk"),
        NotifCategory("new_products", Icons.Default.NewReleases, "Produk Baru", "Produk baru masuk database"),
        NotifCategory("watchlist_alerts", Icons.Default.Warning, "Peringatan Watchlist", "Bahan berbahaya di watchlist"),
        NotifCategory("security_alerts", Icons.Default.Security, "Keamanan Akun", "Login baru dan perubahan akun"),
    )

    val states = remember {
        mutableStateMapOf<String, Boolean>().apply {
            categories.forEach { cat ->
                this[cat.key] = initialPrefs[cat.key] ?: cat.default
            }
        }
    }
    var hasChanges by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Notifikasi") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (hasChanges) {
                        TextButton(onClick = {
                            onSave(states.toMap())
                            hasChanges = false
                        }) {
                            Text("Simpan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF004D40).copy(alpha = 0.05f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF004D40)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Kelola notifikasi sesuai kebutuhanmu. Notifikasi keamanan tidak bisa dimatikan.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            categories.forEach { cat ->
                item(key = cat.key) {
                    val isEnabled = states[cat.key] ?: true
                    val isSecurityAlert = cat.key == "security_alerts"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = null,
                                tint = if (isEnabled) Color(0xFF004D40) else Color.Gray,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    cat.title,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                )
                                Text(
                                    cat.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = {
                                    if (!isSecurityAlert) {
                                        states[cat.key] = it
                                        hasChanges = true
                                    }
                                },
                                enabled = !isSecurityAlert
                            )
                        }
                    }
                }
            }

            // Quick actions
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            categories.forEach { states[it.key] = true }
                            hasChanges = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Aktifkan Semua", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            categories.forEach {
                                if (it.key != "security_alerts") states[it.key] = false
                            }
                            hasChanges = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Matikan Semua", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
