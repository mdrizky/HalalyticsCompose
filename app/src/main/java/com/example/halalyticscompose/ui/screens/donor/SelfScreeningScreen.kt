package com.example.halalyticscompose.ui.screens.donor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.halalyticscompose.ui.viewmodel.DonorViewModel

import com.example.halalyticscompose.ui.components.UnderDevelopmentPlaceholder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfScreeningScreen(navController: NavController, viewModel: DonorViewModel, token: String, eventId: Int) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Self Screening", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        UnderDevelopmentPlaceholder(
            title = "Self Screening Donor",
            modifier = Modifier.padding(padding)
        )
    }
}
