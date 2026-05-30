package com.example.halalyticscompose.ui.screens

import android.util.Log
import android.os.SystemClock
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.halalyticscompose.R
import com.example.halalyticscompose.utils.RoleHelper
import com.example.halalyticscompose.utils.SessionManager
import com.example.halalyticscompose.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ═══════════════════════════════════════════════════════════
// HALALYTICS PREMIUM SPLASH SCREEN v3.0
// Inspired by: Grab, Gojek, Tokopedia splash screens
// ═══════════════════════════════════════════════════════════

// Premium color palette for splash
private val SplashBgTop = Color(0xFFFFFFFF)
private val SplashBgMiddle = Color(0xFFF0FFF4)
private val SplashBgBottom = Color(0xFFE6F7ED)
private val SplashEmerald = Color(0xFF059669)
private val SplashEmeraldDark = Color(0xFF047857)
private val SplashEmeraldLight = Color(0xFF34D399)
private val SplashGoldAccent = Color(0xFFD4AF37)
private val SplashMint = Color(0xFF6EE7B7)
private val SplashSlate = Color(0xFF64748B)
private val SplashSlateLight = Color(0xFF94A3B8)

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToUserHome: () -> Unit,
    onNavigateToAdminDashboard: () -> Unit,
    onNavigateToNutritionistHome: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sm = remember { SessionManager.getInstance(context) }

    // ═══ ANIMATION STATES ═══
    // Phase 1: Logo entrance
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.3f) }

    // Phase 2: Glow ring
    val ringAlpha = remember { Animatable(0f) }
    val ringScale = remember { Animatable(0.8f) }

    // Phase 3: Rotating orbit ring
    val orbitRotation = remember { Animatable(0f) }

    // Phase 4: Text entrance
    val titleAlpha = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(30f) }
    val taglineAlpha = remember { Animatable(0f) }

    // Phase 5: Bottom section
    val bottomAlpha = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }

    // Phase 6: Floating particles
    val particleAlpha = remember { Animatable(0f) }

    var isNavigated by remember { mutableStateOf(false) }

    // ═══ MAIN ANIMATION SEQUENCE ═══
    LaunchedEffect(Unit) {
        Log.d("HALALYTICS_FLOW", "SplashScreen: Premium Animation Started")
        val splashStart = SystemClock.elapsedRealtime()

        // Phase 1: Logo appears with bounce (0ms - 600ms)
        coroutineScope {
            launch {
                logoAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
            }
            launch {
                logoScale.animateTo(1f, spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ))
            }
        }

        // Phase 2: Glow ring expands (600ms - 900ms)
        coroutineScope {
            launch { ringAlpha.animateTo(0.6f, tween(400)) }
            launch { ringScale.animateTo(1f, tween(500, easing = FastOutSlowInEasing)) }
            launch { particleAlpha.animateTo(0.7f, tween(600)) }
        }

        // Phase 3: Orbit starts rotating (continuous)
        launch {
            orbitRotation.animateTo(
                360f,
                infiniteRepeatable(
                    animation = tween(8000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }

        // Phase 4: Title + tagline slide in (900ms - 1400ms)
        delay(200)
        coroutineScope {
            launch { titleAlpha.animateTo(1f, tween(400)) }
            launch { titleOffset.animateTo(0f, tween(500, easing = FastOutSlowInEasing)) }
        }
        delay(200)
        taglineAlpha.animateTo(1f, tween(350))

        // Phase 5: Bottom + progress bar (1400ms - 2200ms)
        delay(200)
        coroutineScope {
            launch { bottomAlpha.animateTo(1f, tween(400)) }
            launch { progress.animateTo(1f, tween(1000, easing = FastOutSlowInEasing)) }
        }

        // Ensure minimum 2.5 seconds splash display
        val elapsed = SystemClock.elapsedRealtime() - splashStart
        val waitMore = (2500L - elapsed).coerceAtLeast(0L)
        delay(waitMore)

        if (isNavigated) return@LaunchedEffect
        isNavigated = true

        // ═══ SESSION CHECK & NAVIGATION ═══
        Log.d("HALALYTICS_FLOW", "SplashScreen: Checking Session")
        val token = sm.getAuthToken()
        val rawRole = sm.getRole()
        val role = RoleHelper.normalizeRole(rawRole)

        try {
            if (token.isNullOrEmpty()) {
                Log.d("HALALYTICS_FLOW", "SplashScreen: No token. Checking onboarding.")
                if (!sm.hasCompletedOnboarding()) {
                    Log.d("HALALYTICS_FLOW", "SplashScreen: → Onboarding")
                    onNavigateToOnboarding()
                } else {
                    Log.d("HALALYTICS_FLOW", "SplashScreen: → Login")
                    onNavigateToLogin()
                }
            } else {
                Log.d("HALALYTICS_FLOW", "SplashScreen: Token found. Role=$role")
                when (role) {
                    "admin" -> {
                        Log.d("HALALYTICS_FLOW", "SplashScreen: → Admin Dashboard")
                        onNavigateToAdminDashboard()
                    }
                    "ahli_gizi" -> {
                        Log.d("HALALYTICS_FLOW", "SplashScreen: → Nutritionist Home")
                        onNavigateToNutritionistHome()
                    }
                    else -> {
                        Log.d("HALALYTICS_FLOW", "SplashScreen: → User Home")
                        onNavigateToUserHome()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("HALALYTICS_FLOW", "SplashScreen: Navigation Error", e)
            onNavigateToLogin()
        }
    }

    // ═══════════════════════════════════════════════════════════
    // UI LAYOUT
    // ═══════════════════════════════════════════════════════════
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SplashBgTop, SplashBgMiddle, SplashBgBottom),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // ═══ FLOATING PARTICLES (Background decoration) ═══
        FloatingParticles(
            alpha = particleAlpha.value,
            modifier = Modifier.fillMaxSize()
        )

        // ═══ MAIN CONTENT ═══
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {

            Spacer(modifier = Modifier.height(80.dp))

            // ═══ LOGO SECTION WITH GLOW RING ═══
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(260.dp)
            ) {
                // Layer 1: Outer soft glow (blur effect)
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .alpha(ringAlpha.value * 0.3f)
                        .scale(ringScale.value * 1.1f)
                        .blur(20.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    SplashEmerald.copy(alpha = 0.4f),
                                    SplashMint.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Layer 2: Rotating orbit ring with dots
                Canvas(
                    modifier = Modifier
                        .size(230.dp)
                        .alpha(ringAlpha.value * 0.5f)
                        .rotate(orbitRotation.value)
                ) {
                    val radius = size.minDimension / 2f
                    val dotCount = 12
                    for (i in 0 until dotCount) {
                        val angle = (2 * PI * i / dotCount).toFloat()
                        val x = center.x + radius * cos(angle)
                        val y = center.y + radius * sin(angle)
                        val dotSize = if (i % 3 == 0) 4.dp.toPx() else 2.dp.toPx()
                        val dotAlpha = if (i % 3 == 0) 0.8f else 0.3f
                        drawCircle(
                            color = SplashEmerald.copy(alpha = dotAlpha),
                            radius = dotSize,
                            center = Offset(x, y)
                        )
                    }
                }

                // Layer 3: Inner glowing ring
                Canvas(
                    modifier = Modifier
                        .size(200.dp)
                        .alpha(ringAlpha.value * 0.7f)
                        .scale(ringScale.value)
                ) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                SplashEmerald.copy(alpha = 0.6f),
                                SplashMint.copy(alpha = 0.2f),
                                SplashEmeraldLight.copy(alpha = 0.4f),
                                SplashGoldAccent.copy(alpha = 0.3f),
                                SplashEmerald.copy(alpha = 0.6f)
                            )
                        ),
                        radius = size.minDimension / 2f,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Layer 4: White circle background for logo
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                        .shadow(
                            elevation = 24.dp,
                            shape = CircleShape,
                            spotColor = SplashEmerald.copy(alpha = 0.25f),
                            ambientColor = SplashEmerald.copy(alpha = 0.1f)
                        )
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    // Layer 5: The actual logo
                    Image(
                        painter = painterResource(id = R.drawable.logo_halalytics_official),
                        contentDescription = "Halalytics Logo",
                        modifier = Modifier
                            .size(145.dp)
                            .padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ═══ APP NAME ═══
            Text(
                text = "Halalytics",
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                color = SplashEmeraldDark,
                letterSpacing = (-1).sp,
                modifier = Modifier
                    .alpha(titleAlpha.value)
                    .offset(y = titleOffset.value.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ═══ TAGLINE ═══
            Text(
                text = "Verify · Safe · Trusted",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = SplashSlate,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(taglineAlpha.value)
            )

            Spacer(modifier = Modifier.weight(1f))

            // ═══ BOTTOM SECTION ═══
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(bottomAlpha.value)
                    .padding(bottom = 60.dp)
            ) {
                // Premium progress bar with gradient
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(SplashEmerald.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress.value)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        SplashEmerald,
                                        SplashEmeraldLight,
                                        SplashMint
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Your Halal Lifestyle Companion",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = SplashSlateLight,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Version text
                Text(
                    text = "v2.0",
                    fontSize = 11.sp,
                    color = SplashSlateLight.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// FLOATING PARTICLES — Ambient decoration
// ═══════════════════════════════════════════════════════════
@Composable
private fun FloatingParticles(
    alpha: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle1"
    )

    val offset2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle2"
    )

    Canvas(modifier = modifier.alpha(alpha)) {
        val w = size.width
        val h = size.height

        // Floating emerald circles
        drawCircle(
            color = SplashEmerald.copy(alpha = 0.06f),
            radius = 80.dp.toPx(),
            center = Offset(w * 0.15f, h * (0.2f + offset1 * 0.05f))
        )
        drawCircle(
            color = SplashMint.copy(alpha = 0.08f),
            radius = 60.dp.toPx(),
            center = Offset(w * 0.85f, h * (0.15f + offset2 * 0.06f))
        )
        drawCircle(
            color = SplashEmeraldLight.copy(alpha = 0.05f),
            radius = 100.dp.toPx(),
            center = Offset(w * 0.7f, h * (0.75f + offset1 * 0.04f))
        )
        drawCircle(
            color = SplashGoldAccent.copy(alpha = 0.04f),
            radius = 50.dp.toPx(),
            center = Offset(w * 0.25f, h * (0.8f + offset2 * 0.03f))
        )

        // Small floating dots
        val dotPositions = listOf(
            Offset(w * 0.1f, h * 0.3f), Offset(w * 0.9f, h * 0.25f),
            Offset(w * 0.3f, h * 0.65f), Offset(w * 0.8f, h * 0.7f),
            Offset(w * 0.5f, h * 0.1f), Offset(w * 0.2f, h * 0.5f),
            Offset(w * 0.7f, h * 0.45f), Offset(w * 0.4f, h * 0.85f)
        )
        dotPositions.forEachIndexed { i, pos ->
            val yShift = if (i % 2 == 0) offset1 * 8.dp.toPx() else offset2 * 6.dp.toPx()
            drawCircle(
                color = SplashEmerald.copy(alpha = 0.12f),
                radius = (2 + (i % 3)).dp.toPx(),
                center = Offset(pos.x, pos.y + yShift)
            )
        }
    }
}
