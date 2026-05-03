package com.example.halalyticscompose.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

/**
 * LocalizationManager
 * Mengelola bahasa aplikasi & localization
 * Mendukung: English (EN) & Indonesian (ID)
 */
class LocalizationManager(private val context: Context) {

    companion object {
        const val LANGUAGE_EN = "en"
        const val LANGUAGE_ID = "id"
        
        // SharedPreferences keys
        private const val PREF_LANGUAGE = "app_language"
        private const val PREF_NAME = "halalytics_prefs"
    }

    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Dapatkan bahasa yang sedang aktif
     */
    fun getCurrentLanguage(): String {
        return sharedPreferences.getString(PREF_LANGUAGE, LANGUAGE_ID) ?: LANGUAGE_ID
    }

    /**
     * Set bahasa aplikasi
     * Akan change Locale dan SharedPreferences
     */
    fun setLanguage(language: String) {
        // Validasi bahasa yang supported
        if (language != LANGUAGE_EN && language != LANGUAGE_ID) {
            return
        }

        // Save ke SharedPreferences
        sharedPreferences.edit().putString(PREF_LANGUAGE, language).apply()

        // Change Locale
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.locale = locale

        // Untuk Android 7.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            configuration.setLocales(localeList)
        }

        // Apply configuration
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    /**
     * Toggle antara English & Indonesian
     */
    fun toggleLanguage() {
        val currentLanguage = getCurrentLanguage()
        val newLanguage = if (currentLanguage == LANGUAGE_EN) LANGUAGE_ID else LANGUAGE_EN
        setLanguage(newLanguage)
    }

    /**
     * Dapatkan nama bahasa untuk display
     */
    fun getLanguageName(language: String): String {
        return when (language) {
            LANGUAGE_EN -> "English"
            LANGUAGE_ID -> "Bahasa Indonesia"
            else -> "Unknown"
        }
    }

    /**
     * Get language flag emoji
     */
    fun getLanguageFlag(language: String): String {
        return when (language) {
            LANGUAGE_EN -> "🇬🇧" // UK flag untuk English
            LANGUAGE_ID -> "🇮🇩" // Indonesian flag
            else -> "🌐"
        }
    }

    /**
     * Dapatkan list bahasa yang supported
     */
    fun getSupportedLanguages(): List<String> {
        return listOf(LANGUAGE_EN, LANGUAGE_ID)
    }

    /**
     * Dapatkan Locale object
     */
    fun getLocale(language: String = getCurrentLanguage()): Locale {
        return Locale(language)
    }
}
