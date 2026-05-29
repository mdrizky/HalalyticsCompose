package com.example.halalyticscompose.ui.screens

import android.net.Uri
import java.util.Calendar
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.halalyticscompose.R
import com.example.halalyticscompose.data.model.*
import com.example.halalyticscompose.ui.viewmodel.*
import com.example.halalyticscompose.utils.ImageUtils
import com.example.halalyticscompose.ui.components.*
import com.example.halalyticscompose.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    historyViewModel: HistoryViewModel = hiltViewModel(),
    healthViewModel: HealthViewModel = hiltViewModel(),
    articleViewModel: HealthArticleViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel(),
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    val userData by authViewModel.userData.collectAsState()
    
    // Auto-refresh when screen is displayed to ensure personalized data
    LaunchedEffect(Unit) {
        authViewModel.loadUserProfile()
        healthViewModel.refreshHealthData()
        historyViewModel.refreshAll()
        articleViewModel.loadArticles()
        notificationViewModel.loadNotifications()
    }

    val defaultUserName = stringResource(R.string.home_default_user)
    val rawName = userData?.fullName?.takeIf { it.isNotBlank() } ?: userData?.username?.takeIf { it.isNotBlank() } ?: defaultUserName
    // Format name to Title Case if it's not the default user name
    val name = if (rawName != defaultUserName) rawName.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } } else rawName
    
    val bmiState by healthViewModel.bmi.collectAsState()
    val userBmi = userData?.bmi
    val bmi = if (userBmi != null && userBmi > 0) String.format(java.util.Locale.US, "%.1f", userBmi) else bmiState
    val dailyIntake by healthViewModel.dailyIntake.collectAsState()
    val categories by healthViewModel.categories.collectAsState()
    val articles by articleViewModel.articles.collectAsState()
    val isArticlesLoading by articleViewModel.isLoading.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val banners by historyViewModel.banners.collectAsState()
    val recommendedProducts by historyViewModel.recommendedProducts.collectAsState()
    
    var showAllFeaturesSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            val locationText = userData?.address?.substringBefore(",")?.takeIf { it.isNotBlank() } ?: "Batam, Indonesia"
            PremiumHomeHeader(
                name = name,
                imageUrl = userData?.image,
                unreadCount = unreadCount,
                location = locationText,
                onProfileClick = { navController.navigate("profile") },
                onNotificationClick = { navController.navigate("notifications") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("health_assistant") },
                containerColor = Emerald,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .shadow(20.dp, CircleShape, spotColor = Emerald.copy(alpha = 0.5f), ambientColor = Emerald.copy(alpha = 0.3f))
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = stringResource(R.string.home_action_assistant), modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Premium Health Ring + Summary
                PremiumHealthRingCard(
                    bmi = bmi,
                    dailyIntake = dailyIntake?.dailyIntake,
                    targets = dailyIntake?.targets,
                    onDetailsClick = { navController.navigate("health_monitor") }
                )
            }

            item {
                MedicalAiDisclaimerBanner(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    compact = true
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                AutoSlidingBannerImproved(
                    banners = banners,
                    onClick = { banner -> navigateByBannerAction(navController, banner) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                PremiumCategorySection(
                    categories = categories,
                    onCategoryClick = { categorySlug ->
                        navController.navigate("manual_input?category=$categorySlug")
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                PremiumFeatureGridSection(
                    onActionClick = { route -> navController.navigate(route) },
                    onLainnyaClick = { showAllFeaturesSheet = true }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                PremiumRecommendationSection(
                    products = recommendedProducts,
                    onProductClick = { product -> 
                        navController.navigate("product_detail/${product.barcode}")
                    },
                    onViewAll = { navController.navigate("search_external") }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                PremiumHealthArticlesSection(
                    articles = articles,
                    isLoading = isArticlesLoading,
                    onArticleClick = { article -> 
                        articleViewModel.setSelectedArticle(article)
                        navController.navigate("health_article_detail/${article.id}")
                    },
                    onSeeAllClick = { navController.navigate("health_articles") }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (showAllFeaturesSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAllFeaturesSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                AllFeaturesSheetContent(
                    navController = navController,
                    onClose = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) showAllFeaturesSheet = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PremiumHomeHeader(
    name: String,
    imageUrl: String?,
    unreadCount: Int,
    location: String? = "Indonesia",
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val timeGreeting = when (hour) {
        in 4..10 -> stringResource(R.string.home_greeting_morning)
        in 11..14 -> stringResource(R.string.home_greeting_noon)
        in 15..18 -> stringResource(R.string.home_greeting_afternoon)
        else -> stringResource(R.string.home_greeting_night)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Emerald, TealDark)
                )
            )
            .statusBarsPadding()
            .padding(top = 24.dp, bottom = 32.dp, start = 20.dp, end = 20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .border(2.dp, Gold, CircleShape)
                            .padding(2.dp)
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { onProfileClick() }
                    ) {
                        val profileImageUrl = ImageUtils.normalizeUrl(imageUrl)
                        if (profileImageUrl != null) {
                            AsyncImage(
                                model = profileImageUrl,
                                contentDescription = stringResource(R.string.profile),
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.logo_halalytics_official),
                                contentDescription = stringResource(R.string.profile),
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = timeGreeting,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                        val greetingText = if (name != stringResource(R.string.home_default_user)) {
                            stringResource(R.string.home_greeting_user, name) + " 👋"
                        } else {
                            name
                        }
                        Text(
                            text = greetingText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Glassmorphism Location Card
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, null, tint = Gold, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(location ?: "Indonesia", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box {
                        IconButton(
                            onClick = onNotificationClick,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = stringResource(R.string.notifications),
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        }
                        if (unreadCount > 0) {
                            Surface(
                                color = Error,
                                shape = CircleShape,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(18.dp)
                                    .offset(x = (-2).dp, y = 2.dp),
                                border = BorderStroke(2.dp, Emerald)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumHealthRingCard(
    bmi: String,
    dailyIntake: DailyIntakeData?,
    targets: IntakeTargets?,
    onDetailsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = Emerald.copy(alpha = 0.2f), ambientColor = TealLight.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ring Progress placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(EmeraldLight.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = bmi,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Emerald
                )
                Text(
                    text = stringResource(R.string.home_bmi_label),
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                    color = Slate500
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_healthy_today),
                    fontSize = 14.sp,
                    color = Slate600
                )
                Text(
                    text = stringResource(
                        R.string.home_calorie_intake,
                        dailyIntake?.totalCalories ?: 0,
                        targets?.calorieLimit ?: 2000
                    ),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Slate900
                )
                LinearProgressIndicator(
                    progress = { (dailyIntake?.totalCalories ?: 0).toFloat() / (targets?.calorieLimit ?: 2000).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(8.dp)
                        .clip(CircleShape),
                    color = Emerald,
                    trackColor = EmeraldLight.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AutoSlidingBannerImproved(banners: List<Banner>, onClick: (Banner?) -> Unit) {
    if (banners.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { banners.size })
    
    LaunchedEffect(banners) {
        while (true) {
            delay(4000)
            if (banners.size > 1) {
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % banners.size)
            }
        }
    }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(32.dp))
                .shadow(16.dp, RoundedCornerShape(32.dp))
        ) { page ->
            val banner = banners[page]
            Box(modifier = Modifier.fillMaxSize().clickable { onClick(banner) }) {
                AsyncImage(
                    model = ImageUtils.normalizeUrl(banner.image),
                    contentDescription = banner.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                        .padding(20.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Text(
                            text = banner.title ?: "",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = banner.description ?: "",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumCategorySection(categories: List<com.example.halalyticscompose.data.model.CategoryItem>, onCategoryClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = stringResource(R.string.home_popular_categories),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Text(
                text = stringResource(R.string.home_see_all),
                color = Emerald,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { category ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onCategoryClick(category.slug) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(EmeraldLight.copy(alpha = 0.4f), TealLight.copy(alpha = 0.4f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when(category.icon) {
                            "kitchen" -> Icons.Default.Restaurant
                            "herb" -> Icons.Default.Grass
                            "medication" -> Icons.Default.Medication
                            "spa" -> Icons.Default.Spa
                            else -> Icons.Default.Category
                        }
                        Icon(icon, contentDescription = category.name, tint = Emerald, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = category.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Slate700)
                }
            }
        }
    }
}

@Composable
fun ArticleSkeleton() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate100.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(16.dp)).background(Slate200))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Box(modifier = Modifier.width(60.dp).height(12.dp).background(Slate200))
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth(0.8f).height(16.dp).background(Slate200))
            }
        }
    }
}

@Composable
fun PremiumFeatureGridSection(onActionClick: (String) -> Unit, onLainnyaClick: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.home_featured_services), fontSize = 18.sp, fontWeight = FontWeight.Black, color = Slate900)
            Text(
                text = stringResource(R.string.home_see_all),
                fontSize = 12.sp,
                color = Emerald,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onLainnyaClick() }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                val features = listOf(
                    FeatureItemData(stringResource(R.string.feature_scan_halal_alt), Icons.Default.QrCodeScanner, "scan", Emerald),
                    FeatureItemData(stringResource(R.string.feature_skincare_ai), Icons.Default.AutoAwesome, "skincare_scanner", Color(0xFF8B5CF6)),
                    FeatureItemData(stringResource(R.string.feature_bpom_alt), Icons.Default.HealthAndSafety, "bpom_scanner", Color(0xFF3B82F6)),
                    FeatureItemData(stringResource(R.string.feature_check_medicine_alt), Icons.AutoMirrored.Filled.MenuBook, "drug_interaction", Color(0xFFEF4444)),
                    FeatureItemData(stringResource(R.string.feature_donor_darah), Icons.Default.Favorite, "donor_home", Color(0xFFF43F5E)),
                    FeatureItemData(stringResource(R.string.feature_bmi_calculator_alt), Icons.Default.Calculate, "bmi_calculator", Color(0xFFF59E0B)),
                    FeatureItemData(stringResource(R.string.feature_ai_chat), Icons.AutoMirrored.Filled.Chat, "ai_chat", Color(0xFF6366F1)),
                    FeatureItemData(stringResource(R.string.home_lainnya_id), Icons.Default.GridView, "all_features", Slate500)
                )
                features.chunked(4).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        row.forEach { item ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { if (item.route == "all_features") onLainnyaClick() else onActionClick(item.route) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(item.color.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(item.icon, contentDescription = item.title, tint = item.color, modifier = Modifier.size(26.dp))
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate700,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumRecommendationSection(
    products: List<ProductInfo>,
    onProductClick: (ProductInfo) -> Unit,
    onViewAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stringResource(R.string.home_premium_recommendations), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text(text = stringResource(R.string.home_see_all), color = Emerald, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { onViewAll() })
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(products) { product ->
                Card(
                    modifier = Modifier
                        .width(170.dp)
                        .clickable { onProductClick(product) }
                        .shadow(12.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth().height(130.dp).background(Slate100)) {
                            AsyncImage(
                                model = ImageUtils.normalizeUrl(product.image) ?: "https://ui-avatars.com/api/?name=${product.name}&background=random",
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = product.name ?: "Produk", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(text = product.brand ?: "Local Brand", fontSize = 12.sp, color = Slate500)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumHealthArticlesSection(
    articles: List<HealthArticleItem>,
    isLoading: Boolean,
    onArticleClick: (HealthArticleItem) -> Unit,
    onSeeAllClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stringResource(R.string.home_health_articles), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text(text = stringResource(R.string.home_see_all), color = Emerald, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { onSeeAllClick() })
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (isLoading) {
            repeat(2) { ArticleSkeleton() }
        } else {
            articles.take(3).forEach { article ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { onArticleClick(article) },
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = ImageUtils.normalizeUrl(article.imageUrl),
                            contentDescription = null,
                            modifier = Modifier.size(70.dp).clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = article.category ?: "Kesehatan", fontSize = 11.sp, color = Emerald, fontWeight = FontWeight.Bold)
                            Text(text = article.title, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AllFeaturesSheetContent(navController: NavController, onClose: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(R.string.home_all_features), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = stringResource(R.string.common_close)) }
        }
        Button(
            onClick = { onClose(); navController.navigate("all_features") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Emerald)
        ) {
            Text(stringResource(R.string.home_view_all_services), color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

data class FeatureItemData(val title: String, val icon: ImageVector, val route: String, val color: Color)

private fun navigateByBannerAction(navController: NavController, banner: Banner?) {
    val route = when (banner?.action_type) {
        "open_screen" -> banner.action_value
        "open_news" -> "health_articles"
        else -> "scan"
    }
    if (!route.isNullOrBlank()) navController.navigate(route)
}