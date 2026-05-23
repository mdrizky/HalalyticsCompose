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
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var bloodType by remember { mutableStateOf("") }
    var allergy by remember { mutableStateOf("") }
    var medicalHistory by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(EmeraldLight, Color.White)))) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
            IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Spacer(modifier = Modifier.height(20.dp))
            Card(shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(16.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Buat Akun", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), leadingIcon = { Icon(Icons.Default.Person, null) })
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) { Icon(if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null) } }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Konfirmasi Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    var bloodTypeExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = bloodTypeExpanded, onExpandedChange = { bloodTypeExpanded = it }) {
                        OutlinedTextField(
                            value = bloodType, 
                            onValueChange = {}, 
                            readOnly = true, 
                            label = { Text("Golongan Darah") }, 
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, 
                            modifier = Modifier.fillMaxWidth().menuAnchor(), 
                            shape = RoundedCornerShape(16.dp)
                        )
                        DropdownMenu(expanded = bloodTypeExpanded, onDismissRequest = { bloodTypeExpanded = false }) {
                            listOf("A", "B", "AB", "O").forEach { DropdownMenuItem(text = { Text(it) }, onClick = { bloodType = it; bloodTypeExpanded = false }) }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = allergy, onValueChange = { allergy = it }, label = { Text("Alergi") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = medicalHistory, onValueChange = { medicalHistory = it }, label = { Text("Riwayat Penyakit") }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(16.dp))
                    
                    if (errorMessage.isNotEmpty()) Text(errorMessage, color = Error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            if (fullName.isBlank() || username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() || bloodType.isBlank()) {
                                errorMessage = "Semua field harus diisi"
                            } else if (password != confirmPassword) {
                                errorMessage = "Password tidak cocok"
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
                        else Text("Daftar", color = Color.White, fontWeight = FontWeight.Bold) 
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        Text("Sudah punya akun? ", color = Slate500)
                        Text("Masuk", color = Emerald, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { navController.navigate("login") })
                    }
                }
            }
        }
    }
}
