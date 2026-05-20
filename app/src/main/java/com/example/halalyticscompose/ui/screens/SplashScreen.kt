package com.example.halalyticscompose.ui.screens

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.halalyticscompose.R
import com.example.halalyticscompose.ui.theme.MintAccent
import com.example.halalyticscompose.ui.theme.Navy
import com.example.halalyticscompose.ui.theme.NavyDark
import com.example.halalyticscompose.ui.theme.SplashGreenGlow
import com.example.halalyticscompose.utils.RoleHelper
import com.example.halalyticscompose.utils.SessionManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SplashBlack = Color(0xFF000000)
private val SplashEmeraldDark = Color(0xFF064E3B)
private val SplashEmerald = Color(0xFF10B981)
private val SplashEmeraldLight = Color(0xFF34D399)
private val SplashTaglineGray = Color(0xFFB8C4BE)

private data class SplashCategoryChip(val icon: ImageVector, val label: String)

/**
 * Splash 2,5–3 detik → cek sesi & role → login / home / nutritionist_home.
 * Logo: [R.drawable.logo_halalytics].
 */
@Composable
fun SplashScreen(
    navController: NavController,
    isLoggedIn: Boolean,
    onSplashComplete: () -> Unit
) {
    val context = LocalContext.current
    val sm = remember { SessionManager.getInstance(context) }
    val isDark = remember { sm.isDarkMode() }

    val bgColor1 = if (isDark) SplashEmeraldDark else SplashEmeraldLight
    val bgColor2 = if (isDark) SplashBlack else SplashEmerald
    val bgColor3 = if (isDark) SplashEmeraldDark else SplashEmeraldDark

    val textColorPrimary = if (isDark) Color.White else Color(0xFF1E2824)
    val textColorSecondary = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF4A5A53)
    val textColorTertiary = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF6C7C75)
    val taglineColor = if (isDark) SplashTaglineGray else Color(0xFF53635C)
    val progressTrackColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2EBE6)

    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.82f) }
    val ringAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val chipsAlpha = remember { Animatable(0f) }
    val bottomAlpha = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }
    val chipScales = remember { List(4) { Animatable(0.6f) } }

    val infinite = rememberInfiniteTransition(label = "splash_pulse")
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    LaunchedEffect(Unit) {
        val splashStart = SystemClock.elapsedRealtime()
        val targetMs = 2_500L
        val maxMs = 3_000L

        coroutineScope {
            launch {
                logoAlpha.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
            }
            launch {
                logoScale.animateTo(
                    1f,
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
            launch {
                delay(350)
                ringAlpha.animateTo(0.45f, tween(700))
            }
        }

        delay(1_000)
        taglineAlpha.animateTo(1f, tween(550, easing = FastOutSlowInEasing))
        delay(350)
        chipsAlpha.animateTo(1f, tween(400))
        chipScales.forEachIndexed { _, anim ->
            launch {
                anim.animateTo(
                    1f,
                    spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
            delay(110)
        }

        delay(450)
        bottomAlpha.animateTo(1f, tween(450))
        launch {
            progress.animateTo(1f, tween((targetMs - 1_800L).toInt().coerceAtLeast(1200), easing = FastOutSlowInEasing))
        }

        val elapsed = SystemClock.elapsedRealtime() - splashStart
        val waitMore = (targetMs - elapsed).coerceAtLeast(0L)
        delay(waitMore)

        val total = SystemClock.elapsedRealtime() - splashStart
        if (total < maxMs) {
            delay((maxMs - total).coerceAtMost(800L))
        }

        onSplashComplete()

        val smObj = SessionManager.getInstance(context)
        val loggedIn = isLoggedIn && smObj.isLoggedIn() && !smObj.getAuthToken().isNullOrBlank()
        val hasNetwork = context.isNetworkAvailable()

        var dest = when {
            !loggedIn && !smObj.hasCompletedOnboarding() -> "onboarding"
            !loggedIn -> "login"
            else -> RoleHelper.homeRoute(smObj.getRole()) ?: ""
        }

        if (dest.isBlank() || dest == "splash") {
            dest = "login"
        }

        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (currentRoute != dest) {
            navController.navigate(dest) {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(bgColor1, bgColor2, bgColor3)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .alpha(ringAlpha.value)
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0.55f),
                                Color.White.copy(alpha = 0.15f)
                            )
                        ),
                            shape = CircleShape
                        )
                )
                Image(
                    painter = painterResource(id = R.drawable.logo_halalytics),
                    contentDescription = "Halalytics",
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    modifier = Modifier
                        .size(220.dp)
                        .scale(logoScale.value * pulse)
                        .alpha(logoAlpha.value)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "VERIFY · AMAN · TERPERCAYA",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp,
                letterSpacing = 2.2.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.alpha(taglineAlpha.value),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Makanan · Obat · Kosmetik · Perawatan",
                color = MintAccent.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.alpha(taglineAlpha.value),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(chipsAlpha.value)
            ) {
                val chips = listOf(
                    SplashCategoryChip(Icons.Filled.Restaurant, "Makanan"),
                    SplashCategoryChip(Icons.Filled.Medication, "Obat"),
                    SplashCategoryChip(Icons.Filled.Spa, "Kosmetik"),
                    SplashCategoryChip(Icons.Filled.HealthAndSafety, "Perawatan")
                )
                chips.forEachIndexed { index, chip ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .scale(chipScales[index].value)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.92f))
                                .border(1.dp, MintAccent.copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = chip.icon,
                                contentDescription = chip.label,
                                tint = SplashGreenGlow,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = chip.label,
                            fontSize = 10.sp,
                            color = textColorSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(bottomAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Menyiapkan pengalaman Halalytics…",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColorTertiary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier
                        .width(220.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MintAccent,
                    trackColor = progressTrackColor
                )
            }
        }
    }
}

private fun android.content.Context.isNetworkAvailable(): Boolean {
    val cm = getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return true
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
