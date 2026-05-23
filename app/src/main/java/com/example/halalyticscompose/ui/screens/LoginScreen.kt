package com.example.halalyticscompose.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import com.example.halalyticscompose.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.example.halalyticscompose.ui.viewmodel.AuthViewModel
import com.example.halalyticscompose.data.model.LoginRequest
import com.example.halalyticscompose.data.model.LoginResponse
import com.example.halalyticscompose.BuildConfig
import com.example.halalyticscompose.ui.LocalFacebookCallbackManager
import com.example.halalyticscompose.utils.RoleHelper
import com.example.halalyticscompose.utils.SessionManager

@Composable
fun LoginScreen(
    navController: NavController,
    prefillUsername: String = "",
    showRegisterSuccess: Boolean = false,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf(prefillUsername) }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val navigateAfterLogin: (LoginResponse) -> Unit = { response ->
        val sm = SessionManager.getInstance(context)
        val dest = RoleHelper.homeRoute(sm.getRole())
        navController.navigate(dest) { popUpTo("login") { inclusive = true } }
    }

    val facebookCallbackManager = LocalFacebookCallbackManager.current
    DisposableEffect(facebookCallbackManager) {
        val fbCallback = object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) { viewModel.loginWithFacebook(result.accessToken.token) { response -> navigateAfterLogin(response) } }
            override fun onCancel() { Log.d("LoginScreen", "Facebook cancelled") }
            override fun onError(error: FacebookException) { Log.e("LoginScreen", "Facebook error", error) }
        }
        LoginManager.getInstance().registerCallback(facebookCallbackManager, fbCallback)
        onDispose { LoginManager.getInstance().unregisterCallback(facebookCallbackManager) }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                account?.idToken?.let { viewModel.loginWithGoogle(it) { response -> navigateAfterLogin(response) } }
            } catch (e: Exception) { Log.e("LoginScreen", "Google sign in failed", e) }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(EmeraldLight, TealLight, Color.White)))) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(60.dp))
            androidx.compose.foundation.Image(painter = painterResource(R.drawable.logo_halalytics), contentDescription = "Logo", modifier = Modifier.size(100.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Halalytics", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Emerald)
            Text("Scan · Sehat · Terpercaya", fontSize = 14.sp, color = Slate600)
            Spacer(modifier = Modifier.height(32.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)), elevation = CardDefaults.cardElevation(16.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Selamat Datang", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Email / Username") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Emerald))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) { Icon(if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null) } })
                    if (!errorMessage.isNullOrEmpty()) Text(errorMessage!!, color = Error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { viewModel.login(LoginRequest(email = username, password = password), onSuccess = { response -> navigateAfterLogin(response) }) }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = Emerald)) { Text("Masuk", color = Color.White, fontWeight = FontWeight.Bold) }
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { navController.navigate("forgot_password") }) { Text("Lupa password?", color = Teal) }
                        TextButton(onClick = { navController.navigate("register") }) { Text("Buat akun", color = Emerald) }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Atau masuk dengan", fontSize = 12.sp, color = Slate500)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SocialLoginButton(iconRes = R.drawable.ic_google, text = "Google", onClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(BuildConfig.GOOGLE_CLIENT_ID).requestEmail().build()
                    val client = GoogleSignIn.getClient(context, gso)
                    googleSignInLauncher.launch(client.signInIntent)
                })
                SocialLoginButton(icon = Icons.Default.Facebook, text = "Facebook", onClick = {
                    (context as? FragmentActivity)?.let { LoginManager.getInstance().logInWithReadPermissions(it, listOf("email", "public_profile")) }
                })
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SocialLoginButton(iconRes: Int? = null, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.height(48.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White), elevation = ButtonDefaults.buttonElevation(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (iconRes != null) androidx.compose.foundation.Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(20.dp))
            else if (icon != null) Icon(icon, null, tint = Emerald, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Medium)
        }
    }
}