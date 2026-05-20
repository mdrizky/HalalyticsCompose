package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.halalyticscompose.R
import com.example.halalyticscompose.ui.viewmodel.MainViewModel
import com.example.halalyticscompose.ui.theme.ErrorColor
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountManagementScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val userData by viewModel.userData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val textColor = MaterialTheme.colorScheme.onSurface
    val cardColor = MaterialTheme.colorScheme.surface
    val color = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_manage_account), color = textColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("edit_profile") }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Section
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(cardColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val imageUrl = userData?.avatarUrl ?: userData?.image
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Foto profil",
                        modifier = Modifier
                            .size(84.dp)
                            .background(cardColor, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = color.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = currentUser ?: stringResource(R.string.account_default_user),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Info Cards
            AccountInfoItem(
                label = stringResource(R.string.account_username),
                value = userData?.username ?: currentUser ?: "-",
                icon = Icons.Default.Person
            )

            AccountInfoItem(
                label = stringResource(R.string.account_email),
                value = userData?.email ?: "-",
                icon = Icons.Default.Email
            )

            AccountInfoItem(
                label = stringResource(R.string.account_phone),
                value = userData?.phone ?: "-",
                icon = Icons.Default.Phone
            )

            AccountInfoItem(
                label = stringResource(R.string.account_blood_type),
                value = userData?.bloodType ?: "-",
                icon = Icons.Default.Bloodtype
            )

            AccountInfoItem(
                label = stringResource(R.string.account_allergy),
                value = userData?.allergy ?: "-",
                icon = Icons.Default.HealthAndSafety
            )

            AccountInfoItem(
                label = stringResource(R.string.account_medical_history),
                value = userData?.medicalHistory ?: "-",
                icon = Icons.Default.Description
            )

            AccountInfoItem(
                label = "Height",
                value = userData?.height?.toString()?.let { "$it cm" } ?: "-",
                icon = Icons.Default.Straighten
            )

            AccountInfoItem(
                label = "Weight",
                value = userData?.weight?.toString()?.let { "$it kg" } ?: "-",
                icon = Icons.Default.FitnessCenter
            )

            AccountInfoItem(
                label = "Age",
                value = userData?.age?.toString() ?: "-",
                icon = Icons.Default.CalendarMonth
            )

            AccountInfoItem(
                label = "Goal",
                value = userData?.goal ?: "-",
                icon = Icons.Default.Flag
            )

            AccountInfoItem(
                label = "Activity Level",
                value = userData?.activityLevel ?: "-",
                icon = Icons.AutoMirrored.Filled.DirectionsRun
            )
            
            AccountInfoItem(
                label = stringResource(R.string.account_security_status),
                value = stringResource(R.string.account_verified),
                icon = Icons.Default.VerifiedUser,
                valueColor = color.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(stringResource(R.string.account_appearance), color = textColor, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.profile_dark_mode), color = textColor)
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { viewModel.toggleDarkMode() }
                        )
                    }
                    Text(stringResource(R.string.language), color = textColor, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = appLanguage == "id",
                            onClick = { viewModel.setAppLanguage("id") },
                            label = { Text("Indonesia") }
                        )
                        FilterChip(
                            selected = appLanguage == "en",
                            onClick = { viewModel.setAppLanguage("en") },
                            label = { Text("English") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Danger Zone
            Text(
                text = stringResource(R.string.account_danger_zone),
                modifier = Modifier.fillMaxWidth(),
                color = ErrorColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { /* Implement delete account or reset */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorColor.copy(alpha = 0.1f),
                    contentColor = ErrorColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.account_delete_data), fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { viewModel.logout(navController) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorColor
                ),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.profile_logout), fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun AccountInfoItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val color = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = label, fontSize = 12.sp, color = color.onSurfaceVariant)
                Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = valueColor)
            }
        }
    }
}
