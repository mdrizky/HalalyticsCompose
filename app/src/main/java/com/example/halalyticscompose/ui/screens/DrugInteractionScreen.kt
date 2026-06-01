package com.example.halalyticscompose.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.halalyticscompose.ui.theme.MushboohYellow
import com.example.halalyticscompose.ui.viewmodel.HealthAiViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.halalyticscompose.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugInteractionScreen(
    navController: NavController,
    viewModel: HealthAiViewModel = hiltViewModel()
) {
    val interactionResult by viewModel.interactionResult.collectAsState()
    val interactionSource by viewModel.interactionSource.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var drugA by remember { mutableStateOf("") }
    var drugB by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drug Interaction Checker", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1B6B5A)
                )
            )
        },
        containerColor = Color(0xFFF8FBFB)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 2. Ilustrasi Dua Botol Obat
            DrugIllustration()

            // Judul
            Text(
                text = "Periksa Interaksi Obat",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B6B5A),
                textAlign = TextAlign.Center
            )

            // Deskripsi
            Text(
                text = "Pastikan obat yang Anda konsumsi aman untuk diminum bersamaan menggunakan analisis AI.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. TextField Obat 1
            DrugTextField(
                value = drugA,
                onValueChange = { drugA = it },
                label = "Nama Obat Pertama"
            )

            // 3. TextField Obat 2
            DrugTextField(
                value = drugB,
                onValueChange = { drugB = it },
                label = "Nama Obat Kedua"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Button Cek Interaksi
            CheckInteractionButton(
                onClick = { viewModel.checkInteraction(drugAName = drugA, drugBName = drugB) },
                isLoading = isLoading,
                enabled = drugA.isNotBlank() && drugB.isNotBlank()
            )

            // 5. Error Message
            error?.let {
                Text(
                    text = it,
                    color = Color(0xFFD32F2F),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Tampilkan hasil jika ada
            AnimatedVisibility(
                visible = interactionResult != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                interactionResult?.let { result ->
                    InteractionResultCard(result, interactionSource)
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun DrugIllustration() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Botol 1 (filled/dark)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1B6B5A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Medication,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Icon(
            imageVector = Icons.Default.CompareArrows,
            contentDescription = null,
            tint = Color(0xFF1B6B5A),
            modifier = Modifier.size(28.dp)
        )

        // Botol 2 (outlined/light)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFD0EFE8)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Medication,
                contentDescription = null,
                tint = Color(0xFF1B6B5A),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun DrugTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.Gray
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF1B6B5A),
            focusedLabelColor = Color(0xFF1B6B5A),
            cursorColor = Color(0xFF1B6B5A)
        )
    )
}

@Composable
fun CheckInteractionButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1B6B5A)
        ),
        enabled = !isLoading && enabled
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Cek Interaksi Sekarang",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
fun InteractionResultCard(
    data: com.example.halalyticscompose.data.model.DrugInteractionData,
    source: String? = null
) {
    val severityColor = when (data.severity.lowercase()) {
        "contraindicated", "major" -> Color(0xFFD32F2F)
        "moderate" -> Color(0xFFF59E0B)
        else -> Color(0xFF1B6B5A)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, severityColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(severityColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (data.hasInteraction) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = severityColor
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (data.hasInteraction) "Interaksi Terdeteksi" else "Aman Dikonsumsi Bersama",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1B6B5A)
                    )
                    Text(
                        text = "Tingkat: ${data.severity.uppercase()}",
                        fontSize = 12.sp,
                        color = severityColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Analisis AI:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            Text(
                text = data.description,
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            data.recommendation?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Rekomendasi:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = it,
                    fontSize = 14.sp,
                    color = Color.Black,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (!source.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Sumber data: $source",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
