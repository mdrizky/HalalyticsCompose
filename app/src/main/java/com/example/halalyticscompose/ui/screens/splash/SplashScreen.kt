package com.example.halalyticscompose.ui.screens.splash

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.halalyticscompose.ui.theme.*
import kotlinx.coroutines.delay

/**
 * HALALYTICS SPLASH SCREEN - Premium & Cinematic
 * 
 * Animation sequence:
 * 1. Gradient background loads (instant)
 * 2. Logo scale entrance: 0 → 1.0 (600ms) + fade
 * 3. Text slides from bottom (300ms) with fade
 * 4. Logo pulse animation (continuous, 1.5s loop)
 * 5. Auto-navigate to next screen after 3 seconds
 */

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit = {}
) {
    var logoScale by remember { mutableStateOf(0f) }
    var textAlpha by remember { mutableStateOf(0f) }
    var showPulse by remember { mutableStateOf(false) }
    
    // Logo entrance animation (scale + fade)
    LaunchedEffect(key1 = true) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = EaseOutBack)
        ) { value, _ ->
            logoScale = value
        }
    }
    
    // Text entrance (delayed, slides from bottom)
    LaunchedEffect(key1 = true) {
        delay(300)
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = 300, easing = EaseInOut)
        ) { value, _ ->
            textAlpha = value
        }
    }
    
    // Start pulse animation after entrance complete
    LaunchedEffect(key1 = true) {
        delay(1000)
        showPulse = true
    }
    
    // Auto-navigate after delay
    LaunchedEffect(key1 = true) {
        delay(3000)
        onNavigateToOnboarding()
    }
    
    // Main content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Emerald, Teal),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HalalyticsDimensions.paddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pulse ring animation (background)
            if (showPulse) {
                PulseRings()
            }
            
            // Logo icon with entrance animation
            Icon(
                imageVector = Icons.Filled.HealthAndSafety,
                contentDescription = "HALALYTICS Logo",
                tint = Color.White,
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale)
                    .alpha(logoScale)
            )
            
            Spacer(modifier = Modifier.height(HalalyticsDimensions.space_6))
            
            // App name with animation
            Column(
                modifier = Modifier.alpha(textAlpha),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HALALYTICS",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.animateContentSize()
                )
                
                Spacer(modifier = Modifier.height(HalalyticsDimensions.space_2))
                
                Text(
                    text = "Health Intelligence, Powered by AI",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.animateContentSize()
                )
            }
        }
    }
}

/**
 * Pulse rings animation - appears behind logo
 */
@Composable
private fun PulseRings() {
    val infiniteTransition = rememberInfiniteTransition()
    
    // Ring 1
    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    // Ring 2 (delayed)
    val ring2Scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    // Ring 1
    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(ring1Scale)
            .alpha(ring1Alpha)
            .border(
                width = 2.dp,
                color = Color.White,
                shape = RoundedCornerShape(100.dp)
            )
    )
    
    // Ring 2
    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(ring2Scale)
            .alpha(ring2Alpha)
            .border(
                width = 2.dp,
                color = Color.White.copy(alpha = 0.7f),
                shape = RoundedCornerShape(100.dp)
            )
    )
}

// For use in compose previews
@androidx.compose.ui.tooling.preview.Preview
@Composable
fun PreviewSplashScreen() {
    SplashScreen()
}
