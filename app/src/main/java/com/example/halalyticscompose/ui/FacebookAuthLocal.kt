package com.example.halalyticscompose.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.facebook.CallbackManager

/**
 * [CallbackManager] harus sama instance antara [com.facebook.login.LoginManager]
 * dan [android.app.Activity.onActivityResult] / callback pipeline.
 */
val LocalFacebookCallbackManager = staticCompositionLocalOf<CallbackManager> {
    error("LocalFacebookCallbackManager belum disediakan — bungkus NavHost dengan CompositionLocalProvider di MainActivity.")
}
