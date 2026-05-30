@file:Suppress("DEPRECATION")

package com.example.halalyticscompose

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.halalyticscompose.ui.screens.*
import com.example.halalyticscompose.ui.components.MainLayout
import com.example.halalyticscompose.ui.theme.*
import com.example.halalyticscompose.healthcare.screens.*
import com.example.halalyticscompose.healthcare.viewmodel.HealthScannerViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.halalyticscompose.ui.viewmodel.MainViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.halalyticscompose.ui.viewmodel.NotificationViewModel

import com.example.halalyticscompose.utils.LanguageManager
import com.example.halalyticscompose.utils.BiometricAuthHelper
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import android.content.Intent
import androidx.compose.runtime.CompositionLocalProvider
import com.example.halalyticscompose.ui.LocalFacebookCallbackManager
import com.facebook.CallbackManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint

@Composable
fun HalalyticsComposeTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
private fun MedicalRouteGuard(
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    if (!enabled) {
        content()
        return
    }

    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var isUnlocked by rememberSaveable { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    if (isUnlocked) {
        content()
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Akses Medis Terkunci",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Gunakan biometrik untuk membuka halaman medis sensitif.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                authError?.let {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val act = activity
                        if (act == null) {
                            authError = "Biometrik tidak tersedia di perangkat ini."
                            return@Button
                        }
                        if (!BiometricAuthHelper.canAuthenticate(act)) {
                            authError = "Biometrik belum aktif. Buka pengaturan perangkat untuk mengaktifkan."
                            return@Button
                        }
                        BiometricAuthHelper.authenticate(
                            activity = act,
                            executor = ContextCompat.getMainExecutor(act),
                            onSuccess = {
                                authError = null
                                isUnlocked = true
                            },
                            onError = { message ->
                                authError = message
                            }
                        )
                    }
                ) {
                    Text("Buka dengan Biometrik")
                }
            }
        }
    }
}

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val facebookCallbackManager: CallbackManager = CallbackManager.Factory.create()

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        Log.d("HALALYTICS_FLOW", "MainActivity: onCreate Started")
        Log.d("HALALYTICS_DEBUG", "MainActivity Started")

        // Global crash handling via CrashReporter
        com.example.halalyticscompose.utils.CrashReporter.install(this)
        
        // Show previous crash log if exists
        val lastCrash = com.example.halalyticscompose.utils.CrashReporter.getLastCrash(this)
        if (lastCrash != null) {
            Log.e("HALALYTICS_CRASH", "PREVIOUS CRASH DETECTED:\n$lastCrash")
            com.example.halalyticscompose.utils.CrashReporter.clearLastCrash(this)
        }

        setContent {
            Log.d("HALALYTICS_FLOW", "MainActivity: setContent block started")
            val mainViewModel: MainViewModel = hiltViewModel()
            val authViewModel: com.example.halalyticscompose.ui.viewmodel.AuthViewModel = hiltViewModel()
            
            // Keep system splash screen until we have the first frame of Compose and initial session state
            var isReady by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                Log.d("HALALYTICS_FLOW", "MainActivity: Initializing isReady delay")
                // Short delay to allow Compose to settle
                kotlinx.coroutines.delay(200)
                isReady = true
                Log.d("HALALYTICS_FLOW", "MainActivity: isReady = true")
            }
            splashScreen.setKeepOnScreenCondition { !isReady }
            val historyViewModel: com.example.halalyticscompose.ui.viewmodel.HistoryViewModel = hiltViewModel()
            val healthViewModel: com.example.halalyticscompose.ui.viewmodel.HealthViewModel = hiltViewModel()
            
            // Database and DAO are now injected via Hilt
            val isDarkMode by mainViewModel.isDarkMode.collectAsState()
            val appLanguage by mainViewModel.appLanguage.collectAsState()
            val privacyModeEnabled by mainViewModel.privacyModeEnabled.collectAsState()
            val biometricLockEnabled by mainViewModel.biometricLockEnabled.collectAsState()
            val autoLogoutEnabled by mainViewModel.autoLogoutEnabled.collectAsState()
            val autoLogoutMinutes by mainViewModel.autoLogoutMinutes.collectAsState()
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
            val isAdmin by authViewModel.isAdmin.collectAsState()
            val isNutritionist by authViewModel.isNutritionist.collectAsState()

            Log.d("HALALYTICS_FLOW", "MainActivity: Rendering Theme. isLoggedIn=$isLoggedIn, isAdmin=$isAdmin")

            HalalyticsComposeTheme(darkTheme = isDarkMode) {
                CompositionLocalProvider(LocalFacebookCallbackManager provides facebookCallbackManager) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) {
                    val navController = rememberNavController()
                    
                    // Handle Notification Navigation (e.g. from Blood Emergency Alerts)
        var intentNavigated by remember { mutableStateOf(false) }
        LaunchedEffect(intent) {
            val navigateTo = intent.getStringExtra("navigate_to")
            if (!navigateTo.isNullOrEmpty() && !intentNavigated) {
                Log.d("HALALYTICS_FLOW", "MainActivity: Intent navigation to $navigateTo")
                // Wait for NavHost to be ready
                kotlinx.coroutines.delay(1000)
                try {
                    navController.navigate(navigateTo)
                    intentNavigated = true
                } catch (e: Exception) {
                    Log.e("HALALYTICS_FLOW", "MainActivity: Intent navigation failed", e)
                }
            }
        }
                    
                    val context = LocalContext.current
                    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

                LaunchedEffect(appLanguage) {
                    Log.d("HALALYTICS_FLOW", "MainActivity: Language changed to $appLanguage")
                    LanguageManager.applyLanguageIfNeeded(this@MainActivity, appLanguage)
                }

                LaunchedEffect(privacyModeEnabled) {
                    val shouldUseSecureWindow = privacyModeEnabled && !BuildConfig.DEBUG
                    if (shouldUseSecureWindow) {
                        window.setFlags(
                            WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE
                        )
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }
                
                // Initialize SessionManager
                val sessionManager = remember {
                    com.example.halalyticscompose.utils.SessionManager.getInstance(context)
                }

                // Sync FCM Token
                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn) {
                        try {
                            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    task.result?.let { mainViewModel.updateFcmToken(it) }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Firebase not initialized")
                        }
                    }
                }

                DisposableEffect(lifecycleOwner, autoLogoutEnabled, autoLogoutMinutes, isLoggedIn) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (!autoLogoutEnabled) return@LifecycleEventObserver
                        when (event) {
                            Lifecycle.Event.ON_STOP -> {
                                if (isLoggedIn) {
                                    sessionManager.setLastBackgroundTimestamp(System.currentTimeMillis())
                                }
                            }
                            Lifecycle.Event.ON_START -> {
                                if (isLoggedIn) {
                                    val lastTs = sessionManager.getLastBackgroundTimestamp()
                                    if (lastTs > 0L) {
                                        val timeoutMs = autoLogoutMinutes * 60_000L
                                        val idleMs = System.currentTimeMillis() - lastTs
                                        if (idleMs >= timeoutMs) {
                                            authViewModel.logout {
                                                navController.navigate("login") {
                                                    popUpTo(0) { inclusive = true }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else -> Unit
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }
                
                // Health Scanner Feature ViewModel
                val healthScannerViewModel: HealthScannerViewModel = hiltViewModel()
                
                // Notifications
                val notificationViewModel: NotificationViewModel = hiltViewModel()



                // Comparison Feature
                val compareViewModel: com.example.halalyticscompose.ui.viewmodel.CompareViewModel = hiltViewModel()

                // Donor Feature
                val donorViewModel: com.example.halalyticscompose.ui.viewmodel.DonorViewModel = hiltViewModel()

                // Initialize PreferenceManager
                val preferenceManager = remember {
                    com.example.halalyticscompose.utils.PreferenceManager(context)
                }
                
                // Initialize ViewModel with SessionManager
                LaunchedEffect(Unit) {
                    // MainViewModel dependencies are now injected by Hilt
                    

                    
                     val token = sessionManager.getAuthToken() ?: ""
                     val userId = sessionManager.getUserId()
                     // Start notification listener on app start if logged in
                     if (token.isNotEmpty()) {
                         notificationViewModel.loadNotifications(token, userId)
                         // Sync with MySQL if Firebase user is present
                         try { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser } catch (e: Throwable) { null }?.let { firebaseUser ->
                             mainViewModel.syncWithMySQL(firebaseUser, token)
                         }
                     }
                }
                
                // Always show splash first for consistent session + role routing
                val startDestination = remember { "splash" }
                
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.background(MaterialTheme.colorScheme.background),
                    // ═══ GLOBAL TRANSITION ANIMATIONS ═══
                    // Subtle slide-up + fade for all screen transitions
                    enterTransition = {
                        slideInVertically(
                            initialOffsetY = { 200 },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(200))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(300))
                    },
                    popExitTransition = {
                        slideOutVertically(
                            targetOffsetY = { 200 },
                            animationSpec = tween(300)
                        ) + fadeOut(animationSpec = tween(200))
                    }
                ) {
                    // Splash Screen
                    composable("splash") {
                        SplashScreen(
                            onNavigateToOnboarding = {
                                Log.d("HALALYTICS_FLOW", "Navigate Onboarding")
                                try {
                                    navController.navigate("onboarding") { popUpTo("splash") { inclusive = true } }
                                } catch (e: Exception) {
                                    Log.e("HALALYTICS_FLOW", "Navigation to Onboarding failed", e)
                                }
                            },
                            onNavigateToLogin = {
                                Log.d("HALALYTICS_FLOW", "Navigate Login")
                                try {
                                    navController.navigate("login") { popUpTo("splash") { inclusive = true } }
                                } catch (e: Exception) {
                                    Log.e("HALALYTICS_FLOW", "Navigation to Login failed", e)
                                }
                            },
                            onNavigateToUserHome = {
                                Log.d("HALALYTICS_FLOW", "Navigate Home")
                                try {
                                    navController.navigate("home") { popUpTo("splash") { inclusive = true } }
                                } catch (e: Exception) {
                                    Log.e("HALALYTICS_FLOW", "Navigation to Home failed", e)
                                }
                            },
                            onNavigateToAdminDashboard = {
                                Log.d("HALALYTICS_FLOW", "Navigate Admin")
                                try {
                                    navController.navigate("home") { popUpTo("splash") { inclusive = true } }
                                } catch (e: Exception) {
                                    Log.e("HALALYTICS_FLOW", "Navigation to Admin failed", e)
                                }
                            },
                            onNavigateToNutritionistHome = {
                                Log.d("HALALYTICS_FLOW", "Navigate Nutritionist")
                                try {
                                    navController.navigate("nutritionist_home") { popUpTo("splash") { inclusive = true } }
                                } catch (e: Exception) {
                                    Log.e("HALALYTICS_FLOW", "Navigation to Nutritionist failed", e)
                                }
                            }
                        )
                    }

                    // Login — jangan pernah sertakan password di deep link / savedState.
                    composable(
                        "login?reg_user={reg_user}&reg_success={reg_success}",
                        arguments = listOf(
                            androidx.navigation.navArgument("reg_user") {
                                type = androidx.navigation.NavType.StringType
                                defaultValue = ""
                            },
                            androidx.navigation.navArgument("reg_success") {
                                type = androidx.navigation.NavType.StringType
                                defaultValue = ""
                            }
                        )
                    ) { backStackEntry ->
                        val regUser = backStackEntry.arguments?.getString("reg_user") ?: ""
                        val regSuccess = backStackEntry.arguments?.getString("reg_success") == "1"
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            LoginScreen(
                                navController = navController,
                                prefillUsername = regUser,
                                showRegisterSuccess = regSuccess
                            )
                        }
                    }
                    
                    // Register Screen
                    composable("register") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            SimpleRegisterScreen(
                                navController = navController
                            )
                        }
                    }

                    composable("basic_profile") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            BasicProfileScreen(
                                navController = navController
                            )
                        }
                    }
                    
                    // Home Screen
                    composable("home") {
                        MainLayout(
                            navController = navController,
                            showBottomNav = !isNutritionist,
                            isAdmin = isAdmin,
                            isNutritionist = isNutritionist
                        ) { paddingValues ->
                            when {
                                isAdmin -> AdminPanelScreen(navController = navController)
                                isNutritionist -> NutritionistMainScreen(navController = navController)
                                else -> HomeScreen(
                                    navController = navController,
                                    paddingValues = paddingValues
                                )
                            }
                        }
                    }

                    composable("nutritionist_home") {
                        if (isNutritionist) {
                            NutritionistMainScreen(navController = navController)
                        } else {
                            LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                        }
                    }

                    composable("patient_detail/{userId}") { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId") ?: ""
                        PatientDetailScreen(userId = userId, navController = navController)
                    }

                    composable("consultations") {
                        ConsultationScreen(navController = navController)
                    }


                    composable("cosmetic_detail") {
                        MainLayout(navController = navController, isAdmin = isAdmin) {
                            CosmeticDetailScreen(navController = navController)
                        }
                    }

                    composable("cosmetic_detail/{productId}") { backStackEntry ->
                        val productId = backStackEntry.arguments?.getString("productId")
                        MainLayout(navController = navController, isAdmin = isAdmin) {
                            CosmeticDetailScreen(
                                navController = navController,
                                productId = productId
                            )
                        }
                    }

                    composable("health_articles") {
                        MainLayout(navController = navController, isAdmin = isAdmin) {
                            HealthArticleListScreen(navController = navController)
                        }
                    }

                    composable("health_article_detail/{articleId}") { backStackEntry ->
                        val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
                        MainLayout(navController = navController, isAdmin = isAdmin) {
                            HealthArticleDetailScreen(
                                navController = navController,
                                articleId = articleId
                            )
                        }
                    }

                    // Scan Screen
                    composable("scan") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            ScanScreen(
                                navController = navController
                            )
                        }
                    }
                    
                    // Scan Hub Screen
                    composable("scan_hub") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            ScanHubScreen(navController = navController)
                        }
                    }

                    composable("voice_logging") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            VoiceLoggingScreen(navController = navController)
                        }
                    }

                    // Water Tracker Screen
                    composable("water_tracker") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            WaterTrackerScreen(navController = navController)
                        }
                    }

                    // Calorie Counter Screen
                    composable("calorie_counter") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            CalorieCounterScreen(navController = navController)
                        }
                    }

                    // Grocery List (Under Development)
                    composable("grocery_list") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            GroceryListScreen(navController = navController)
                        }
                    }

                    // Wearable Integration (Under Development)
                    composable("wearable_integration") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            WearableIntegrationScreen(navController = navController)
                        }
                    }

                    // Sleep Tracker (Under Development)
                    composable("sleep_tracker") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            SleepTrackerScreen(navController = navController)
                        }
                    }

                    // Settings Screen
                    composable("settings") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            SettingsScreen(navController = navController, viewModel = mainViewModel)
                        }
                    }

                    // Profile Screen (BottomNav route)
                    composable("profile") {
                        MainLayout(navController = navController, showBottomNav = true, isAdmin = isAdmin) { paddingValues ->
                            ProfileScreen(
                                navController = navController
                            )
                        }
                    }

                    composable("profile_status") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            ProfileStatusScreen(
                                navController = navController
                            )
                        }
                    }
                    
                    // Account Management Screen
                    composable("account_management") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            AccountManagementScreen(navController = navController)
                        }
                    }
                    
                    // Privacy Policy Screen
                    composable("privacy_policy") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            PrivacyPolicyScreen(navController = navController)
                        }
                    }

                    // Enhanced OCR Screen
                    composable("enhanced_ocr") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            EnhancedOCRScreen(
                                navController = navController
                            )
                        }
                    }
                    

                    
                    // AI Analysis Screen
                    composable(
                        "ai_analysis?ingredients={ingredients}",
                        arguments = listOf(
                            navArgument("ingredients") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { backStackEntry ->
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            AiAnalysisScreen(
                                navController = navController
                            )
                        }
                    }

                    composable("ai_report") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            AiReportScreen(navController = navController)
                        }
                    }
                    
                    // All Features App Menu
                    composable("all_features") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            AllFeaturesScreen(navController = navController)
                        }
                    }

                    composable("diet_tips") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            DietTipsScreen(navController = navController)
                        }
                    }

                    composable("sugar_warning") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            SugarWarningScreen(navController = navController)
                        }
                    }

                    composable("risk_checker") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            RiskCheckerScreen(navController = navController)
                        }
                    }

                    composable(
                        "product_request/{barcode}",
                        arguments = listOf(navArgument("barcode") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val barcode = backStackEntry.arguments?.getString("barcode") ?: ""
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            ProductRequestScreen(
                                navController = navController,
                                barcode = barcode
                            )
                        }
                    }

                    composable("community_hub") {
                        MainLayout(navController = navController, showBottomNav = true, isAdmin = isAdmin) { paddingValues ->
                            CommunityHubScreen(navController = navController)
                        }
                    }

                    composable("community") {
                        MainLayout(navController = navController, isAdmin = isAdmin) {
                            com.example.halalyticscompose.feature.expansion.ui.CommunityScreen(
                                navController = navController
                            )
                        }
                    }

                    composable(
                        route = "community_post/{postId}",
                        arguments = listOf(navArgument("postId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val postId = backStackEntry.arguments?.getInt("postId") ?: 0
                        MainLayout(navController = navController, isAdmin = isAdmin) {
                            com.example.halalyticscompose.feature.expansion.ui.CommunityPostDetailScreen(
                                postId = postId,
                                navController = navController
                            )
                        }
                    }
                    
                    // Manual Input Screen
                    composable(
                        "manual_input?category={category}",
                        arguments = listOf(
                            navArgument("category") {
                                type = NavType.StringType
                                defaultValue = ""
                            }
                        )
                    ) { backStackEntry ->
                        val category = backStackEntry.arguments?.getString("category") ?: ""
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            ManualInputScreen(
                                navController = navController
                            )
                        }
                    }
                    
                    // History Screen (New Realtime)
                    composable("history") {
                        MainLayout(navController = navController, showBottomNav = true, isAdmin = isAdmin) { paddingValues ->
                            ScanHistoryScreen(
                                navController = navController,
                                paddingValues = paddingValues
                            )
                        }
                    }



                    // Scan history detail
                    composable("scan_history_detail/{id}") { backStackEntry ->
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            val historyId = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
                            ScanHistoryDetailScreen(
                                navController = navController,
                                historyId = historyId
                            )
                        }
                    }

                    // Notification Screen (New)
                    composable("notifications") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            NotificationScreen(
                                navController = navController,
                                viewModel = notificationViewModel
                            )
                        }
                    }

                    composable("admin_notifications_app") {
                        if (isAdmin) {
                            MainLayout(navController = navController, showBottomNav = true, isAdmin = isAdmin) { paddingValues ->
                                AdminNotificationScreen(
                                    navController = navController
                                )
                            }
                        } else {
                            LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                        }
                    }

                    // Favorites Screen (New)
                    composable("favorites") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            FavoritesScreen(
                                navController = navController
                            )
                        }
                    }
                    
                    // Edit Profile Screen
                    composable("edit_profile") {
                        EditProfileScreen(navController = navController)
                    }

                    // Family Box Screen
                    composable("family_box") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            FamilyBoxScreen(
                                navController = navController
                            )
                        }
                    }
                    
                                        
                    // Product Detail Screen
                    composable("product_detail/{barcode}") { backStackEntry ->
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            val barcode = backStackEntry.arguments?.getString("barcode")
                            ProductDetailScreen(
                                navController = navController,
                                barcode = barcode ?: ""
                            )
                        }
                    }
                    
                    // Search External Screen
                    composable(
                        "search_external?q={q}",
                        arguments = listOf(
                            navArgument("q") {
                                type = NavType.StringType
                                defaultValue = ""
                            }
                        )
                    ) { backStackEntry ->
                        val query = backStackEntry.arguments?.getString("q") ?: ""
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            SearchExternalScreen(
                                navController = navController,
                                initialQuery = query
                            )
                        }
                    }
                    
                    // Product External Detail Screen
                    composable("product_external_detail/{barcode}") { backStackEntry ->
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            val barcode = backStackEntry.arguments?.getString("barcode")
                            ProductExternalDetailScreen(
                                navController = navController,
                                barcode = barcode ?: ""
                            )
                        }
                    }
                    
                    // Ingredient Detail Screen
                    composable("ingredient_detail/{ingredientId}") { backStackEntry ->
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            val ingredientId = backStackEntry.arguments?.getString("ingredientId")?.toIntOrNull() ?: 0
                            IngredientDetailScreen(
                                navController = navController,
                                ingredientId = ingredientId
                            )
                        }
                    }
                    
                    // Forgot Password Screen
                    composable("forgot_password") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            ForgotPasswordScreen(
                                navController = navController
                            )
                        }
                    }

                    // Health Scanner Feature Routes
                    composable(
                        "health_assistant?symptom={symptom}",
                        arguments = listOf(
                            navArgument("symptom") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { backStackEntry ->
                        val symptom = backStackEntry.arguments?.getString("symptom")
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            MedicalRouteGuard(enabled = biometricLockEnabled) {
                                HealthAssistantScreen(
                                    navController = navController,
                                    initialSymptom = symptom
                                )
                            }
                        }
                    }
                    composable("medicine_reminders") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            MedicalRouteGuard(enabled = biometricLockEnabled) {
                                MedicineRemindersScreen(
                                    navController = navController
                                )
                            }
                        }
                    }
                    
                    composable("health_scanner") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            HealthScannerScreen(
                                navController = navController,
                                viewModel = healthScannerViewModel
                            )
                        }
                    }
                    
                    composable("health_analysis") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            AnalysisResultScreen(
                                navController = navController,
                                viewModel = healthScannerViewModel
                            )
                        }
                    }
                    
                    // Food Scan & Recognition Screen
                    // Food Scan & Recognition Screen (AI Meal Scanner) - PHASE 6 UPGRADE
                    composable("food_scan") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            FoodCameraScreen(
                                navController = navController
                            )
                        }
                    }
                    
                    // Food Analysis Result Screen - PHASE 6 UPGRADE
                    composable(
                        route = "food_result/{imagePath}",
                        arguments = listOf(androidx.navigation.navArgument("imagePath") { type = androidx.navigation.NavType.StringType })
                    ) { backStackEntry ->
                        val imagePath = backStackEntry.arguments?.getString("imagePath") ?: ""
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            FoodAnalysisResultScreen(
                                navController = navController,
                                imagePath = imagePath,
                                viewModel = mainViewModel
                            )
                        }
                    }



                    // ⚠️ ADDED: AI Weekly Report Screen
                    composable("weekly_report") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            AiReportScreen(
                                navController = navController
                            )
                        }
                    }

                    // ⚠️ ADDED: Encyclopedia Screen
                    composable("encyclopedia") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            EncyclopediaScreen(
                                navController = navController,
                                paddingValues = paddingValues
                            )
                        }
                    }

                    // ⚠️ ADDED: Encyclopedia Detail Screen
                    composable("encyclopedia_detail/{id}/{title}") { backStackEntry ->
                        val itemId = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
                        val viewModel: com.example.halalyticscompose.ui.viewmodel.HealthEncyclopediaViewModel = hiltViewModel()
                        val selectedItem by viewModel.selectedItem.collectAsState()
                        val isLoading by viewModel.isLoading.collectAsState()

                        LaunchedEffect(itemId) {
                            viewModel.fetchById(itemId)
                        }

                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        } else if (selectedItem != null) {
                            EncyclopediaDetailScreen(
                                navController = navController,
                                item = selectedItem!!
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Item tidak ditemukan", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }



                    // Contribution Screen
                    composable(
                        route = "contribution?barcode={barcode}&name={name}",
                        arguments = listOf(
                            androidx.navigation.navArgument("barcode") {
                                type = androidx.navigation.NavType.StringType
                                nullable = true
                                defaultValue = ""
                            },
                            androidx.navigation.navArgument("name") {
                                type = androidx.navigation.NavType.StringType
                                nullable = true
                                defaultValue = ""
                            }
                        )
                    ) { backStackEntry ->
                        val initialBarcode = backStackEntry.arguments?.getString("barcode")
                        val initialName = backStackEntry.arguments?.getString("name")
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            ContributionScreen(
                                navController = navController,
                                initialBarcode = initialBarcode,
                                initialProductName = initialName
                            )
                        }
                    }

                    // Emergency QR Screen
                    composable("emergency_qr") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            MedicalRouteGuard(enabled = biometricLockEnabled) {
                                EmergencyQRScreen(
                                    navController = navController
                                )
                            }
                        }
                    }

                    // Health Profile Screen
                    composable("health_profile") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            HealthProfileScreen(
                                navController = navController
                            )
                        }
                    }

                    // Body Monitor Screen
                    composable("health_monitor") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            MedicalRouteGuard(enabled = biometricLockEnabled) {
                                HealthMonitorScreen(
                                    navController = navController
                                )
                            }
                        }
                    }

                    composable("health_diary") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            HealthDiaryScreen(navController = navController)
                        }
                    }

                    composable("medical_resume") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            MedicalRouteGuard(enabled = biometricLockEnabled) {
                                MedicalResumeScreen(navController = navController)
                            }
                        }
                    }

                    composable("health_pass") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            MedicalRouteGuard(enabled = biometricLockEnabled) {
                                HealthPassScreen(navController = navController)
                            }
                        }
                    }

                    // International Medicine Search Screen
                    composable("international_medicine") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            InternationalMedicineScreen(
                                navController = navController
                            )
                        }
                    }

                    // Medicine Detail Screen
                    composable("medicine_detail/{medicineId}") { backStackEntry ->
                        val medicineId = backStackEntry.arguments?.getString("medicineId")?.toIntOrNull() ?: 0
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            MedicineDetailScreen(
                                navController = navController,
                                medicineId = medicineId
                            )
                        }
                    }

                    // ==================== ADVANCED AI HEALTH SUITE ====================
                    composable("health_suite_hub") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            HealthSuiteHubScreen(navController = navController)
                        }
                    }

                    composable("drug_interaction") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            MedicalRouteGuard(enabled = biometricLockEnabled) {
                                DrugInteractionScreen(navController = navController)
                            }
                        }
                    }

                    composable("pill_scanner") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            PillScannerScreen(navController = navController)
                        }
                    }



                    composable("health_journey") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            MedicalRouteGuard(enabled = biometricLockEnabled) {
                                HealthJourneyScreen(navController = navController)
                            }
                        }
                    }

                    composable("nutrition_scanner") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            MedicalRouteGuard(enabled = biometricLockEnabled) {
                                NutritionScannerScreen(navController = navController)
                            }
                        }
                    }

                    composable("meal_scan") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            MealScanScreen(
                                navController = navController
                            )
                        }
                    }

                    composable(
                        route = "food_analysis/{foodId}",
                        arguments = listOf(
                            androidx.navigation.navArgument("foodId") {
                                type = androidx.navigation.NavType.IntType
                            }
                        )
                    ) { backStackEntry ->
                        val foodId = backStackEntry.arguments?.getInt("foodId") ?: 0
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            FoodAnalysisScreen(
                                navController = navController,
                                foodId = foodId
                            )
                        }
                    }

                    composable("medical_records") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            MedicalRouteGuard(enabled = biometricLockEnabled) {
                                MedicalRecordsScreen(navController = navController)
                            }
                        }
                    }

                    composable("watchlist_editor") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            WatchlistEditorScreen(navController = navController)
                        }
                    }

                    composable("emergency_p3k") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            EmergencyP3KScreen(navController = navController)
                        }
                    }

                    composable("halal_specialist") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            MedicalRouteGuard(enabled = biometricLockEnabled) {
                                HalalSpecialistScreen(navController = navController)
                            }
                        }
                    }

                    composable("bpom_scanner") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            BpomScannerScreen(
                                navController = navController
                            )
                        }
                    }

                    composable("skincare_scanner") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            SkincareScannerScreen(
                                navController = navController,
                                mainViewModel = mainViewModel
                            )
                        }
                    }

                    // Product Request Screen (Crowdsourcing)
                    composable("product_request/{barcode}") { backStackEntry ->
                        val barcode = backStackEntry.arguments?.getString("barcode") ?: ""
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            ProductRequestScreen(
                                navController = navController,
                                barcode = barcode
                            )
                        }
                    }

                    composable("compare_products") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            CompareScreen(
                                navController,
                                mainViewModel,
                                compareViewModel
                            )
                        }
                    }

                    composable("admin_panel_app") {
                        if (isAdmin) {
                            MainLayout(navController = navController, showBottomNav = true, isAdmin = isAdmin) { paddingValues ->
                                AdminPanelScreen(
                                    navController = navController
                                )
                            }
                        } else {
                            LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                        }
                    }

                    composable("admin_users") {
                        if (isAdmin) {
                            MainLayout(navController = navController, showBottomNav = true, isAdmin = isAdmin) { paddingValues ->
                                AdminUsersListScreen(
                                    navController = navController
                                )
                            }
                        } else {
                            LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                        }
                    }
                    composable("admin_system_health") {
                        if (isAdmin) {
                            MainLayout(navController = navController, showBottomNav = true, isAdmin = isAdmin) { paddingValues ->
                                AdminSystemHealthScreen(
                                    navController = navController
                                )
                            }
                        } else {
                            LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                        }
                    }

                    composable("comparison_result") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            ComparisonResultScreen(
                                navController = navController,
                                viewModel = compareViewModel
                            )
                        }
                    }

                    composable("report_issue/{productId}/{productName}") { backStackEntry ->
                        val productId = backStackEntry.arguments?.getString("productId")?.toIntOrNull() ?: 0
                        val productName = backStackEntry.arguments?.getString("productName") ?: ""
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            ReportIssueScreen(
                                navController = navController,
                                productId = productId,
                                productName = productName
                            )
                        }
                        }

                    // ==========================================================
                    // 🤖 AI EXPANSION FEATURES (4-7)
                    // ==========================================================

                    composable("ocr_scan") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            EnhancedOCRScreen(
                                navController = navController
                            )
                        }
                    }

                    composable("nutrition_dashboard") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            NutritionScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }




                    composable("recipes") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            RecipeListScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onRecipeClick = { recipeId -> navController.navigate("recipe_detail/$recipeId") }
                            )
                        }
                    }

                    composable(
                        route = "recipe_detail/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getInt("id") ?: 0
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            RecipeDetailScreen(
                                recipeId = id,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }

                    // ═══════════════════════════════════════
                    // 🆕 EXPANSION SCREENS
                    // ═══════════════════════════════════════

                    composable("onboarding") {
                        OnboardingScreen(
                            navController = navController,
                            onFinish = {
                                sessionManager.setOnboardingCompleted()
                                navController.navigate("login") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("donations") {
                        MainLayout(navController = navController, showBottomNav = true, isAdmin = isAdmin, isNutritionist = isNutritionist) {
                            com.example.halalyticscompose.ui.screens.donation.DonationScreen(navController = navController)
                        }
                    }
                    composable(
                        "donation_detail/{campaignId}",
                        arguments = listOf(navArgument("campaignId") { type = NavType.LongType })
                    ) { entry ->
                        val campaignId = entry.arguments?.getLong("campaignId") ?: 0L
                        com.example.halalyticscompose.ui.screens.donation.DonationDetailScreen(
                            campaignId = campaignId,
                            navController = navController
                        )
                    }
                    composable(
                        "donation_form/{campaignId}",
                        arguments = listOf(navArgument("campaignId") { type = NavType.LongType })
                    ) { entry ->
                        val campaignId = entry.arguments?.getLong("campaignId") ?: 0L
                        com.example.halalyticscompose.ui.screens.donation.DonationFormScreen(
                            campaignId = campaignId,
                            navController = navController
                        )
                    }
                    composable("donation_payment") {
                        com.example.halalyticscompose.ui.screens.donation.DonationPaymentScreen(navController = navController)
                    }
                    composable("donation_success") {
                        com.example.halalyticscompose.ui.screens.donation.DonationSuccessScreen(navController = navController)
                    }
                    composable("donation_history") {
                        com.example.halalyticscompose.ui.screens.donation.DonationHistoryScreen(navController = navController)
                    }
                    composable("ai_chat") {
                        MainLayout(navController = navController, isAdmin = isAdmin) {
                            AiChatScreen(navController = navController)
                        }
                    }



                    composable("notification_settings") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            NotificationSettingsScreen(navController = navController)
                        }
                    }

                    composable("barcode_gallery") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            BarcodeGalleryScreen(navController = navController)
                        }
                    }

                    // ═══ HEALTH EXPANSION ROUTES ═══

                    composable("medical_info") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            MedicalRouteGuard(enabled = biometricLockEnabled) {
                                MedicalInfoScreen(navController = navController)
                            }
                        }
                    }

                    composable("bmi_calculator") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            BMICalculatorScreen(navController = navController)
                        }
                    }

                    composable("medicine_search") {
                        MedicineSearchScreen(navController = navController)
                    }

                    composable("add_medicine_reminder") {
                        MedicalRouteGuard(enabled = biometricLockEnabled) {
                            AddMedicineReminderScreen(navController = navController)
                        }
                    }

                    composable("mental_health_hub") {
                        MedicalRouteGuard(enabled = biometricLockEnabled) {
                            MentalHealthHubScreen(navController = navController)
                        }
                    }

                    composable(
                        "mental_health_quiz/{quizType}",
                        arguments = listOf(navArgument("quizType") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val quizType = backStackEntry.arguments?.getString("quizType") ?: "gad7"
                        MedicalRouteGuard(enabled = biometricLockEnabled) {
                            MentalHealthQuizScreen(navController = navController, quizType = quizType)
                        }
                    }

                    composable("daily_mission_dashboard") {
                        DailyMissionDashboardScreen(navController = navController)
                    }

                    composable("help_center") {
                        HelpCenterScreen(navController = navController)
                    }

                    // ═══ BLOOD DONOR ROUTES ═══

                    composable("donor_home") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            val token = sessionManager.getAuthToken() ?: ""
                            com.example.halalyticscompose.ui.screens.donor.DonorHomeScreen(
                                navController = navController,
                                viewModel = donorViewModel,
                                token = token
                            )
                        }
                    }

                    composable("donor_events") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            com.example.halalyticscompose.ui.screens.donor.DonorEventsScreen(
                                navController = navController,
                                viewModel = donorViewModel
                            )
                        }
                    }

                    composable(
                        "donor_event_detail/{eventId}",
                        arguments = listOf(navArgument("eventId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val eventId = backStackEntry.arguments?.getInt("eventId") ?: 0
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            com.example.halalyticscompose.ui.screens.donor.DonorEventDetailScreen(
                                navController = navController,
                                eventId = eventId
                            )
                        }
                    }

                    composable("donor_history") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            val token = sessionManager.getAuthToken() ?: ""
                            com.example.halalyticscompose.ui.screens.donor.DonorHistoryScreen(
                                navController = navController,
                                viewModel = donorViewModel,
                                token = token
                            )
                        }
                    }

                    composable("emergency_blood") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            val emergencies by donorViewModel.activeEmergencies.collectAsState()
                            com.example.halalyticscompose.ui.screens.donor.EmergencyRequestScreen(
                                navController = navController,
                                emergencies = emergencies
                            )
                        }
                    }

                    composable("emergency_request") {
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            val emergencies by donorViewModel.activeEmergencies.collectAsState()
                            com.example.halalyticscompose.ui.screens.donor.EmergencyRequestScreen(
                                navController = navController,
                                emergencies = emergencies
                            )
                        }
                    }

                    composable(
                        "donor_form/{eventId}",
                        arguments = listOf(navArgument("eventId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val eventId = backStackEntry.arguments?.getInt("eventId") ?: 0
                        MainLayout(navController = navController, isAdmin = isAdmin) { paddingValues ->
                            val token = sessionManager.getAuthToken() ?: ""
                            com.example.halalyticscompose.ui.screens.donor.DonorFormScreen(
                                navController = navController,
                                viewModel = donorViewModel,
                                eventId = eventId,
                                token = token
                            )
                        }
                    }
                }
                } // End of Surface
                } // CompositionLocalProvider (Facebook)
            }
        }
    }
}

