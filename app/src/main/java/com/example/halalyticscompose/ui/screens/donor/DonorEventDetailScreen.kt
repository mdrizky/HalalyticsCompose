package com.example.halalyticscompose.ui.screens.donor

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorEventDetailScreen(navController: NavController, eventId: Int) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Event ID: $eventId", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    // For demo purposes, mapping to a dummy location related to blood donation.
                    val mapUri = Uri.parse("geo:0,0?q=PMI+Blood+Bank")
                    val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(mapIntent)
                    } else {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=PMI+Blood+Bank"))
                        context.startActivity(browserIntent)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Buka Lokasi di Google Maps")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { navController.navigate("donor_form/$eventId") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register for this Event")
            }
        }
    }
}
