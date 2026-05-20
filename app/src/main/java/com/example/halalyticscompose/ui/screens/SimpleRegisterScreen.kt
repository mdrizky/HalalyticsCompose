package com.example.halalyticscompose.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.halalyticscompose.data.model.RegisterRequest
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.halalyticscompose.R
import com.example.halalyticscompose.ui.components.PrimaryButton
import com.example.halalyticscompose.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleRegisterScreen(
    navController: NavController,
    viewModel: com.example.halalyticscompose.ui.viewmodel.AuthViewModel = hiltViewModel(),
    mainViewModel: com.example.halalyticscompose.ui.viewmodel.MainViewModel = hiltViewModel()
) {
    val isDarkMode by mainViewModel.isDarkMode.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val viewModelError by viewModel.errorMessage.collectAsState()
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var bloodType by remember { mutableStateOf("") }
    var allergy by remember { mutableStateOf("") }
    var medicalHistory by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = { navController.navigateUp() }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.halalyticscompose.R.drawable.logo_halalytics),
                        contentDescription = "Halalytics Logo",
                        modifier = Modifier.size(100.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = stringResource(R.string.register_create_account),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Full Name
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.register_full_name)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = stringResource(R.string.register_full_name),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Username
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.register_username)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = stringResource(R.string.register_username),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.register_email)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = stringResource(R.string.register_email),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.register_password)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = stringResource(R.string.register_password),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { isPasswordVisible = !isPasswordVisible }
                            ) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility 
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = if (isPasswordVisible) stringResource(R.string.register_hide_password) else stringResource(R.string.register_show_password),
                                    tint = if (isPasswordVisible) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None 
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Confirm Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.register_confirm_password)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = stringResource(R.string.register_confirm_password),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }
                            ) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility 
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = if (isConfirmPasswordVisible) stringResource(R.string.register_hide_password) else stringResource(R.string.register_show_password),
                                    tint = if (isConfirmPasswordVisible) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                        },
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None 
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Blood Type
                    var bloodTypeExpanded by remember { mutableStateOf(false) }
                    val bloodTypes = listOf("A", "B", "AB", "O")
                    
                    ExposedDropdownMenuBox(
                        expanded = bloodTypeExpanded,
                        onExpandedChange = { bloodTypeExpanded = !bloodTypeExpanded }
                    ) {
                        OutlinedTextField(
                            value = bloodType,
                            onValueChange = { },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                            label = { Text(stringResource(R.string.register_blood_type)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocalHospital,
                                    contentDescription = stringResource(R.string.register_blood_type),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = stringResource(R.string.common_dropdown),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                            )
                        )
                        
                        DropdownMenu(
                            expanded = bloodTypeExpanded,
                            onDismissRequest = { bloodTypeExpanded = false }
                        ) {
                            bloodTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        bloodType = type
                                        bloodTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Allergies
                    var allergyExpanded by remember { mutableStateOf(false) }
                    val allergies = listOf(
                        "Tidak ada",
                        "Diabetes",
                        "Makanan laut",
                        "Kacang tanah",
                        "Susu",
                        "Telur",
                        "Kedelai",
                        "Gandum",
                        "Lainnya"
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = allergyExpanded,
                        onExpandedChange = { allergyExpanded = !allergyExpanded }
                    ) {
                        OutlinedTextField(
                            value = allergy,
                            onValueChange = { },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                            label = { Text(stringResource(R.string.register_allergies)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = stringResource(R.string.register_allergies),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = stringResource(R.string.common_dropdown),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                            )
                        )
                        
                        DropdownMenu(
                            expanded = allergyExpanded,
                            onDismissRequest = { allergyExpanded = false }
                        ) {
                            allergies.forEach { allergyType ->
                                DropdownMenuItem(
                                    text = { Text(allergyType) },
                                    onClick = {
                                        allergy = allergyType
                                        allergyExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Medical History
                    OutlinedTextField(
                        value = medicalHistory,
                        onValueChange = { medicalHistory = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        label = { Text(stringResource(R.string.register_medical_history)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.MedicalServices,
                                contentDescription = stringResource(R.string.register_medical_history),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        placeholder = { Text(stringResource(R.string.register_medical_history_placeholder)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        maxLines = 4
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Error message
                    if (errorMessage.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFF5252).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                modifier = Modifier.padding(12.dp),
                                color = Color(0xFFFF5252),
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    val fullNameError = stringResource(R.string.register_error_full_name)
                    val usernameError = stringResource(R.string.register_error_username)
                    val emailError = stringResource(R.string.register_error_email)
                    val invalidEmailError = stringResource(R.string.register_error_invalid_email)
                    val passwordError = stringResource(R.string.register_error_password)
                    val passwordShortError = stringResource(R.string.register_error_password_short)
                    val confirmPasswordError = stringResource(R.string.register_error_confirm_password)
                    val passwordMismatchError = stringResource(R.string.register_error_password_mismatch)
                    val bloodTypeError = stringResource(R.string.register_error_blood_type)
                    val allergyError = stringResource(R.string.register_error_allergy)
                    val medicalHistoryError = stringResource(R.string.register_error_medical_history)

                    // Register button
                    PrimaryButton(
                        text = stringResource(R.string.register_create_account),
                        onClick = {
                            // Clear previous error
                            errorMessage = ""
                            
                            // Validate all required fields
                            if (fullName.isEmpty()) {
                                errorMessage = fullNameError
                                return@PrimaryButton
                            }
                            
                            if (username.isEmpty()) {
                                errorMessage = usernameError
                                return@PrimaryButton
                            }
                            
                            if (email.isEmpty()) {
                                errorMessage = emailError
                                return@PrimaryButton
                            }
                            
                            if (!email.contains("@")) {
                                errorMessage = invalidEmailError
                                return@PrimaryButton
                            }
                            
                            if (password.isEmpty()) {
                                errorMessage = passwordError
                                return@PrimaryButton
                            }
                            
                            if (password.length < 8) {
                                errorMessage = passwordShortError
                                return@PrimaryButton
                            }
                            
                            if (confirmPassword.isEmpty()) {
                                errorMessage = confirmPasswordError
                                return@PrimaryButton
                            }
                            
                            if (password != confirmPassword) {
                                errorMessage = passwordMismatchError
                                return@PrimaryButton
                            }
                            
                            if (bloodType.isEmpty()) {
                                errorMessage = bloodTypeError
                                return@PrimaryButton
                            }
                            
                            if (allergy.isEmpty()) {
                                errorMessage = allergyError
                                return@PrimaryButton
                            }
                            
                            if (medicalHistory.isEmpty()) {
                                errorMessage = medicalHistoryError
                                return@PrimaryButton
                            }
                            
                            errorMessage = ""
                            
                            viewModel.register(
                                com.example.halalyticscompose.data.model.RegisterRequest(
                                    fullName = fullName,
                                    username = username,
                                    email = email,
                                    password = password,
                                    passwordConfirmation = confirmPassword,
                                    phone = null,
                                    bloodType = bloodType,
                                    allergy = allergy,
                                    medicalHistory = medicalHistory
                                ),
                                onSuccess = {
                                    navController.navigate("splash") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = isLoading,
                        fullWidth = true,
                        enabled = !isLoading
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Back to login
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sudah punya akun? ",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        
                        Text(
                            text = "Masuk",
                            modifier = Modifier
                                .clickable {
                                    navController.navigate("login")
                                }
                                .padding(start = 4.dp),
                            color = Emerald,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
