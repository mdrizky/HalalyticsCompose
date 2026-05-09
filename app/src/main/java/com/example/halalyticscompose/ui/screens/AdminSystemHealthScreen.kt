package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSystemHealthScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Health", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("API Status Monitoring", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                HealthStatusCard("Main Backend API", "ONLINE", Color(0xFF10B981))
                Spacer(modifier = Modifier.height(8.dp))
                HealthStatusCard("Ollama Local AI", "READY", Color(0xFF10B981))
                Spacer(modifier = Modifier.height(8.dp))
                HealthStatusCard("Database Connection", "CONNECTED", Color(0xFF10B981))
            }

            item {
                Text("Recent System Logs", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            items(getMockLogs()) { log ->
                LogItem(log)
            }
        }
    }
}

@Composable
fun HealthStatusCard(name: String, status: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
            Surface(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    status,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LogItem(log: SystemLog) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .offset(y = 6.dp)
                .background(if (log.isError) Color.Red else Color.Gray, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(log.time, fontSize = 11.sp, color = Color.Gray)
            Text(log.message, fontSize = 14.sp)
        }
    }
}

data class SystemLog(val time: String, val message: String, val isError: Boolean = false)

fun getMockLogs() = listOf(
    SystemLog("10:45:22", "User 'admin' logged in successfully."),
    SystemLog("10:42:10", "Failed to scan barcode: Network Timeout", true),
    SystemLog("10:30:05", "Product 'Indomie Goreng' approved by admin."),
    SystemLog("10:15:00", "System backup completed."),
    SystemLog("09:55:12", "New user registered: daffa_rizky"),
    SystemLog("09:40:00", "API health check: All services normal.")
)
