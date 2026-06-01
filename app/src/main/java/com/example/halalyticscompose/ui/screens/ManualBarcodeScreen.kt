package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.halalyticscompose.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualBarcodeScreen(navController: NavController) {
    var barcode by remember { mutableStateOf("") }
    val isError = barcode.isNotEmpty() && barcode.length < 8

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Input Barcode Manual", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Slate50
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Emerald.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.QrCodeScanner, null, tint = Emerald, modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Masukkan Nomor Barcode",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            
            Text(
                "Ketikkan nomor yang tertera di bawah barcode produk",
                fontSize = 14.sp,
                color = Slate500,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = barcode,
                onValueChange = { if (it.all { char -> char.isDigit() }) barcode = it },
                label = { Text("Nomor Barcode (EAN-13/UPC)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                isError = isError,
                supportingText = {
                    if (isError) Text("Barcode biasanya terdiri dari 8-13 digit angka")
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald,
                    focusedLabelColor = Emerald
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { 
                    if (barcode.length >= 8) {
                        navController.navigate("product_detail/$barcode") 
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = barcode.length >= 8,
                colors = ButtonDefaults.buttonColors(containerColor = Emerald)
            ) {
                Text("Cek Kehalalan Produk", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
