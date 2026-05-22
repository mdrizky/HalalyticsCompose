package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.example.halalyticscompose.R
import com.example.halalyticscompose.ui.theme.*
import com.example.halalyticscompose.ui.viewmodel.MainViewModel
import com.example.halalyticscompose.utils.BiometricAuthHelper
import com.example.halalyticscompose.utils.CrashReporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val isNotifEnabled by viewModel.isNotifEnabled.collectAsState()
    val privacyModeEnabled by viewModel.privacyModeEnabled.collectAsState()
    val biometricLockEnabled by viewModel.biometricLockEnabled.collectAsState()
    val autoLogoutEnabled by viewModel.autoLogoutEnabled.collectAsState()
    val autoLogoutMinutes by viewModel.autoLogoutMinutes.collectAsState()
    var crashInfo by remember { mutableStateOf(CrashReporter.getLastCrash(context)) }
    var biometricNotice by remember { mutableStateOf<String?>(null) }
    
    val layoutDirection = if (appLanguage == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            containerColor = Slate50,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.settings_title),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Emerald)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Appearance Section ──
                SettingsSectionTitle(stringResource(R.string.dark_mode))
                SettingsCard {
                    SettingsSwitchRow(
                        title = stringResource(R.string.dark_mode),
                        icon = Icons.Default.DarkMode,
                        iconBg = EmeraldLight.copy(alpha = 0.2f),
                        iconTint = Emerald,
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() }
                    )
                }

                // ── Language Section ──
                SettingsSectionTitle(stringResource(R.string.language))
                SettingsCard {
                    Column(modifier = Modifier.padding(8.dp)) {
                        LanguageOption("id", "Indonesia", "🇮🇩", appLanguage) { viewModel.setAppLanguage("id") }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp), color = Slate100, thickness = 1.dp)
                        LanguageOption("en", "English", "🇺🇸", appLanguage) { viewModel.setAppLanguage("en") }
                    }
                }

                // ── Notifications Section ──
                SettingsSectionTitle(stringResource(R.string.notifications))
                SettingsCard {
                    SettingsSwitchRow(
                        title = stringResource(R.string.notifications),
                        icon = Icons.Default.Notifications,
                        iconBg = EmeraldLight.copy(alpha = 0.2f),
                        iconTint = Emerald,
                        checked = isNotifEnabled,
                        onCheckedChange = { viewModel.setNotifEnabled(it) }
                    )
                }

                // ── Security Section ──
                SettingsSectionTitle(stringResource(R.string.section_security))
                SettingsCard {
                    Column(modifier = Modifier.padding(8.dp)) {
                        SettingsSwitchRow(
                            title = stringResource(R.string.settings_privacy_mode),
                            icon = Icons.Default.Shield,
                            iconBg = EmeraldLight.copy(alpha = 0.2f),
                            iconTint = Emerald,
                            checked = privacyModeEnabled,
                            onCheckedChange = viewModel::setPrivacyModeEnabled
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp), color = Slate100, thickness = 1.dp)
                        SettingsSwitchRow(
                            title = stringResource(R.string.settings_biometric_lock),
                            icon = Icons.Default.Fingerprint,
                            iconBg = EmeraldLight.copy(alpha = 0.2f),
                            iconTint = Emerald,
                            checked = biometricLockEnabled,
                            onCheckedChange = { enabled ->
                                if (!enabled) {
                                    biometricNotice = null
                                    viewModel.setBiometricLockEnabled(false)
                                } else {
                                    val activity = context as? FragmentActivity
                                    if (activity == null) {
                                        biometricNotice = context.getString(R.string.settings_biometric_unavailable)
                                        viewModel.setBiometricLockEnabled(false)
                                    } else if (!BiometricAuthHelper.canAuthenticate(activity)) {
                                        biometricNotice = context.getString(R.string.settings_biometric_not_active)
                                        viewModel.setBiometricLockEnabled(false)
                                    } else {
                                        BiometricAuthHelper.authenticate(
                                            activity = activity,
                                            executor = ContextCompat.getMainExecutor(activity),
                                            title = context.getString(R.string.settings_biometric_title),
                                            subtitle = context.getString(R.string.settings_biometric_subtitle),
                                            description = context.getString(R.string.settings_biometric_desc),
                                            onSuccess = {
                                                biometricNotice = context.getString(R.string.settings_biometric_success)
                                                viewModel.setBiometricLockEnabled(true)
                                            },
                                            onError = { err ->
                                                biometricNotice = err
                                                viewModel.setBiometricLockEnabled(false)
                                            }
                                        )
                                    }
                                }
                            }
                        )
                        biometricNotice?.let {
                            Text(
                                text = it,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp), color = Slate100, thickness = 1.dp)
                        SettingsSwitchRow(
                            title = stringResource(R.string.settings_auto_logout),
                            icon = Icons.Default.Timer,
                            iconBg = EmeraldLight.copy(alpha = 0.2f),
                            iconTint = Emerald,
                            checked = autoLogoutEnabled,
                            onCheckedChange = viewModel::setAutoLogoutEnabled
                        )

                        if (autoLogoutEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.settings_timeout_minutes),
                                modifier = Modifier.padding(horizontal = 12.dp),
                                fontSize = 12.sp,
                                color = Slate500
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(5, 15, 30).forEach { minutes ->
                                    FilterChip(
                                        selected = autoLogoutMinutes == minutes,
                                        onClick = { viewModel.setAutoLogoutMinutes(minutes) },
                                        label = { Text("$minutes mnt", color = if (autoLogoutMinutes == minutes) Color.White else Slate700) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Emerald,
                                            containerColor = Slate100
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }

                // ── Watchlist Section ──
                SettingsSectionTitle(stringResource(R.string.watchlist))
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("watchlist_editor") }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(EmeraldLight.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.RemoveRedEye, contentDescription = null, tint = Emerald, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(R.string.watchlist), fontWeight = FontWeight.Medium, color = Slate900)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Slate400)
                    }
                }

                // ── About Section ──
                SettingsSectionTitle(stringResource(R.string.section_about))
                SettingsCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.settings_app_version), fontWeight = FontWeight.Bold, color = Emerald)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(stringResource(R.string.settings_app_desc), fontSize = 14.sp, color = Slate600)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.settings_copyright), fontSize = 12.sp, color = Slate400)
                    }
                }

                // ── Crash Diagnostics ──
                SettingsSectionTitle(stringResource(R.string.section_crash_diagnostics))
                SettingsCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (crashInfo.isNullOrBlank()) {
                            Text(
                                text = stringResource(R.string.settings_no_crash),
                                color = Slate500
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.settings_last_crash),
                                color = Slate600,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = crashInfo!!.take(650),
                                fontSize = 12.sp,
                                color = Slate900,
                                maxLines = 12
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    CrashReporter.clearLastCrash(context)
                                    crashInfo = null
                                }
                            ) {
                                Text(stringResource(R.string.settings_clear_crash), color = Slate700)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// SETTINGS COMPONENTS
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun SettingsSwitchRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color = EmeraldLight.copy(alpha = 0.2f),
    iconTint: Color = Emerald,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Slate900)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Emerald)
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Emerald,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        content()
    }
}

@Composable
private fun LanguageOption(
    code: String,
    name: String,
    flag: String,
    currentCode: String,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(flag, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Text(name, fontWeight = if (code == currentCode) FontWeight.Bold else FontWeight.Normal, color = Slate900)
        }
        RadioButton(
            selected = code == currentCode,
            onClick = { onSelect() },
            colors = RadioButtonDefaults.colors(selectedColor = Emerald, unselectedColor = Slate400)
        )
    }
}
