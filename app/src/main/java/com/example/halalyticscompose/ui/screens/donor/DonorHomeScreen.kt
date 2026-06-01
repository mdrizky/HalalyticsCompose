package com.example.halalyticscompose.ui.screens.donor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.halalyticscompose.data.model.BloodStock
import com.example.halalyticscompose.ui.viewmodel.DonorViewModel

// Premium Blood Donor Red Palette
private val DonorRed = Color(0xFFE74C3C)
private val DonorRedDark = Color(0xFFC0392B)
private val DonorBackground = Color(0xFFFDF2F2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorHomeScreen(
    navController: NavController,
    viewModel: DonorViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    authViewModel: com.example.halalyticscompose.ui.viewmodel.AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    token: String
) {
    val bloodEvents by viewModel.bloodEvents.collectAsState()
    val bloodStock by viewModel.bloodStock.collectAsState()
    val donorCard by viewModel.donorCard.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userData by authViewModel.userData.collectAsState()
    
    val displayName = userData?.username ?: "Active Donor"

    LaunchedEffect(token) {
        if (token.isNotEmpty()) {
            viewModel.loadDonorDashboard(token)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blood Donor Central", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = DonorRed
                )
            )
        },
        containerColor = DonorBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Donor Card Section
            item {
                donorCard?.let { card ->
                    DonorCardWidget(
                        name = displayName,
                        bloodType = card.bloodType ?: "-",
                        donations = card.totalDonations ?: 0,
                        nextDate = card.nextEligibleDate ?: "Ready to Donate"
                    )
                } ?: run {
                    // Fallback or empty state while loading
                    DonorCardWidget(
                        name = displayName,
                        bloodType = "-",
                        donations = 0,
                        nextDate = if (isLoading) "Loading..." else "Ready to Donate"
                    )
                }
            }

            // 2. Voluntary Status Toggle
            item {
                VoluntaryStatusToggle(
                    isVoluntary = donorCard?.isVoluntaryDonor ?: false,
                    onToggle = { isActive ->
                        viewModel.toggleVoluntaryStatus(isActive, token)
                    }
                )
            }

            // 3. Quick Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        title = "Find Events",
                        icon = Icons.Default.Event,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("donor_events") }
                    )
                    ActionCard(
                        title = "My History",
                        icon = Icons.Default.History,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("donor_history") }
                    )
                }
            }

            // 3. Blood Stock Summary
            item {
                BloodStockSection(bloodStock)
            }

            // 4. Emergency Requests
            item {
                val emergencies by viewModel.activeEmergencies.collectAsState()
                val firstEmergency = emergencies.firstOrNull()
                if (firstEmergency != null) {
                    EmergencyCard(
                        hospital = firstEmergency.hospital,
                        bloodType = firstEmergency.bloodType,
                        reason = firstEmergency.reason ?: "Butuh Darah Cepat",
                        onDonate = { navController.navigate("emergency_request") }
                    )
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { navController.navigate("emergency_request") },
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            "Belum ada permintaan darurat",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 5. Featured Events
            item {
                Text("Upcoming Events", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            items(bloodEvents.take(3)) { event ->
                EventListItem(
                    title = event.title,
                    location = event.location,
                    date = event.eventDate,
                    image = event.image,
                    onClick = { navController.navigate("donor_event_detail/${event.id}") }
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DonorRed)
            }
        }
    }
}

@Composable
private fun DonorCardWidget(name: String, bloodType: String, donations: Int, nextDate: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DonorRed)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Pattern
            Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.1f),
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 40.dp, y = 40.dp)
            )

            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Donor ID Card", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(bloodType, color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("Total Donations", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Text("$donations Times", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Next Eligibility", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Text(nextDate, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun VoluntaryStatusToggle(isVoluntary: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isVoluntary) Color(0xFFE8F5E9) else Color(0xFFFEEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isVoluntary) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isVoluntary) Color(0xFF4CAF50) else DonorRed
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Saya Siap Donor Sukarela", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        if (isVoluntary) "Lokasi Anda akan terlihat saat keadaan darurat" else "Aktifkan agar bisa dihubungi saat darurat",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
            Switch(
                checked = isVoluntary,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF4CAF50)
                )
            )
        }
    }
}

@Composable
private fun ActionCard(title: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = DonorRed, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun BloodStockSection(stocks: List<BloodStock>) {
    Column {
        Text("Blood Stock Summary", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (stocks.isEmpty()) {
                items(listOf("A", "B", "AB", "O")) { type ->
                    StockItemCard(type, 0)
                }
            } else {
                items(stocks) { stock ->
                    StockItemCard(stock.bloodType, stock.units)
                }
            }
        }
    }
}

@Composable
private fun StockItemCard(type: String, units: Int) {
    Card(
        modifier = Modifier.width(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(type, fontWeight = FontWeight.Black, fontSize = 20.sp, color = DonorRed)
            Text("$units u", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (units / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = if (units < 20) Color.Red else Color.Green,
                trackColor = Color.LightGray
            )
        }
    }
}

@Composable
private fun EmergencyCard(hospital: String, bloodType: String, reason: String, onDonate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
        border = androidx.compose.foundation.BorderStroke(1.dp, DonorRed.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(DonorRed),
                contentAlignment = Alignment.Center
            ) {
                Text(bloodType, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Emergency: $hospital", fontWeight = FontWeight.Bold, color = DonorRed)
                Text(reason, fontSize = 12.sp, color = Color.DarkGray)
            }
            Button(
                onClick = onDonate,
                colors = ButtonDefaults.buttonColors(containerColor = DonorRed),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Donate", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun EventListItem(title: String, location: String, date: String, image: String?, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f))
            ) {
                AsyncImage(
                    model = image ?: "https://via.placeholder.com/150/ff0000/FFFFFF?text=Donor",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(location, fontSize = 12.sp, color = Color.Gray)
                }
                Text(date, fontSize = 12.sp, color = DonorRed, fontWeight = FontWeight.Medium)
            }
        }
    }
}
