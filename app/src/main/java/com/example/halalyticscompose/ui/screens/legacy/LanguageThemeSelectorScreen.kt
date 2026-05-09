package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.halalyticscompose.ui.viewmodel.MainViewModel
import com.example.halalyticscompose.utils.LocalizationManager
import com.example.halalyticscompose.utils.ThemeManager

/**
 * Enhanced Language & Theme Selector
 * Menampilkan pilihan bahasa dan tema dengan visual yang clear
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageThemeSelectorBottomSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    val themeManager = remember { ThemeManager(context) }
    
    val appLanguage by viewModel.appLanguage.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    
    var selectedLanguage by remember { mutableStateOf(appLanguage) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ═══════════════════════════════════════════════════
        // LANGUAGE SELECTOR
        // ═══════════════════════════════════════════════════
        
        Text(
            text = "🌐 Pilih Bahasa / Choose Language",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Current Language Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showLanguageDialog = !showLanguageDialog }
                .clip(RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Language, "Language", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Column {
                        Text("Bahasa Saat Ini", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = when (selectedLanguage) {
                                LocalizationManager.LANGUAGE_EN -> "English 🇬🇧"
                                LocalizationManager.LANGUAGE_ID -> "Bahasa Indonesia 🇮🇩"
                                else -> "Unknown"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Icon(Icons.Default.Check, "Selected", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Language Options (Expandable)
        if (showLanguageDialog) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    LocalizationManager.LANGUAGE_ID to "Bahasa Indonesia 🇮🇩",
                    LocalizationManager.LANGUAGE_EN to "English 🇬🇧"
                ).forEach { (languageCode, languageName) ->
                    LanguageOption(
                        language = languageName,
                        isSelected = selectedLanguage == languageCode,
                        onClick = {
                            selectedLanguage = languageCode
                            viewModel.setAppLanguage(languageCode)
                            localizationManager.setLanguage(languageCode)
                            showLanguageDialog = false
                        }
                    )
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // ═══════════════════════════════════════════════════
        // THEME SELECTOR
        // ═══════════════════════════════════════════════════
        
        Text(
            text = "🎨 Pilih Tema / Choose Theme",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Current Theme Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showThemeDialog = !showThemeDialog }
                .clip(RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.DarkMode, "Theme", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Column {
                        Text("Tema Saat Ini", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = when {
                                isDarkMode -> "🌙 Dark Mode"
                                else -> "☀️ Light Mode"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Icon(Icons.Default.Check, "Selected", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Theme Options (Expandable)
        if (showThemeDialog) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Light" to "☀️ Light Mode",
                    "Dark" to "🌙 Dark Mode"
                ).forEach { (themeMode, themeName) ->
                    ThemeOption(
                        theme = themeName,
                        isSelected = (isDarkMode && themeMode == "Dark") || (!isDarkMode && themeMode == "Light"),
                        onClick = {
                            viewModel.toggleDarkMode()
                            showThemeDialog = false
                        }
                    )
                }
            }
        }

        // Info Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = "💡 Tip: Ubah bahasa dan tema akan langsung diterapkan ke seluruh aplikasi",
                fontSize = 12.sp,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Language Option Component
 */
@Composable
fun LanguageOption(
    language: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .clip(RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.material3.CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.foundation.BorderStroke(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            ).brush
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Theme Option Component
 */
@Composable
fun ThemeOption(
    theme: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .clip(RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.material3.CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.foundation.BorderStroke(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            ).brush
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = theme,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
