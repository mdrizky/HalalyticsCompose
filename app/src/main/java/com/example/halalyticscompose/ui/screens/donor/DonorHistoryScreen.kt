package com.example.halalyticscompose.ui.screens.donor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.halalyticscompose.ui.viewmodel.DonorViewModel

import com.example.halalyticscompose.ui.components.UnderDevelopmentPlaceholder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorHistoryScreen(navController: NavController, viewModel: DonorViewModel, token: String) {
    val history by viewModel.donorHistory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Donation History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (history.isEmpty()) {
            UnderDevelopmentPlaceholder(
                title = "Riwayat Donor",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(contentPadding = padding) {
                items(history) { appointment ->
                    ListItem(
                        headlineContent = { Text(appointment.event?.title ?: "Unknown Event") },
                        supportingContent = { Text(appointment.status) },
                        trailingContent = { Text(appointment.appointmentDate) }
                    )
                }
            }
        }
    }
}
