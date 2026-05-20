package com.example.halalyticscompose.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.halalyticscompose.R
import com.example.halalyticscompose.data.model.User
import com.example.halalyticscompose.ui.viewmodel.AuthViewModel
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val userData by viewModel.userData.collectAsState()
    val isLoadingVM by viewModel.isLoading.collectAsState()
    
    // Form States
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var bloodType by remember { mutableStateOf("-") }
    var selectedDiet by remember { mutableStateOf("None") }
    var activityLevel by remember { mutableStateOf("medium") }
    var address by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var allergy by remember { mutableStateOf("") }
    var medicalHistory by remember { mutableStateOf("") }
    
    // Password States
    var showPasswordDialog by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    var isInitialized by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }
    
    LaunchedEffect(userData) {
        userData?.let { user ->
            if (!isInitialized) {
                fullName = user.fullName ?: ""
                phone = user.phone ?: ""
                email = user.email
                bio = user.bio ?: ""
                height = user.height?.toString() ?: ""
                weight = user.weight?.toString() ?: ""
                age = user.age?.toString() ?: ""
                gender = user.gender ?: "Male"
                bloodType = user.bloodType ?: "-"
                selectedDiet = user.dietPreference ?: "None"
                activityLevel = user.activityLevel ?: "medium"
                allergy = user.allergy ?: ""
                medicalHistory = user.medicalHistory ?: ""
                address = user.address ?: ""
                emergencyContact = user.emergencyContact ?: ""
                birthDate = user.birthDate ?: ""
                isInitialized = true
            }
        }
    }

    fun handleSave() {
        if (fullName.isBlank()) {
            Toast.makeText(context, context.getString(R.string.edit_profile_name_empty), Toast.LENGTH_SHORT).show()
            return
        }

        val imageFile = selectedImageUri?.let { uri ->
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "profile_update_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                file
            } catch (e: Exception) { null }
        }

        viewModel.updateProfile(
            fullName = fullName,
            phone = phone,
            bio = bio,
            height = height.toDoubleOrNull(),
            weight = weight.toDoubleOrNull(),
            age = age.toIntOrNull(),
            gender = gender,
            bloodType = bloodType,
            dietPreference = selectedDiet,
            activityLevel = activityLevel,
            allergy = allergy,
            medicalHistory = medicalHistory,
            address = address,
            emergencyContact = emergencyContact,
            birthDate = birthDate,
            image = imageFile,
            onSuccess = {
                Toast.makeText(context, context.getString(R.string.edit_profile_success), Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            },
            onError = { msg ->
                Toast.makeText(context, context.getString(R.string.edit_profile_failed, msg), Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (userData == null && !isInitialized) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_profile_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                actions = {
                    Button(
                        onClick = { handleSave() },
                        enabled = !isLoadingVM,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (isLoadingVM) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.edit_profile_save))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // --- HEADER & PHOTO ---
            item {
                ProfilePhotoHeader(
                    userData = userData,
                    selectedUri = selectedImageUri,
                    onPickPhoto = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                )
            }

            // --- PERSONAL INFO SECTION ---
            item {
                SectionHeader(stringResource(R.string.edit_profile_personal_info), Icons.Default.Person)
                EditCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        EditTextField(value = fullName, onValueChange = { fullName = it }, label = stringResource(R.string.edit_profile_full_name), icon = Icons.Default.Badge)
                        EditTextField(value = email, onValueChange = { }, label = stringResource(R.string.edit_profile_email), icon = Icons.Default.Email, enabled = false)
                        EditTextField(value = phone, onValueChange = { phone = it }, label = stringResource(R.string.edit_profile_phone), icon = Icons.Default.Phone, keyboardType = KeyboardType.Phone)
                        EditTextField(value = birthDate, onValueChange = { birthDate = it }, label = stringResource(R.string.edit_profile_birthdate), icon = Icons.Default.Cake, placeholder = "YYYY-MM-DD")
                        EditTextField(value = address, onValueChange = { address = it }, label = stringResource(R.string.edit_profile_address), icon = Icons.Default.Home)
                        EditTextField(value = bio, onValueChange = { bio = it }, label = stringResource(R.string.edit_profile_bio), icon = Icons.Default.EditNote, singleLine = false, minLines = 2)
                    }
                }
            }

            // --- HEALTH PROFILE SECTION ---
            item {
                SectionHeader(stringResource(R.string.edit_profile_health_profile), Icons.Default.HealthAndSafety)
                EditCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            EditTextField(value = height, onValueChange = { height = it }, label = stringResource(R.string.edit_profile_height), icon = Icons.Default.Height, modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
                            EditTextField(value = weight, onValueChange = { weight = it }, label = stringResource(R.string.edit_profile_weight), icon = Icons.Default.Scale, modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            EditTextField(value = age, onValueChange = { age = it }, label = stringResource(R.string.edit_profile_age), icon = Icons.Default.CalendarToday, modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
                            BloodTypeDropdown(selected = bloodType, onSelected = { bloodType = it }, modifier = Modifier.weight(1f))
                        }
                        GenderSelector(selected = gender, onSelected = { gender = it })
                        EditTextField(value = emergencyContact, onValueChange = { emergencyContact = it }, label = stringResource(R.string.edit_profile_emergency), icon = Icons.Default.ContactPhone, keyboardType = KeyboardType.Phone)
                        EditTextField(value = allergy, onValueChange = { allergy = it }, label = stringResource(R.string.edit_profile_allergy), icon = Icons.Default.Warning, placeholder = stringResource(R.string.edit_profile_allergy_placeholder))
                        EditTextField(value = medicalHistory, onValueChange = { medicalHistory = it }, label = stringResource(R.string.edit_profile_medical_history), icon = Icons.Default.History, singleLine = false, minLines = 2)
                    }
                }
            }

            // --- SECURITY SECTION ---
            item {
                SectionHeader(stringResource(R.string.edit_profile_security), Icons.Default.Security)
                EditCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPasswordDialog = true }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.edit_profile_change_password), fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.edit_profile_change_password_desc), fontSize = 12.sp, color = Color.Gray)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.Gray)
                    }
                }
            }
        }
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            currentPassword = currentPassword,
            newPassword = newPassword,
            confirmPassword = confirmPassword,
            onCurrentChange = { currentPassword = it },
            onNewChange = { newPassword = it },
            onConfirmChange = { confirmPassword = it },
            onDismiss = { showPasswordDialog = false },
            onSubmit = {
                if (newPassword != confirmPassword) {
                    Toast.makeText(context, context.getString(R.string.edit_profile_password_mismatch), Toast.LENGTH_SHORT).show()
                } else if (newPassword.length < 6) {
                    Toast.makeText(context, context.getString(R.string.edit_profile_password_too_short), Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.changePassword(
                        current = currentPassword,
                        new = newPassword,
                        confirm = confirmPassword,
                        onSuccess = {
                            Toast.makeText(context, context.getString(R.string.edit_profile_password_success), Toast.LENGTH_SHORT).show()
                            showPasswordDialog = false
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                        },
                        onError = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            isLoading = isLoadingVM
        )
    }
}

@Composable
fun ProfilePhotoHeader(
    userData: User?,
    selectedUri: android.net.Uri?,
    onPickPhoto: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.primary.copy(0.1f), Color.Transparent)
                    )
                )
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier
                        .size(110.dp)
                        .border(4.dp, Color.White, CircleShape)
                        .shadow(8.dp, CircleShape),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    if (selectedUri != null) {
                        AsyncImage(
                            model = selectedUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (!userData?.image.isNullOrBlank()) {
                        AsyncImage(
                            model = userData?.image,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primary.copy(0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                
                SmallFloatingActionButton(
                    onClick = onPickPhoto,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.offset(x = (-4).dp, y = (-4).dp)
                ) {
                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(userData?.username ?: "username", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun EditCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun EditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    placeholder: String = "",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, modifier = Modifier.size(20.dp)) },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        minLines = minLines,
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder, color = Color.Gray) },
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodTypeDropdown(selected: String, onSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    val types = listOf("-", "A", "B", "AB", "O")
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.edit_profile_blood_type)) },
            leadingIcon = { Icon(Icons.Default.WaterDrop, null, modifier = Modifier.size(20.dp), tint = Color.Red) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            types.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun GenderSelector(selected: String, onSelected: (String) -> Unit) {
    Column {
        Text(stringResource(R.string.edit_profile_gender), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selected == "Male",
                onClick = { onSelected("Male") },
                label = { Text(stringResource(R.string.edit_profile_gender_male)) },
                leadingIcon = { if (selected == "Male") Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
            )
            FilterChip(
                selected = selected == "Female",
                onClick = { onSelected("Female") },
                label = { Text(stringResource(R.string.edit_profile_gender_female)) },
                leadingIcon = { if (selected == "Female") Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
            )
        }
    }
}

@Composable
fun ChangePasswordDialog(
    currentPassword: String,
    newPassword: String,
    confirmPassword: String,
    onCurrentChange: (String) -> Unit,
    onNewChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean
) {
    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_profile_change_password), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PasswordTextField(
                    value = currentPassword,
                    onValueChange = onCurrentChange,
                    label = stringResource(R.string.edit_profile_current_password),
                    isVisible = showCurrent,
                    onToggleVisibility = { showCurrent = !showCurrent }
                )
                PasswordTextField(
                    value = newPassword,
                    onValueChange = onNewChange,
                    label = stringResource(R.string.edit_profile_new_password),
                    isVisible = showNew,
                    onToggleVisibility = { showNew = !showNew }
                )
                PasswordTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmChange,
                    label = stringResource(R.string.edit_profile_confirm_password),
                    isVisible = showConfirm,
                    onToggleVisibility = { showConfirm = !showConfirm }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = !isLoading && currentPassword.isNotEmpty() && newPassword.isNotEmpty(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                else Text(stringResource(R.string.edit_profile_change_password))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.edit_profile_cancel)) }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
            }
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}
