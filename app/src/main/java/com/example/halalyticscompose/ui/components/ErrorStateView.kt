@file:Suppress("DEPRECATION")

package com.example.halalyticscompose.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable error state view with icon, message, and retry button.
 * Supports different error types (no internet, server error, not found).
 */
@Composable
fun ErrorStateView(
    message: String = "Terjadi kesalahan",
    errorType: ErrorType = ErrorType.GENERAL,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector
    val iconColor: Color
    val title: String

    when (errorType) {
        ErrorType.NO_INTERNET -> {
            icon = Icons.Default.WifiOff
            iconColor = Color(0xFFE57373)
            title = "Tidak Ada Koneksi"
        }
        ErrorType.SERVER_ERROR -> {
            icon = Icons.Default.CloudOff
            iconColor = Color(0xFFFF7043)
            title = "Server Bermasalah"
        }
        ErrorType.NOT_FOUND -> {
            icon = Icons.Default.SearchOff
            iconColor = Color(0xFF78909C)
            title = "Tidak Ditemukan"
        }
        ErrorType.TIMEOUT -> {
            icon = Icons.Default.Timer
            iconColor = Color(0xFFFFB74D)
            title = "Koneksi Timeout"
        }
        ErrorType.UNAUTHORIZED -> {
            icon = Icons.Default.Lock
            iconColor = Color(0xFFE57373)
            title = "Sesi Berakhir"
        }
        ErrorType.GENERAL -> {
            icon = Icons.Default.ErrorOutline
            iconColor = Color(0xFFE57373)
            title = "Terjadi Kesalahan"
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated icon
        val infiniteTransition = rememberInfiniteTransition(label = "error_pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "icon_scale"
        )

        Box(
            modifier = Modifier
                .size((72 * scale).dp)
                .clip(RoundedCornerShape(20.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        if (onRetry != null) {
            Spacer(modifier = Modifier.height(24.dp))

            var isRetrying by remember { mutableStateOf(false) }

            Button(
                onClick = {
                    isRetrying = true
                    onRetry()
                },
                enabled = !isRetrying,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isRetrying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Coba Lagi")
            }

            // Auto-reset retry state
            LaunchedEffect(isRetrying) {
                if (isRetrying) {
                    kotlinx.coroutines.delay(2000)
                    isRetrying = false
                }
            }
        }
    }
}

enum class ErrorType {
    NO_INTERNET,
    SERVER_ERROR,
    NOT_FOUND,
    TIMEOUT,
    UNAUTHORIZED,
    GENERAL
}
