package com.example.halalyticscompose.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.halalyticscompose.ui.theme.*
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.border
import androidx.navigation.NavController
import androidx.compose.ui.draw.shadow

@Composable
fun SplashScreen(
    navController: NavController,
    isLoggedIn: Boolean,
    onSplashComplete: () -> Unit
) {
    val color = MaterialTheme.colorScheme
    
    // Animation Phase State
    var currentPhase by remember { mutableStateOf(0) } // 0: Scanning, 1: Result, 2: Final Logo
    
    // Phase 0: Scanning Animations
    val scanBarOffset = remember { Animatable(-100f) }
    val productScale = remember { Animatable(0.8f) }
    
    // Phase 1: Result Animations
    val resultScale = remember { Animatable(0f) }
    val resultAlpha = remember { Animatable(0f) }
    
    // Phase 2: Final Logo Animations
    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // --- PHASE 0: SCANNING ---
        productScale.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
        
        repeat(2) {
            scanBarOffset.animateTo(100f, tween(800, easing = LinearEasing))
            scanBarOffset.snapTo(-100f)
        }
        
        delay(300)
        currentPhase = 1
        
        // --- PHASE 1: RESULT ---
        resultAlpha.animateTo(1f, tween(400))
        resultScale.animateTo(1.1f, spring(Spring.DampingRatioMediumBouncy))
        resultScale.animateTo(1f, tween(200))
        
        delay(1200)
        currentPhase = 2
        
        // --- PHASE 2: FINAL LOGO ---
        logoAlpha.animateTo(1f, tween(600))
        logoScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy))
        
        delay(300)
        textAlpha.animateTo(1f, tween(800))
        
        delay(1800)
        onSplashComplete()
        
        if (isLoggedIn) {
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Navy, NavyDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        when (currentPhase) {
            0 -> { // SCANNING PHASE
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(2.dp, MintAccent.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                            .scale(productScale.value),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            tint = Color.White.copy(alpha = 0.2f)
                        )
                        
                        // Product Placeholder
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("PRODUCT", color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                        }

                        // Scanning Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .offset(y = scanBarOffset.value.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, MintAccent, Color.Transparent)
                                    )
                                )
                                .shadow(8.dp, spotColor = MintAccent)
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        "ANALYZING PRODUCT...",
                        color = MintAccent,
                        style = MaterialTheme.typography.labelLarge,
                        letterSpacing = 3.sp
                    )
                }
            }
            1 -> { // RESULT PHASE
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .scale(resultScale.value)
                            .alpha(resultAlpha.value)
                            .clip(CircleShape)
                            .background(MintAccent.copy(alpha = 0.15f))
                            .border(4.dp, MintAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MintAccent
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "HALAL VERIFIED",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.alpha(resultAlpha.value),
                        letterSpacing = 1.sp
                    )
                    Text(
                        "Safe & Healthy for consumption",
                        color = MintAccent,
                        fontSize = 14.sp,
                        modifier = Modifier.alpha(resultAlpha.value)
                    )
                }
            }
            2 -> { // FINAL LOGO PHASE
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = com.example.halalyticscompose.R.drawable.logo_halalytics),
                        contentDescription = "Halalytics Logo",
                        modifier = Modifier
                            .size(180.dp)
                            .scale(logoScale.value)
                            .alpha(logoAlpha.value)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "HALALYTICS",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.alpha(textAlpha.value),
                        letterSpacing = 4.sp
                    )
                    
                    Text(
                        text = "HEALTH & HALAL INTELLIGENCE",
                        fontSize = 12.sp,
                        color = MintAccent,
                        modifier = Modifier.alpha(textAlpha.value),
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
