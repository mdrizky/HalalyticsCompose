import re

with open('app/src/main/java/com/example/halalyticscompose/ui/screens/LoginScreen.kt', 'r') as f:
    content = f.read()

# We want to replace from `    Box(` (around line 119) to the end of the `LoginScreen` function (around line 403), and then replace the `SocialLoginButton` function.
# Let's just use regular expressions to extract everything up to `    val googleSignInLauncher = ... }` and append the new UI.

match = re.search(r'(.*?    val googleSignInLauncher.*?^    \})', content, re.MULTILINE | re.DOTALL)
if match:
    prefix = match.group(1)
else:
    print("Failed to match prefix")
    exit(1)

new_ui = """
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        com.example.halalyticscompose.ui.theme.EmeraldLight,
                        com.example.halalyticscompose.ui.theme.TealLight,
                        com.example.halalyticscompose.ui.theme.BackgroundLight
                    )
                )
            )
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            
            // Modern Logo Header
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(androidx.compose.ui.graphics.Color.White)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.halalyticscompose.R.drawable.logo_halalytics),
                    contentDescription = "Halalytics Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Halalytics",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = com.example.halalyticscompose.ui.theme.Emerald
            )
            Text(
                text = "Intelligent Halal Healthcare",
                fontSize = 14.sp,
                color = com.example.halalyticscompose.ui.theme.Slate600,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Glassmorphism Login Card
            com.example.halalyticscompose.ui.components.Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Welcome Back",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = com.example.halalyticscompose.ui.theme.Slate900,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    com.example.halalyticscompose.ui.components.TextInputField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Email / Username",
                        placeholder = "Enter your email"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    com.example.halalyticscompose.ui.components.TextInputField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        placeholder = "Enter your password",
                        isPassword = true
                    )

                    if (!errorMessage.isNullOrEmpty()) {
                        Text(
                            text = errorMessage ?: "",
                            color = com.example.halalyticscompose.ui.theme.Error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Demo Login:", fontSize = 12.sp, color = com.example.halalyticscompose.ui.theme.Slate500)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            com.example.halalyticscompose.ui.components.SecondaryButton(
                                text = "Admin",
                                onClick = { username = "admin"; password = "admin123" },
                                modifier = Modifier.height(28.dp).width(60.dp)
                            )
                            com.example.halalyticscompose.ui.components.SecondaryButton(
                                text = "User",
                                onClick = { username = "daffa"; password = "12345678" },
                                modifier = Modifier.height(28.dp).width(60.dp)
                            )
                            com.example.halalyticscompose.ui.components.SecondaryButton(
                                text = "Pakar",
                                onClick = { username = "nutritionist"; password = "12345678" },
                                modifier = Modifier.height(28.dp).width(60.dp)
                            )
                        }
                    }

                    com.example.halalyticscompose.ui.components.PrimaryButton(
                        text = "Sign In",
                        onClick = {
                            if (username.isBlank() || password.isBlank()) return@PrimaryButton
                            viewModel.login(
                                com.example.halalyticscompose.data.model.LoginRequest(email = username, password = password),
                                onSuccess = { navigateAfterLogin() }
                            )
                        },
                        isLoading = isLoading,
                        fullWidth = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { navController.navigate("forgot_password") }) {
                            Text("Forgot Password?", color = com.example.halalyticscompose.ui.theme.Teal)
                        }
                        TextButton(onClick = { navController.navigate("register") }) {
                            Text("Register", color = com.example.halalyticscompose.ui.theme.Emerald)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Or continue with",
                fontSize = 12.sp,
                color = com.example.halalyticscompose.ui.theme.Slate500,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Google Button
                SocialLoginButton(
                    onClick = {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
                            .requestEmail()
                            .build()
                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    },
                    iconRes = R.drawable.ic_google,
                    text = "Google",
                    modifier = Modifier.weight(1f),
                    isLoading = isLoading
                )

                // Facebook Button
                SocialLoginButton(
                    onClick = {
                        val activity = context as? FragmentActivity
                        if (activity != null) {
                            try {
                                LoginManager.getInstance().logInWithReadPermissions(
                                    activity,
                                    listOf("email", "public_profile")
                                )
                            } catch (e: Exception) {
                                Log.e("LoginScreen", "Facebook login failed: ${e.message}")
                            }
                        }
                    },
                    icon = Icons.Default.Facebook,
                    text = "Facebook",
                    modifier = Modifier.weight(1f),
                    isLoading = isLoading
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SocialLoginButton(
    onClick: () -> Unit,
    iconRes: Int? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    text: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = androidx.compose.ui.graphics.Color.White,
            contentColor = com.example.halalyticscompose.ui.theme.Slate700
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        enabled = !isLoading
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (iconRes != null) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = com.example.halalyticscompose.ui.theme.Emerald
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}
"""

with open('app/src/main/java/com/example/halalyticscompose/ui/screens/LoginScreen.kt', 'w') as f:
    f.write(prefix + "\n" + new_ui)

print("Rewrite successful")
