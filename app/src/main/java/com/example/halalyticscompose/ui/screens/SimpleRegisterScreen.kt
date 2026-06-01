package com.example.halalyticscompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.halalyticscompose.R
import com.example.halalyticscompose.ui.viewmodel.AuthViewModel
import com.example.halalyticscompose.data.model.RegisterRequest
import com.example.halalyticscompose.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleRegisterScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    val isLoading by viewModel.isLoading.collectAsState()
    val apiErrorMessage by viewModel.errorMessage.collectAsState()
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var bloodType by remember { mutableStateOf("") }
    var allergy by remember { mutableStateOf("") }
    var medicalHistory by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var localErrorMessage by remember { mutableStateOf("") }

    val displayErrorMessage = apiErrorMessage ?: localErrorMessage

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
            IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back)) }
            
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.logo_halalytics_transparent),
                    contentDescription = "Halalytics Logo",
                    modifier = Modifier.size(100.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            Card(shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(16.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(stringResource(R.string.register_create_account), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text(stringResource(R.string.register_full_name)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), leadingIcon = { Icon(Icons.Default.Person, null) })
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text(stringResource(R.string.register_username)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(stringResource(R.string.register_email)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text(stringResource(R.string.register_password)) }, visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) { Icon(if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null) } }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text(stringResource(R.string.register_confirm_password)) }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    var bloodTypeExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = bloodTypeExpanded, onExpandedChange = { bloodTypeExpanded = it }) {
                        OutlinedTextField(
                            value = bloodType, 
                            onValueChange = {}, 
                            readOnly = true, 
                            label = { Text(stringResource(R.string.register_blood_type)) }, 
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, 
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true), 
                            shape = RoundedCornerShape(16.dp)
                        )
                        DropdownMenu(expanded = bloodTypeExpanded, onDismissRequest = { bloodTypeExpanded = false }) {
                            listOf("A", "B", "AB", "O").forEach { DropdownMenuItem(text = { Text(it) }, onClick = { bloodType = it; bloodTypeExpanded = false }) }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = allergy, onValueChange = { allergy = it }, label = { Text(stringResource(R.string.register_allergies)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = medicalHistory, onValueChange = { medicalHistory = it }, label = { Text(stringResource(R.string.register_medical_history_label)) }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(16.dp))
                    
                    if (displayErrorMessage.isNotEmpty()) Text(displayErrorMessage, color = Error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    val errorEmptyFields = stringResource(R.string.register_error_empty_fields)
                    val errorPasswordMismatch = stringResource(R.string.register_error_password_mismatch)
                    Button(
                        onClick = {
                            viewModel.clearError()
                            localErrorMessage = ""
                            if (fullName.isBlank() || username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() || bloodType.isBlank()) {
                                localErrorMessage = errorEmptyFields
                            } else if (password.length < 8) {
                                localErrorMessage = "Password minimal 8 karakter"
                            } else if (password != confirmPassword) {
                                localErrorMessage = errorPasswordMismatch
                            } else {
                                viewModel.register(
                                    RegisterRequest(
                                        fullName = fullName, 
                                        username = username, 
                                        email = email, 
                                        password = password, 
                                        passwordConfirmation = confirmPassword, 
                                        bloodType = bloodType, 
                                        allergy = allergy, 
                                        medicalHistory = medicalHistory
                                    ),
                                    onSuccess = {
                                        viewModel.clearError() // Clear any residual errors
                                        navController.navigate("login?reg_user=$username&reg_success=1") {
                                            popUpTo("register") { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }, 
                        modifier = Modifier.fillMaxWidth().height(52.dp), 
                        shape = RoundedCornerShape(28.dp), 
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald),
                        enabled = !isLoading
                    ) { 
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text(stringResource(R.string.login_register), color = Color.White, fontWeight = FontWeight.Bold) 
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.onboarding_have_account).substringBefore("Masuk"), color = Slate500)
                        Text(stringResource(R.string.login_button), color = Emerald, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { navController.navigate("login") })
                    }
                }
            }
        }
    }
}
