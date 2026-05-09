package com.example.halalyticscompose.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.halalyticscompose.data.model.NotificationItem
import com.example.halalyticscompose.ui.viewmodel.NotificationViewModel
import com.example.halalyticscompose.utils.SessionManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.halalyticscompose.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val token = sessionManager.getAuthToken() ?: ""
    val userId = sessionManager.getUserId() // Get real user ID from session

    val notifications by viewModel.notifications.collectAsState()
    val adminOnlyNotifications = remember(notifications) {
        notifications.filter {
            it.type.lowercase() in setOf("admin", "announcement", "product", "verification", "request", "bpom", "system")
        }
    }
    val unreadCount by viewModel.unreadCount.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        if (token.isNotEmpty()) {
            viewModel.loadNotifications(token, userId) // Pass correct User ID
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(stringResource(R.string.notification_title), fontWeight = FontWeight.Bold)
                        if (unreadCount > 0) {
                            Text(
                                text = stringResource(R.string.notification_unread, unreadCount),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(
                            onClick = { viewModel.markAllAsRead() }
                        ) {
                            Text(stringResource(R.string.notification_mark_all))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.TopCenter),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        TextButton(onClick = { /* dismiss error */ }) {
                            Text("Tutup")
                        }
                    }
                }
            }

            when {
                loading && adminOnlyNotifications.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                adminOnlyNotifications.isEmpty() -> {
                    EmptyNotificationsState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(adminOnlyNotifications) { notification ->
                            NotificationCard(
                                notification = notification,
                                onClick = {
                                    if (!notification.isRead) {
                                        viewModel.markAsRead(notification.id)
                                    }
                                    navController.navigate(resolveNotificationRoute(notification))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun resolveNotificationRoute(notification: NotificationItem): String {
    val actionType = notification.actionType?.lowercase().orEmpty()
    val actionValue = notification.actionValue.orEmpty()
    val fallbackBarcode = notification.relatedProduct?.barcode.orEmpty()

    fun resolveProductRoute(raw: String): String {
        val value = raw.ifBlank { fallbackBarcode }
        if (value.isBlank()) return "manual_input"
        return when {
            value.startsWith("ext:", ignoreCase = true) ->
                "product_external_detail/${Uri.encode(value.substringAfter("ext:"))}"
            value.startsWith("local:", ignoreCase = true) ->
                "product_detail/${Uri.encode(value.substringAfter("local:"))}"
            value.all { it.isDigit() } && value.length >= 8 ->
                "product_external_detail/${Uri.encode(value)}"
            else -> "product_detail/${Uri.encode(value)}"
        }
    }

    return when (actionType) {
        "open_product", "view_product" -> resolveProductRoute(actionValue)
        "view_request", "open_request" -> {
            val label = Uri.encode("Request Verifikasi #${if (actionValue.isBlank()) notification.id else actionValue}")
            "report_issue/0/$label"
        }
        "open_bpom", "open_verification", "verification_result" -> "bpom_scanner"
        "open_health_suite" -> "health_suite_hub"
        "open_search" -> "manual_input"
        "open_news", "open_article" ->
            if (actionValue.isNotBlank()) "health_article_detail/${Uri.encode(actionValue)}" else "health_articles"
        else -> {
            when (notification.type.lowercase()) {
                "product" -> resolveProductRoute(actionValue)
                "verification", "bpom" -> "bpom_scanner"
                "request" -> "report_issue/0/${Uri.encode("Laporan Verifikasi")}"
                "announcement", "admin", "system" -> "health_articles"
                else -> "notifications"
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    val isRead = notification.isRead
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isRead) 1.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon Logic
            val icon = when(notification.type) {
                "scan" -> Icons.Default.QrCodeScanner
                "umkm" -> Icons.Default.Store
                "favorite" -> Icons.Default.Star
                else -> Icons.Default.Notifications
            }
            val iconColor = when(notification.type) {
                "scan" -> Color(0xFF4CAF50)
                "favorite" -> Color(0xFFFFC107)
                else -> MaterialTheme.colorScheme.primary
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = if (isRead) FontWeight.Normal else FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (!isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyNotificationsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.notification_empty),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
