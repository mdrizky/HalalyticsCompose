package com.example.halalyticscompose.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ========== HALAL STATUS COLORS ==========
object HalalBadgePalette {
    val HalalGreen = Color(0xFF2E7D32)
    val HalalGreenBg = Color(0xFFE8F5E9)
    val HaramRed = Color(0xFFC62828)
    val HaramRedBg = Color(0xFFFFEBEE)
    val SyubhatOrange = Color(0xFFE65100)
    val SyubhatOrangeBg = Color(0xFFFFF3E0)
    val UnknownGray = Color(0xFF546E7A)
    val UnknownGrayBg = Color(0xFFECEFF1)
}

enum class HalalStatus(
    val label: String,
    val arabicLabel: String,
    val color: Color,
    val bgColor: Color,
    val icon: ImageVector
) {
    HALAL("HALAL", "حلال", HalalBadgePalette.HalalGreen, HalalBadgePalette.HalalGreenBg, Icons.Default.CheckCircle),
    HARAM("HARAM", "حرام", HalalBadgePalette.HaramRed, HalalBadgePalette.HaramRedBg, Icons.Default.Block),
    SYUBHAT("SYUBHAT", "شبهة", HalalBadgePalette.SyubhatOrange, HalalBadgePalette.SyubhatOrangeBg, Icons.Default.Warning),
    UNKNOWN("BELUM DIVERIFIKASI", "غير معروف", HalalBadgePalette.UnknownGray, HalalBadgePalette.UnknownGrayBg, Icons.AutoMirrored.Filled.HelpOutline);

    companion object {
        fun fromString(status: String?): HalalStatus {
            return when (status?.lowercase()?.trim()) {
                "halal", "verified_halal", "halal_certified" -> HALAL
                "haram", "non_halal" -> HARAM
                "syubhat", "mushbooh", "meragukan", "doubtful" -> SYUBHAT
                else -> UNKNOWN
            }
        }
    }
}

enum class BadgeSize(val fontSize: TextUnit, val iconSize: Dp, val paddingH: Dp, val paddingV: Dp) {
    SMALL(11.sp, 14.dp, 8.dp, 4.dp),
    MEDIUM(13.sp, 16.dp, 12.dp, 6.dp),
    LARGE(15.sp, 20.dp, 16.dp, 8.dp)
}

/**
 * Universal halal status badge with animated entrance.
 * Displays status as a pill badge with icon, text, and optional Arabic variant.
 */
@Composable
fun HalalStatusBadge(
    status: HalalStatus,
    size: BadgeSize = BadgeSize.MEDIUM,
    showArabic: Boolean = false,
    animate: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Bounce-in animation
    val scale = if (animate) {
        val animatable = remember { Animatable(0.3f) }
        LaunchedEffect(status) {
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        animatable.value
    } else {
        1f
    }

    Row(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .background(status.bgColor)
            .padding(horizontal = size.paddingH, vertical = size.paddingV),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = status.icon,
            contentDescription = status.label,
            tint = status.color,
            modifier = Modifier.size(size.iconSize)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = if (showArabic) "${status.label} ${status.arabicLabel}" else status.label,
            fontSize = size.fontSize,
            fontWeight = FontWeight.Bold,
            color = status.color,
            maxLines = 1
        )
    }
}

/**
 * Convenience function using string status.
 */
@Composable
fun HalalStatusBadge(
    statusString: String?,
    size: BadgeSize = BadgeSize.MEDIUM,
    showArabic: Boolean = false,
    modifier: Modifier = Modifier
) {
    HalalStatusBadge(
        status = HalalStatus.fromString(statusString),
        size = size,
        showArabic = showArabic,
        modifier = modifier
    )
}

/**
 * Confidence score indicator (0-100%) with colored circular arc.
 */
@Composable
fun HalalConfidenceIndicator(
    score: Int,
    modifier: Modifier = Modifier
) {
    val color = when {
        score >= 80 -> HalalBadgePalette.HalalGreen
        score >= 50 -> HalalBadgePalette.SyubhatOrange
        else -> HalalBadgePalette.HaramRed
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier.size(56.dp),
                color = color,
                trackColor = color.copy(0.15f),
                strokeWidth = 5.dp
            )
            Text(
                text = "$score",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Skor Halal",
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}
