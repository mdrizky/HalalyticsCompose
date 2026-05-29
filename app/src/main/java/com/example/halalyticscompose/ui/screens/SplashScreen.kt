package com.example.halalyticscompose.ui.screens

import android.util.Log
import android.os.SystemClock
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.halalyticscompose.R
import com.example.halalyticscompose.ui.theme.SplashGreenGlow
import com.example.halalyticscompose.ui.theme.MintAccent
import com.example.halalyticscompose.utils.RoleHelper
import com.example.halalyticscompose.utils.SessionManager
import com.example.halalyticscompose.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SplashBlack = Color(0xFF000000)
private val SplashEmeraldDark = Color(0xFF064E3B)
private val SplashEmerald = Color(0xFF10B981)
private val SplashEmeraldLight = Color(0xFF34D399)
private val SplashTaglineGray = Color(0xFFB8C4BE)

private data class SplashCategoryChip(val icon: ImageVector, val label: String)

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
    val isDark = remember { sm.isDarkMode() }

    val bgColor1 = if (isDark) SplashEmeraldDark else SplashEmeraldLight
    val bgColor2 = if (isDark) SplashBlack else SplashEmerald
    val bgColor3 = if (isDark) SplashEmeraldDark else SplashEmeraldDark

    val textColorSecondary = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF4A5A53)
    val textColorTertiary = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF6C7C75)
    val progressTrackColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2EBE6)

    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.82f) }
    val ringAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val chipsAlpha = remember { Animatable(0f) }
    val bottomAlpha = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }
    val chipScales = remember { List(4) { Animatable(0.6f) } }

    var isNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Log.d("HALALYTICS_FLOW", "SplashScreen: Animation Started")
        val splashStart = SystemClock.elapsedRealtime()

        coroutineScope {
            launch { logoAlpha.animateTo(1f, tween(900)) }
            launch { logoScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) }
            launch { delay(350); ringAlpha.animateTo(0.45f, tween(700)) }
        }

        delay(1000)
        taglineAlpha.animateTo(1f, tween(550))
        delay(350)
        chipsAlpha.animateTo(1f, tween(400))
        chipScales.forEachIndexed { _, anim ->
            launch { anim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)) }
            delay(110)
        }

        delay(450)
        bottomAlpha.animateTo(1f, tween(450))
        launch { progress.animateTo(1f, tween(1200)) }

        val elapsed = SystemClock.elapsedRealtime() - splashStart
        val waitMore = (2500L - elapsed).coerceAtLeast(0L)
        delay(waitMore)

        if (isNavigated) return@LaunchedEffect
        isNavigated = true

        Log.d("HALALYTICS_FLOW", "SplashScreen: Checking Session")
        val token = sm.getAuthToken()
        val role = sm.getRole()

        if (token.isNullOrEmpty()) {
            Log.d("HALALYTICS_FLOW", "SplashScreen: No token found. Checking onboarding status.")
            delay(500) 
            if (!sm.hasCompletedOnboarding()) {
                Log.d("HALALYTICS_FLOW", "SplashScreen: Navigating to Onboarding (Start Destination: splash)")
                onNavigateToOnboarding()
            } else {
                Log.d("HALALYTICS_FLOW", "SplashScreen: Navigating to Login")
                onNavigateToLogin()
            }
        } else {
            Log.d("HALALYTICS_FLOW", "SplashScreen: Token found. Role = $role")
            
            when (role) {
                "admin" -> {
                    Log.d("HALALYTICS_FLOW", "SplashScreen: Navigating to Admin Dashboard (home)")
                    onNavigateToAdminDashboard()
                }
                "ahli_gizi" -> {
                    Log.d("HALALYTICS_FLOW", "SplashScreen: Navigating to Nutritionist Home")
                    onNavigateToNutritionistHome()
                }
                else -> {
                    Log.d("HALALYTICS_FLOW", "SplashScreen: Navigating to User Home")
                    onNavigateToUserHome()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgColor1, bgColor2, bgColor3))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                // Outer glowing ring
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .alpha(ringAlpha.value)
                        .border(2.dp, SplashGreenGlow, CircleShape)
                )
                
                Image(
                    painter = painterResource(id = R.drawable.logo_halalytics_official),
                    contentDescription = "Halalytics Official Logo",
                    modifier = Modifier
                        .size(220.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
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
