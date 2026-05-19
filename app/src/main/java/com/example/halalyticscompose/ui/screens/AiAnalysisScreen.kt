package com.example.halalyticscompose.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.halalyticscompose.R
import com.example.halalyticscompose.ui.viewmodel.AiAnalysisViewModel
import com.example.halalyticscompose.ui.viewmodel.MainViewModel
import com.example.halalyticscompose.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAnalysisScreen(
    navController: NavController,
    mainViewModel: MainViewModel = hiltViewModel(),
    viewModel: AiAnalysisViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val color = MaterialTheme.colorScheme
    var ingredientsText by remember { mutableStateOf("") }
    
    val selectedProfile by mainViewModel.selectedFamilyProfile.collectAsState()
    val familyProfiles by mainViewModel.familyProfiles.collectAsState()

    val backStackEntry = navController.currentBackStackEntryAsState()
    val ingredientsArgument = backStackEntry.value?.arguments?.getString("ingredients") 
        ?.let { android.net.Uri.decode(it) } ?: ""
    
    LaunchedEffect(Unit) {
        ingredientsText = ingredientsArgument
        mainViewModel.fetchFamilyProfiles()
        
        if (ingredientsText.isNotEmpty()) {
            viewModel.analyzeIngredients(ingredientsText, selectedProfile?.id)
        }
    }
    
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(ingredientsText, selectedProfile) {
        if (ingredientsText.isNotEmpty()) {
            viewModel.analyzeIngredients(ingredientsText, selectedProfile?.id)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.ai_analysis_title), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(8.dp).clip(CircleShape).background(color.onSurface.copy(0.06f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = color.onSurface)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = color.surface)
            )
        },
        containerColor = color.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.status) {
                "Loading" -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = color.primary)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(stringResource(R.string.ai_analysis_loading), color = color.onSurfaceVariant)
                    }
                }
                "Success" -> {
                    val result = uiState.analysisResult!!
                    val analysisText = result.analysis.ifBlank { result.ringkasan.orEmpty() }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Profile Selector (Premium Style)
                        Text(
                            text = stringResource(R.string.ai_analysis_profile),
                            style = MaterialTheme.typography.labelSmall,
                            color = color.primary,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            fontWeight = FontWeight.Bold
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            item {
                                ProfileChip(
                                    name = stringResource(R.string.ai_analysis_myself),
                                    isSelected = selectedProfile == null,
                                    onClick = { mainViewModel.selectFamilyProfile(null) }
                                )
                            }
                            
                            items(familyProfiles.size) { index ->
                                val profile = familyProfiles[index]
                                ProfileChip(
                                    name = profile.name,
                                    isSelected = selectedProfile?.id == profile.id,
                                    onClick = { mainViewModel.selectFamilyProfile(profile) }
                                )
                            }
                        }

                        // Main Result Card
                        val statusColor = when (result.status.lowercase()) {
                            "halal" -> color.primary
                            "haram" -> color.error
                            "syubhat" -> color.tertiary
                            else -> color.onSurfaceVariant
                        }
                        
                        val statusIcon = when (result.status.lowercase()) {
                            "halal" -> Icons.Default.CheckCircle
                            "haram" -> Icons.Default.Cancel
                            else -> Icons.AutoMirrored.Filled.Help
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(color.surface)
                                .border(1.dp, color.outlineVariant.copy(alpha = 0.45f), RoundedCornerShape(32.dp))
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(statusColor.copy(0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(48.dp))
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Text(
                                    result.status.uppercase(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = statusColor
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    analysisText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = color.onSurface.copy(0.8f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Personal Health Alerts
                        if (uiState.healthAlerts.isNotEmpty()) {
                            Text(
                                stringResource(R.string.ai_analysis_alerts),
                                style = MaterialTheme.typography.titleLarge,
                                color = color.onBackground,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                            
                            uiState.healthAlerts.forEach { alert ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 6.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(color.tertiary.copy(0.1f))
                                        .border(1.dp, color.tertiary.copy(0.25f), RoundedCornerShape(20.dp))
                                        .padding(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, null, tint = color.tertiary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(alert, style = MaterialTheme.typography.bodyMedium, color = color.onSurface)
                                    }
                                }
                            }
                        }

                        // Ingredients Breakdown
                        if (result.redFlags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                stringResource(R.string.ai_analysis_critical),
                                style = MaterialTheme.typography.titleLarge,
                                color = color.onBackground,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                            
                            androidx.compose.foundation.layout.FlowRow(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                result.redFlags.forEach { flag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(color.error.copy(0.1f))
                                            .border(1.dp, color.error.copy(0.25f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(flag, color = color.error, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
                "Error" -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Error, null, tint = color.error, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            uiState.errorMessage ?: stringResource(R.string.ai_analysis_failed),
                            color = color.onSurface,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.linearGradient(listOf(color.primary, color.secondary)))
                                .clickable { viewModel.analyzeIngredients(ingredientsText) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.ai_analysis_retry), color = color.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileChip(name: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) color.primary else color.surface)
            .border(1.dp, if (isSelected) color.primary else color.outlineVariant.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            name, 
            color = if (isSelected) color.onPrimary else color.onSurface,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}
