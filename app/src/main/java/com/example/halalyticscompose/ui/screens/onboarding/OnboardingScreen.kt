package com.example.halalyticscompose.ui.screens.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.halalyticscompose.ui.screens.OnboardingScreen

/**
 * Thin wrapper to keep backward compatibility for imports that reference
 * `com.example.halalyticscompose.ui.screens.onboarding.OnboardingScreen`.
 *
 * This file delegates to the canonical `ui.screens.OnboardingScreen` implementation.
 */
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onFinish: () -> Unit = {},
    onDemoAccountClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    OnboardingScreen(
        navController = null,
        onFinish = onFinish,
        onDemoAccountClick = onDemoAccountClick,
        onSignUpClick = onSignUpClick,
        onLoginClick = onLoginClick
    )
}
