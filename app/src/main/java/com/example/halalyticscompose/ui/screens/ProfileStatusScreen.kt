package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.halalyticscompose.ui.theme.MushboohYellow
import com.example.halalyticscompose.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileStatusScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel(),
    historyViewModel: com.example.halalyticscompose.ui.viewmodel.HistoryViewModel = hiltViewModel()
) {
    val userData by viewModel.userData.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val totalScans by historyViewModel.totalScans.collectAsState()
    val halalProducts by historyViewModel.halalProducts.collectAsState()
    val currentStreak by historyViewModel.currentStreak.collectAsState()
    val bmi = userData?.bmi?.toFloat()
    val profileChecks = remember(userData, currentUser) {
        listOf(
            "Nama lengkap" to !((userData?.fullName ?: currentUser).isNullOrBlank()),
            "Email" to !userData?.email.isNullOrBlank(),
            "No. telepon" to !userData?.phone.isNullOrBlank(),
            "Golongan darah" to !userData?.bloodType.isNullOrBlank(),
            "Alergi" to !userData?.allergy.isNullOrBlank(),
            "Riwayat medis" to !userData?.medicalHistory.isNullOrBlank(),
            "Tujuan kesehatan" to !userData?.goal.isNullOrBlank(),
            "Preferensi diet" to !userData?.dietPreference.isNullOrBlank(),
            "Level aktivitas" to !userData?.activityLevel.isNullOrBlank(),
            "Bahasa" to !userData?.language.isNullOrBlank()
        )
    }
    val completionPercent = remember(profileChecks) {
        if (profileChecks.isEmpty()) 0 else ((profileChecks.count { it.second }.toFloat() / profileChecks.size.toFloat()) * 100f).toInt()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Status Profil",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Kelengkapan Profil",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "$completionPercent%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = completionPercent / 100f,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        profileChecks.forEach { (label, completed) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
                
                // BMI Card
                bmi?.let { bmiValue ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Indeks Massa Tubuh (BMI)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = String.format("%.1f", bmiValue),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    bmiValue < 18.5 -> MaterialTheme.colorScheme.tertiary
                                    bmiValue < 25 -> MaterialTheme.colorScheme.primary
                                    bmiValue < 30 -> MushboohYellow
                                    else -> MaterialTheme.colorScheme.error
                                }
                            )
                            Text(
                                text = when {
                                    bmiValue < 17.0 -> "Sangat Kurus"
                                    bmiValue < 18.5 -> "Kurus"
                                    bmiValue < 20.0 -> "Normal (Tipis)"
                                    bmiValue < 23.0 -> "Normal (Ideal)"
                                    bmiValue < 25.0 -> "Berlebih"
                                    else -> "Obesitas"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                // Profile Information Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Informasi Profil",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Goal
                        userData?.goal?.let {
                            ProfileStatusItem(
                                icon = Icons.Default.Flag,
                                title = "Tujuan",
                                value = it
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        
                        // Diet Preference
                        userData?.dietPreference?.let {
                            ProfileStatusItem(
                                icon = Icons.Default.Restaurant,
                                title = "Preferensi Diet",
                                value = it
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        
                        // Activity Level
                        userData?.activityLevel?.let {
                            ProfileStatusItem(
                                icon = Icons.AutoMirrored.Filled.DirectionsRun,
                                title = "Level Aktivitas",
                                value = it
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        
                        // Address
                        userData?.address?.let {
                            ProfileStatusItem(
                                icon = Icons.Default.LocationOn,
                                title = "Alamat",
                                value = it
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        
                        // Language
                        userData?.language?.let {
                            ProfileStatusItem(
                                icon = Icons.Default.Language,
                                title = "Bahasa",
                                value = it
                            )
                        }
                    }
                }
            }

            item {
                // Stats Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Statistik Scan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                title = "Total Scan",
                                value = totalScans.toString(),
                                color = MaterialTheme.colorScheme.primary
                            )
                            StatItem(
                                title = "Halal",
                                value = halalProducts.toString(),
                                color = MaterialTheme.colorScheme.primary
                            )
                            StatItem(
                                title = "Syubhat",
                                value = (totalScans - halalProducts).coerceAtLeast(0).toString(),
                                color = MushboohYellow
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            StatItem(
                                title = "Hari Beruntun",
                                value = currentStreak.toString(),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { navController.navigate("edit_profile") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Lengkapi Profil")
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ProfileStatusItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
