package com.example.halalyticscompose.ui.screens

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.halalyticscompose.R
import com.example.halalyticscompose.ui.viewmodel.AuthState
import com.example.halalyticscompose.ui.viewmodel.SplashViewModel
import kotlinx.coroutines.async

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.8f) }

    val authState by viewModel.authState.collectAsStateWithLifecycle()

    // Animation effect — runs once on first composition
    LaunchedEffect(Unit) {
        Log.i("HALALYTICS_FLOW", "SplashScreen: Animation started")
        val alphaJob = async { alpha.animateTo(1f, tween(1000)) }
        val scaleJob = async { scale.animateTo(1f, tween(1000)) }
        try { alphaJob.await() } catch (_: Exception) {}
        try { scaleJob.await() } catch (_: Exception) {}
    }

    // Navigation effect — reacts to authState changes
    // Key = authState means this block re-runs EVERY TIME authState changes value.
    // When authState is Loading, we do nothing (wait).
    // When it becomes Authenticated or Unauthenticated, we navigate.
    LaunchedEffect(authState) {
        Log.d("HALALYTICS_FLOW", "SplashScreen authState changed: $authState")
        when (val state = authState) {
            is AuthState.Authenticated -> {
                Log.i("HALALYTICS_FLOW", "Navigating to Home: role=${state.role}")
                val route = when (state.role) {
                    "admin" -> "admin_panel_app"
                    "ahli_gizi" -> "nutritionist_home"
                    else -> "home"
                }
                navController.navigate(route) {
                    popUpTo("splash") { inclusive = true }
                    launchSingleTop = true
                }
            }
            is AuthState.Unauthenticated -> {
                val route = if (state.isExpired) "login?expired=1" else "login"
                Log.i("HALALYTICS_FLOW", "Navigating to Login: route=$route")
                navController.navigate(route) {
                    popUpTo("splash") { inclusive = true }
                    launchSingleTop = true
                }
            }
            AuthState.Loading -> {
                // Do nothing — wait for ViewModel to finish checkSession()
            }
        }
    }

    // Professional Splash Screen UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
        ) {
            // App Logo - Styled Circularly
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .padding(bottom = 32.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, Color(0xFFFFD700), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_halalytics_new),
                    contentDescription = "Halalytics Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // App Name
            Text(
                text = "Halalytics",
                color = Color(0xFF2E7D32),
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            // Tagline
            Text(
                text = "Smart Choice for Halal Lifestyle",
                color = Color(0xFF2E7D32).copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Bottom Credit/Version
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 48.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "v1.0.0 • Verified Safe Trusted",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
