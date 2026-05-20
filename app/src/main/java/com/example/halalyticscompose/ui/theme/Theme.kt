package com.example.halalyticscompose.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ═══════════════════════════════════════════════════════════════════
// HALALYTICS PREMIUM DESIGN SYSTEM v2.0
// Silicon Valley Health-Tech + Medical Futuristic
// Emerald Green + Deep Teal + Mint
// ═══════════════════════════════════════════════════════════════════

// ════════════════════════════════════════════════════════════════
// PRIMARY COLORS — EMERALD & TEAL (REDESIGNED)
// ════════════════════════════════════════════════════════════════

// Primary Emerald Green
val Emerald = Color(0xFF004D40)         // Primary brand color
val EmeraldDark = Color(0xFF00332B)     // Hover states, active
val EmeraldDarker = Color(0xFF00221A)   // Pressed states
val EmeraldLight = Color(0xFFB2DFDB)    // Light backgrounds
val EmeraldLighter = Color(0xFFE0F2F1)  // Lighter backgrounds

// Secondary Mint & Cyan
val Teal = Color(0xFF4DB6AC)            // Mint / Secondary
val TealDark = Color(0xFF00897B)        // Darker Mint
val TealLight = Color(0xFFB2DFDB)       // Light Mint backgrounds
val Mint = Color(0xFF80CBC4)            // Soft Cyan Accent

// Legacy palette mapping (for backward compatibility)
val Navy = Emerald                      // Maps to primary emerald
val NavyDark = EmeraldDark              // Maps to darker emerald
val NavyLight = Teal                    // Maps to teal
val MintAccent = Mint                   // Modern mint accent
val MintLight = EmeraldLight            // Soft sage backgrounds

// Gold — Premium Accent (untuk badge HALAL PREMIUM)
val GoldAccent = Color(0xFFD4AF37)     // Gold premium
val GoldLight = Color(0xFFFFF8E1)      // Gold pale

// Backgrounds
val BackgroundLight = Color(0xFFF8FAFC)     // Premium Startup Background
val SurfaceWhite = Color(0xFFFFFFFF)        // Pure White

// Borders
val BorderLight = Color(0xFFE2E8F0)         // Modern Slate Border
val BorderLighter = Color(0xFFF1F5F9)       // Very light border

// Text
val TextPrimary = Color(0xFF0F172A)          // Dark Text
val TextSecondary = Color(0xFF64748B)        // Gray Text
val TextTertiary = Color(0xFF94A3B8)         // Slate 400
val TextOnNavy = Color(0xFFFFFFFF)           // White on primary

// Semantic — Status Halal (tetap dipertahankan)
val HalalGreen = Color(0xFF2E7D32)          // Hijau profesional (bukan neon)
val HaramRed = Color(0xFFD32F2F)            // Merah medis
val MushboohYellow = Color(0xFFF57C00)      // Oranye tua (bukan kuning cerah)

// Functional
val SuccessGreen = Color(0xFF388E3C)
val WarningAmber = Color(0xFFF57C00)
val ErrorRed = Color(0xFFD32F2F)
val InfoBlue = Color(0xFF004D40)            // Mapped to emerald

// Shimmer / Skeleton Loading
val ShimmerBase = Color(0xFFE0E0E0)
val ShimmerHighlight = Color(0xFFF5F5F5)

// Legacy compatibility (mapping ke palet baru)
val LightBackground = BackgroundLight
val LightCard = SurfaceWhite
val DarkBackground = Color(0xFF121212)
val DarkCard = Color(0xFF1E1E1E)
val TextWhite = Color(0xFFF8FAFC)
val TextGray = TextSecondary
val TextMuted = TextTertiary
val TextDark = TextPrimary
val DarkBorder = Color(0xFF333333)
val DarkCardLight = Color(0xFF1A3330)       // Dark emerald card
val HalalGreenDark = Color(0xFF1B5E20)
val TextGrayDark = Color(0xFF6B7280)
val TextMutedDark = Color(0xFF4B5563)

// Mappings for HomeScreen compatibility
val TextMedium = TextSecondary
val TextLight = TextTertiary
val CardWhite = SurfaceWhite
val MintPale = MintLight
val BorderGray = BorderLight
val Gold = GoldAccent

// Legacy vars mapping
val HalalColor = HalalGreen
val HaramColor = HaramRed
val MushboohColor = MushboohYellow
val PrimaryGreen = Color(0xFF004D40)        // Mapped to emerald
val PrimaryColor = Navy
val SecondaryColor = MintAccent
val SuccessColor = SuccessGreen
val InfoColor = Navy
val WarningColor = WarningAmber
val ErrorColor = ErrorRed
val DangerColor = HaramRed

// Premium Emerald (updated for new palette)
val Emerald500 = Color(0xFF004D40)
val Emerald600 = Color(0xFF00695C)
val Emerald700 = Color(0xFF004D40)
val Emerald900 = Color(0xFF00332B)

// Premium Dark Surface
val BgDarkBase = DarkBackground
val BgDarkSurface = DarkCard
val BgDarkElevated = Color(0xFF1A3330)

// Glass Components
val GlassWhite = Color(0x1AFFFFFF)
val GlassBorder = Color(0x14FFFFFF)

// Splash Glow
val SplashGreenGlow = Color(0xFF00E676)

// ════════════════════════════════════════════════════════════════
// NEUTRAL COLORS — SLATE PALETTE
// ════════════════════════════════════════════════════════════════

val Slate900 = Color(0xFF0F172A)        // Darkest text
val Slate800 = Color(0xFF1E293B)        // Secondary text
val Slate700 = Color(0xFF334155)        // Tertiary text
val Slate600 = Color(0xFF475569)        // Borders, disabled
val Slate500 = Color(0xFF64748B)        // Muted text
val Slate400 = Color(0xFF94A3B8)        // Disabled states
val Slate300 = Color(0xFFCBD5E1)        // Light borders
val Slate200 = Color(0xFFE2E8F0)        // Dividers
val Slate100 = Color(0xFFF1F5F9)        // Light backgrounds
val Slate50 = Color(0xFFF8FAFC)         // Lightest backgrounds

// ════════════════════════════════════════════════════════════════
// SEMANTIC COLORS
// ════════════════════════════════════════════════════════════════

val Success = Emerald                   // Green for success
val Warning = Color(0xFFF59E0B)         // Amber for warnings
val Error = Color(0xFFEF4444)           // Red for errors
val Info = Color(0xFF3B82F6)            // Blue for info
val Neutral = Slate500                  // Gray for neutral

// ════════════════════════════════════════════════════════════════
// GLASSMORPHISM COLORS
// ════════════════════════════════════════════════════════════════

val GlassClear = Color(0xFFFFFFFF).copy(alpha = 0.8f)    // White 80%
val GlassDark = Color(0xFF0F172A).copy(alpha = 0.6f)     // Dark 60%
val GlassEmphasis = Emerald.copy(alpha = 0.1f)           // Emerald tint

// ════════════════════════════════════════════════════════════════
// DIMENSIONS & SPACING
// ════════════════════════════════════════════════════════════════



object HalalyticsDimensions {
    // Spacing scale (4dp base unit)
    val space_1 = 4.dp      // xxs
    val space_2 = 8.dp      // xs
    val space_3 = 12.dp     // sm
    val space_4 = 16.dp     // md
    val space_5 = 20.dp     // lg
    val space_6 = 24.dp     // lg
    val space_7 = 32.dp     // xl
    val space_8 = 40.dp     // 2xl
    val space_9 = 48.dp     // 2xl
    val space_10 = 64.dp    // 3xl
    
    // Padding
    val paddingXSmall = 8.dp
    val paddingSmall = 12.dp
    val paddingMedium = 16.dp
    val paddingLarge = 24.dp
    val paddingXLarge = 32.dp
    
    // Corner radius (glassmorphism ready)
    val radiusSmall = 4.dp
    val radiusMedium = 8.dp
    val radiusLarge = 12.dp
    val radiusXLarge = 16.dp
    val radius2XLarge = 20.dp
    val radius3XLarge = 24.dp
    
    // Button heights
    val buttonHeight = 48.dp
    val buttonHeightSmall = 40.dp
    val buttonHeightLarge = 56.dp
    
    // Icon sizes
    val iconXSmall = 16.dp
    val iconSmall = 20.dp
    val iconMedium = 24.dp
    val iconLarge = 32.dp
    val iconXLarge = 48.dp
    val icon2XLarge = 64.dp
    
    // Avatar sizes
    val avatarSmall = 32.dp
    val avatarMedium = 48.dp
    val avatarLarge = 80.dp
    val avatarXLarge = 120.dp
    
    // Component sizes
    val chipHeight = 32.dp
    val dividerThickness = 1.dp
    val bottomNavHeight = 64.dp
    val topAppBarHeight = 64.dp
    val floatingActionButtonSize = 56.dp
    
    // Screen dimensions
    val screenContentWidth = 380.dp     // 412 - 16 - 16 margin
    val screenMarginHorizontal = 16.dp
    
    // Grid columns
    val gridOneColumn = 380.dp
    val gridTwoColumns = 182.dp
    val gridThreeColumns = 123.dp
    val gridFourColumns = 92.dp
}

// ════════════════════════════════════════════════════════════════
// TYPOGRAPHY
// ════════════════════════════════════════════════════════════════

object HalalyticsTypography {
    // Display sizes
    val displayLarge = 48.sp    // H1
    val displayMedium = 40.sp   // H1 medium
    val displaySmall = 32.sp    // H2
    
    // Headline sizes
    val headlineLarge = 28.sp   // H2
    val headlineMedium = 24.sp  // H3
    val headlineSmall = 20.sp   // H4
    
    // Title sizes
    val titleLarge = 20.sp
    val titleMedium = 16.sp
    val titleSmall = 14.sp
    
    // Body sizes
    val bodyLarge = 16.sp
    val bodyMedium = 14.sp
    val bodySmall = 12.sp
    
    // Label sizes
    val labelLarge = 12.sp
    val labelMedium = 11.sp
    val labelSmall = 10.sp
}

// ════════════════════════════════════════════════════════════════
// SHADOWS & ELEVATION
// ════════════════════════════════════════════════════════════════

object HalalyticsShadows {
    val elevation0 = 0.dp      // Flat
    val elevation1 = 1.dp      // Subtle
    val elevation2 = 3.dp      // Slight lift
    val elevation3 = 6.dp      // Card hover
    val elevation4 = 8.dp      // Modal
    val elevation5 = 12.dp     // Floating action
}

// ════════════════════════════════════════════════════════════════
// ANIMATION DURATIONS
// ════════════════════════════════════════════════════════════════

object HalalyticsAnimationDurations {
    const val VERY_FAST = 100       // Ripple, quick actions
    const val FAST = 200            // Fade, basic transitions
    const val NORMAL = 300          // Page transitions
    const val SLOW = 500            // Entrance animations
    const val VERY_SLOW = 800       // Long sequences
}
