package com.example.halalyticscompose.ui.screens.donation

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.halalyticscompose.data.model.DonationCampaignDto
import com.example.halalyticscompose.ui.viewmodel.DonationViewModel
import java.text.NumberFormat
import java.util.Locale

private fun formatIdr(value: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(value)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(
    navController: NavController,
    viewModel: DonationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadCampaigns() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Donasi Halalytics") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("donation_history") }) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.loading && state.campaigns.isEmpty() -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                state.error != null && state.campaigns.isEmpty() -> {
                    Column(Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadCampaigns() }) { Text("Coba Lagi") }
                    }
                }
                state.campaigns.isEmpty() -> {
                    Text("Belum ada campaign aktif.", Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.campaigns) { c ->
                            CampaignCard(c) {
                                viewModel.selectCampaign(c)
                                navController.navigate("donation_detail/${c.id}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CampaignCard(c: DonationCampaignDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            if (c.isUrgent) {
                Text("URGENT", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
            Text(c.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            c.description?.let { Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 2) }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { (c.progressPercent / 100f).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))
            Text("${formatIdr(c.collectedAmount)} / ${formatIdr(c.targetAmount)}", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationDetailScreen(
    campaignId: Long,
    navController: NavController,
    viewModel: DonationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val campaign = state.campaigns.find { it.id == campaignId } ?: state.selected

    LaunchedEffect(Unit) {
        if (state.campaigns.isEmpty()) viewModel.loadCampaigns()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(campaign?.title ?: "Detail Donasi") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            campaign?.let { c ->
                Text(c.description ?: "", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(progress = { (c.progressPercent / 100f).coerceIn(0f, 1f) })
                Text("${c.donorCount} donatur · ${formatIdr(c.collectedAmount)} terkumpul", Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { navController.navigate("donation_form/${c.id}") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Donasi Sekarang")
                }
            } ?: CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationFormScreen(
    campaignId: Long,
    navController: NavController,
    viewModel: DonationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var amountText by remember { mutableStateOf("50000") }
    var anonymous by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val presets = listOf(25000, 50000, 100000, 250000)

    LaunchedEffect(campaignId) {
        if (state.campaigns.isEmpty()) viewModel.loadCampaigns()
        state.campaigns.find { it.id == campaignId }?.let { viewModel.selectCampaign(it) }
    }

    LaunchedEffect(state.paymentUrl, state.snapToken) {
        val url = state.paymentUrl
        if (!url.isNullOrBlank()) {
            navController.navigate("donation_payment") {
                popUpTo("donation_form/$campaignId")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Form Donasi") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Pilih nominal", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.forEach { p ->
                    FilterChip(
                        selected = amountText == p.toString(),
                        onClick = { amountText = p.toString() },
                        label = { Text(formatIdr(p.toDouble())) }
                    )
                }
            }
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Nominal (Rp)") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = anonymous, onCheckedChange = { anonymous = it })
                Text("Sembunyikan nama saya")
            }
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Pesan (opsional)") },
                modifier = Modifier.fillMaxWidth()
            )
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount >= 1000) {
                        viewModel.createDonation(amount, anonymous, message.ifBlank { null })
                    }
                },
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.loading) CircularProgressIndicator(Modifier.size(22.dp))
                else Text("Lanjutkan ke Pembayaran")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationPaymentScreen(
    navController: NavController,
    viewModel: DonationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val url = state.paymentUrl

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pembayaran Midtrans") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearPayment()
                        navController.navigate("donation_success") { popUpTo("donations") }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (url.isNullOrBlank()) {
            Column(Modifier.fillMaxSize().padding(padding), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Mode sandbox: pembayaran simulasi berhasil.")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { navController.navigate("donation_success") { popUpTo("donations") } }) {
                    Text("Selesai")
                }
            }
        } else {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                if (url != null && (
                                    url.contains("transaction_status=settlement") ||
                                    url.contains("transaction_status=pending") ||
                                    url.contains("pembayaran-sukses") ||
                                    url.contains("/success") ||
                                    url.contains("/pending") ||
                                    url.contains("status_code=200") ||
                                    url.contains("status_code=201")
                                )) {
                                    viewModel.clearPayment()
                                    navController.navigate("donation_success") {
                                        popUpTo("donations") { inclusive = false }
                                    }
                                    return true
                                }
                                return false
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                if (url != null && (
                                    url.contains("transaction_status=settlement") ||
                                    url.contains("transaction_status=pending") ||
                                    url.contains("pembayaran-sukses") ||
                                    url.contains("/success") ||
                                    url.contains("/pending") ||
                                    url.contains("status_code=200") ||
                                    url.contains("status_code=201")
                                )) {
                                    viewModel.clearPayment()
                                    navController.navigate("donation_success") {
                                        popUpTo("donations") { inclusive = false }
                                    }
                                }
                            }
                        }
                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize().padding(padding)
            )
        }
    }
}

@Composable
fun DonationSuccessScreen(navController: NavController) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
        Spacer(Modifier.height(16.dp))
        Text("Terima kasih atas donasi Anda!", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))
        Button(onClick = { navController.navigate("donations") { popUpTo("home") } }) { Text("Kembali ke Donasi") }
        TextButton(onClick = { navController.navigate("home") { popUpTo(0) } }) { Text("Ke Beranda") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationHistoryScreen(
    navController: NavController,
    viewModel: DonationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadHistory() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Donasi") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (state.history.isEmpty()) {
            Text("Belum ada riwayat donasi.", Modifier.padding(padding).padding(24.dp))
        } else {
            LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.history) { item ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(item.campaign?.title ?: "Campaign", fontWeight = FontWeight.Bold)
                            Text(formatIdr(item.amount))
                            Text("Status: ${item.paymentStatus}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
