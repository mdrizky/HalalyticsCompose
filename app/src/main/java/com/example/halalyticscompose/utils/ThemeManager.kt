package com.example.halalyticscompose.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * ThemeManager
 * Mengelola tema aplikasi (Light/Dark Mode)
 * Mendukung: Light, Dark, System Default
 */
class ThemeManager(private val context: Context) {

    companion object {
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_SYSTEM = "system"
        
        // SharedPreferences keys
        private const val PREF_THEME = "app_theme"
        private const val PREF_NAME = "halalytics_prefs"
    }

    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Dapatkan tema yang sedang aktif
     */
    fun getCurrentTheme(): String {
        return sharedPreferences.getString(PREF_THEME, THEME_SYSTEM) ?: THEME_SYSTEM
    }

    /**
     * Set tema aplikasi
     * Akan change AppCompatDelegate mode
     */
    fun setTheme(theme: String) {
        // Validasi tema yang supported
        if (theme != THEME_LIGHT && theme != THEME_DARK && theme != THEME_SYSTEM) {
            return
        }

        // Save ke SharedPreferences
        sharedPreferences.edit().putString(PREF_THEME, theme).apply()

        // Apply tema ke AppCompatDelegate
        when (theme) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    /**
     * Toggle antara Light & Dark mode
     * System mode tidak di-toggle, langsung switch ke Dark
     */
    fun toggleTheme() {
        val currentTheme = getCurrentTheme()
        val newTheme = when (currentTheme) {
            THEME_LIGHT -> THEME_DARK
            THEME_DARK -> THEME_LIGHT
            THEME_SYSTEM -> THEME_DARK // Jika system, toggle ke dark
            else -> THEME_LIGHT
        }
        setTheme(newTheme)
    }

    /**
     * Check apakah saat ini dark mode aktif
     */
    fun isDarkMode(): Boolean {
        val currentTheme = getCurrentTheme()
        return when (currentTheme) {
            THEME_DARK -> true
            THEME_LIGHT -> false
            THEME_SYSTEM -> {
                // Check system setting
                val nightMode = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
            else -> false
        }
    }

    /**
     * Dapatkan nama tema untuk display
     */
    fun getThemeName(theme: String): String {
        return when (theme) {
            THEME_LIGHT -> "Light"
            THEME_DARK -> "Dark"
            THEME_SYSTEM -> "System Default"
            else -> "Unknown"
        }
    }

    /**
     * Get theme icon emoji
     */
    fun getThemeIcon(theme: String): String {
        return when (theme) {
            THEME_LIGHT -> "☀️" // Sun icon untuk light
            THEME_DARK -> "🌙" // Moon icon untuk dark
            THEME_SYSTEM -> "⚙️" // Settings icon untuk system
            else -> "🎨"
        }
    }

    /**
     * Dapatkan list tema yang supported
     */
    fun getSupportedThemes(): List<String> {
        return listOf(THEME_LIGHT, THEME_DARK, THEME_SYSTEM)
    }

    /**
     * Initialize tema saat app startup
     */
    fun initializeTheme() {
        val currentTheme = getCurrentTheme()
        setTheme(currentTheme)
    }
}
