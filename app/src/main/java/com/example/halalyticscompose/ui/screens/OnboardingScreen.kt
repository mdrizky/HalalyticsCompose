package com.example.halalyticscompose.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.example.halalyticscompose.R
import androidx.compose.ui.tooling.preview.Preview
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
import androidx.navigation.NavController
import com.example.halalyticscompose.ui.components.PrimaryButton
import com.example.halalyticscompose.ui.components.SecondaryButton
import com.example.halalyticscompose.ui.theme.*
import kotlinx.coroutines.launch

private data class OnboardingSlide(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val gradient: List<Color>
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    navController: NavController? = null,
    onFinish: (() -> Unit)? = null,
    onDemoAccountClick: (() -> Unit)? = null,
    onSignUpClick: (() -> Unit)? = null,
    onLoginClick: (() -> Unit)? = null
) {
    val slides = listOf(
        OnboardingSlide(
            icon = Icons.Default.QrCodeScanner,
            title = stringResource(R.string.onboarding_title_scan),
            description = stringResource(R.string.onboarding_desc_scan),
            gradient = listOf(Color(0xFF004D40), Color(0xFF00695C))
        ),
        OnboardingSlide(
            icon = Icons.Default.Psychology,
            title = stringResource(R.string.onboarding_title_ai),
            description = stringResource(R.string.onboarding_desc_ai),
            gradient = listOf(Color(0xFF00695C), Color(0xFF26A69A))
        ),
        OnboardingSlide(
            icon = Icons.Default.Storage,
            title = stringResource(R.string.onboarding_title_db),
            description = stringResource(R.string.onboarding_desc_db),
            gradient = listOf(Color(0xFF4A148C), Color(0xFF7C43BD))
        ),
        OnboardingSlide(
            icon = Icons.Default.Rocket,
            title = stringResource(R.string.onboarding_title_start),
            description = stringResource(R.string.onboarding_desc_start),
            gradient = listOf(Color(0xFFE65100), Color(0xFFFF8F00))
        )
    )

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()

    val finishAction: () -> Unit = onFinish ?: {
        navController?.navigate("login") {
            popUpTo("onboarding") { inclusive = true }
        }
        Unit
    }
    val loginAction: () -> Unit = onLoginClick ?: finishAction
    val signUpAction: () -> Unit = onSignUpClick ?: {
        navController?.navigate("register")
        Unit
    }
    val demoAction: () -> Unit = onDemoAccountClick ?: loginAction

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        EmeraldLight.copy(alpha = 0.3f),
                        TealLight.copy(alpha = 0.5f)
                    )
                )
            )
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPage(slide = slides[page])
        }

        PageIndicators(
            pageCount = slides.size,
            currentPage = pagerState.currentPage,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = HalalyticsDimensions.paddingLarge)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = HalalyticsDimensions.paddingLarge, vertical = 28.dp)
        ) {
            if (pagerState.currentPage == slides.lastIndex) {
                PrimaryButton(
                    text = stringResource(R.string.onboarding_button_start),
                    onClick = signUpAction,
                    modifier = Modifier.fillMaxWidth(),
                    fullWidth = true
                )

                Spacer(modifier = Modifier.height(HalalyticsDimensions.space_4))

                SecondaryButton(
                    text = stringResource(R.string.onboarding_button_demo),
                    onClick = demoAction,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(HalalyticsDimensions.space_5))

                Text(
                    text = stringResource(R.string.onboarding_have_account),
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { loginAction() }
                        .padding(vertical = HalalyticsDimensions.paddingMedium)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SecondaryButton(
                        text = stringResource(R.string.onboarding_skip),
                        onClick = finishAction,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(HalalyticsDimensions.paddingMedium))

                    PrimaryButton(
                        text = stringResource(R.string.onboarding_next),
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(slide: OnboardingSlide) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(slide.gradient)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = slide.icon,
                    contentDescription = slide.title,
                    tint = Color.White,
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = slide.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = slide.description,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun PageIndicators(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            Box(
                modifier = Modifier
                    .size(if (isActive) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (isActive) Color.White else Color.White.copy(alpha = 0.4f))
            )

            if (index < pageCount - 1) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

private val EaseInOutSine: Easing = Easing { fraction ->
    (-(kotlin.math.cos(Math.PI * fraction).toFloat() - 1f)) / 2f
}

@Composable
@Preview(showBackground = true)
fun PreviewOnboardingScreen() {
    OnboardingScreen()
}
