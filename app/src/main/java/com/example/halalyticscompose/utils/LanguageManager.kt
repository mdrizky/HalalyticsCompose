package com.example.halalyticscompose.utils

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LanguageManager {
    fun currentLanguage(context: Context): String {
        return context.resources.configuration.locales[0]?.language ?: "id"
    }

    fun applyLanguageIfNeeded(activity: Activity, languageCode: String) {
        val supportedLocales = listOf("id", "en")
        val normalized = if (supportedLocales.contains(languageCode.lowercase())) languageCode.lowercase() else "id"
        if (currentLanguage(activity) == normalized) return

        // Modern explicit language switching
        val appLocale = LocaleListCompat.forLanguageTags(normalized)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}
