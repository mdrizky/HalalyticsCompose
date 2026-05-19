package com.example.halalyticscompose.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.halalyticscompose.R

// Color Constants moved to theme-aware components
private val Navy = Color(0xFF001F3F)

data class FeatureActionItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val iconTint: Color = Navy,
    val bgTint: Color = Color(0xFFE0F2F1)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllFeaturesScreen(navController: NavController) {
    // Categorize features for a true Super App feel
    val coreFeatures = listOf(
        FeatureActionItem(stringResource(R.string.feature_scan_halal), Icons.Default.QrCode2, "scan", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_bpom), Icons.Default.HealthAndSafety, "bpom_scanner", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_cosmetic), Icons.Default.AutoAwesome, "skincare_scanner", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_check_medicine), Icons.Default.Medication, "drug_interaction", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_medicine_search), Icons.Default.Search, "medicine_search", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_intl_medicine), Icons.Default.Public, "international_medicine", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    )

    val healthSuiteFeatures = listOf(
        FeatureActionItem(stringResource(R.string.feature_medical_records), Icons.Default.MedicalServices, "medical_records", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_medical_resume), Icons.Default.Description, "medical_resume", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_health_monitor), Icons.Default.MonitorHeart, "health_monitor", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_bmi_calculator), Icons.Default.Calculate, "bmi_calculator", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_medical_info), Icons.Default.Info, "medical_info", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_mental_health), Icons.Default.Psychology, "mental_health_hub", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    )

    val smartAiFeatures = listOf(
        FeatureActionItem("AI Chat Halalytics", Icons.Default.Chat, "ai_chat", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_ai_assistant), Icons.Default.SmartToy, "health_assistant", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_health_journey), Icons.Default.CalendarMonth, "health_journey", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_nutrition_scan), Icons.Default.CameraAlt, "nutrition_scanner", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_health_diary), Icons.Default.Edit, "health_diary", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_pill_scanner), Icons.Default.PhotoCamera, "pill_scanner", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_weekly_report), Icons.Default.Assessment, "weekly_report", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    )

    val aiExpansionFeatures = listOf(
        FeatureActionItem(stringResource(R.string.feature_ocr_product), Icons.Default.QrCodeScanner, "ocr_scan", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_nutrition_ai), Icons.Default.MonitorHeart, "nutrition_dashboard", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_recipe_ai), Icons.Default.MenuBook, "recipes", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_daily_mission), Icons.Default.TaskAlt, "daily_mission_dashboard", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_encyclopedia), Icons.Default.LibraryBooks, "encyclopedia", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    )

    val supportFeatures = listOf(
        FeatureActionItem("Donasi", Icons.Default.VolunteerActivism, "donations", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_community), Icons.Default.Groups, "community_hub", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_health_pass), Icons.Default.VerifiedUser, "health_pass", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_emergency), Icons.Default.LocalHospital, "emergency_p3k", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_report_issue), Icons.Default.Warning, "report_issue/0/General", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_medicine_reminders), Icons.Default.Alarm, "medicine_reminders", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    )

    val accountFeatures = listOf(
        FeatureActionItem(stringResource(R.string.feature_settings), Icons.Default.Settings, "settings", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_help_center), Icons.Default.Help, "help_center", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_barcode_gallery), Icons.Default.Collections, "barcode_gallery", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        FeatureActionItem(stringResource(R.string.feature_privacy_policy), Icons.Default.Policy, "privacy_policy", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.all_features_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FeatureSectionGrid(stringResource(R.string.all_features_core), coreFeatures, navController)
            }
            item {
                FeatureSectionGrid(stringResource(R.string.all_features_health), healthSuiteFeatures, navController)
            }
            item {
                FeatureSectionGrid(stringResource(R.string.all_features_ai), smartAiFeatures, navController)
            }
            item {
                FeatureSectionGrid("Expansion & Tools", aiExpansionFeatures, navController)
            }
            item {
                FeatureSectionGrid("Support & Community", supportFeatures, navController)
            }
            item {
                FeatureSectionGrid("Account & System", accountFeatures, navController)
            }
        }
    }
}

@Composable
private fun FeatureSectionGrid(
    title: String,
    features: List<FeatureActionItem>,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
        )
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                features.chunked(4).forEach { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (rowItems.size == 4) 8.dp else 0.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowItems.forEach { item ->
                            val weightModifier = Modifier.weight(1f) // Distribute evenly
                            Box(modifier = weightModifier, contentAlignment = Alignment.Center) {
                                FeatureGridItem(item) {
                                    navController.navigate(item.route)
                                }
                            }
                        }
                        repeat(4 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureGridItem(item: FeatureActionItem, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(item.bgTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = item.iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp,
            modifier = Modifier.width(64.dp)
        )
    }
}
