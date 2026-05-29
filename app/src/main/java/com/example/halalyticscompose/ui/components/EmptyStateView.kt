package com.example.halalyticscompose.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.halalyticscompose.ui.theme.*

import androidx.compose.ui.res.painterResource
import com.example.halalyticscompose.R

/**
 * HALALYTICS PREMIUM EMPTY STATE VIEW
 *
 * Features:
 * - Subtle breathing animation on the icon
 * - Gradient icon background circle
 * - Premium typography with emerald accent CTA
 * - Clean, modern, spacious layout
 */
@Composable
fun EmptyStateView(
    title: String = "No Data Yet",
    description: String = "Data will appear here once you start using the app.",
    icon: ImageVector = Icons.Default.Inbox,
    useOfficialLogo: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Subtle breathing animation for the icon
    val infiniteTransition = rememberInfiniteTransition(label = "empty_state_pulse")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_breath"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Gradient circle background with icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(iconScale)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            EmeraldLight.copy(alpha = 0.25f),
                            TealLight.copy(alpha = 0.08f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (useOfficialLogo) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.logo_halalytics_official),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp)
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Emerald.copy(alpha = 0.6f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.3).sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            fontSize = 14.sp,
            color = Slate500,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.widthIn(max = 280.dp)
        )

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onAction,
                shape = RoundedCornerShape(HalalyticsDimensions.radiusLarge),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald
                ),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = HalalyticsShadows.elevation2
                )
            ) {
                Text(
                    actionLabel,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

// ========== Convenience Composables ==========

@Composable
fun EmptyProductsView(onScan: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    EmptyStateView(
        title = "No Products Yet",
        description = "Scan a barcode or search for products to see them here.",
        icon = Icons.Default.QrCodeScanner,
        actionLabel = "Start Scanning",
        onAction = onScan,
        modifier = modifier
    )
}

@Composable
fun EmptyHistoryView(onScan: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    EmptyStateView(
        title = "No History Yet",
        description = "Your product scan history will appear here.",
        icon = Icons.Default.History,
        actionLabel = "Start Scanning",
        onAction = onScan,
        modifier = modifier
    )
}

@Composable
fun EmptyFavoritesView(onBrowse: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    EmptyStateView(
        title = "No Favorites Yet",
        description = "Products you love will be saved here for quick access.",
        icon = Icons.Default.FavoriteBorder,
        actionLabel = "Browse Products",
        onAction = onBrowse,
        modifier = modifier
    )
}

@Composable
fun EmptySearchView(modifier: Modifier = Modifier) {
    EmptyStateView(
        title = "No Results Found",
        description = "Try a different keyword or check your spelling.",
        icon = Icons.Default.SearchOff,
        modifier = modifier
    )
}

@Composable
fun EmptyNotificationsView(modifier: Modifier = Modifier) {
    EmptyStateView(
        title = "All Caught Up!",
        description = "You have no new notifications. Check back later.",
        icon = Icons.Default.NotificationsNone,
        modifier = modifier
    )
}
