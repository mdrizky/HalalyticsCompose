package com.example.halalyticscompose.ui.screens.donor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.halalyticscompose.ui.viewmodel.DonorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorFormScreen(
    navController: NavController,
    viewModel: DonorViewModel,
    eventId: Int,
    token: String
) {
    var age by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var recentSurgery by remember { mutableStateOf(false) }
    var feelingSick by remember { mutableStateOf(false) }
    var pregnant by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pendaftaran & Screening", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Harap isi form screening kesehatan ini dengan jujur. Ketidakjujuran dapat membahayakan diri Anda dan penerima darah.",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Usia (Tahun)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Berat Badan (Kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Riwayat Kesehatan", fontWeight = FontWeight.Bold)

            ScreeningCheckbox("Pernah operasi dalam 6 bulan terakhir?", recentSurgery) { recentSurgery = it }
            ScreeningCheckbox("Sedang merasa tidak enak badan/demam hari ini?", feelingSick) { feelingSick = it }
            ScreeningCheckbox("Sedang hamil atau menyusui?", pregnant) { pregnant = it }

            if (errorMessage != null) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val ageInt = age.toIntOrNull() ?: 0
                    val weightDouble = weight.toDoubleOrNull() ?: 0.0

                    if (ageInt < 17 || ageInt > 65) {
                        errorMessage = "Usia harus antara 17 - 65 tahun."
                        return@Button
                    }
                    if (weightDouble < 45) {
                        errorMessage = "Berat badan minimal 45 kg."
                        return@Button
                    }

                    errorMessage = null
                    
                    val answers = mapOf(
                        "recent_surgery" to recentSurgery,
                        "feel_sick" to feelingSick,
                        "pregnant" to pregnant
                    )

                    val requestPayload = mapOf(
                        "event_id" to eventId,
                        "age" to ageInt,
                        "weight_kg" to weightDouble,
                        "screening_answers" to answers
                    )

                    viewModel.registerForEventWithPayload(token, requestPayload) {
                        navController.navigate("donor_history") {
                            popUpTo("donor_home")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C)),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading && age.isNotBlank() && weight.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Daftar Sekarang", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ScreeningCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}
